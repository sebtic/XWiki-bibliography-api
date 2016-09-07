package org.projectsforge.xwiki.booktools.macro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringJoiner;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.Constants;
import org.projectsforge.xwiki.booktools.mapping.DocumentWalker.Node;
import org.projectsforge.xwiki.booktools.mapping.Index;
import org.projectsforge.xwiki.booktools.mapping.LocalIndex;
import org.projectsforge.xwiki.booktools.service.BookToolsService;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;

/**
 * The Class BibliographyMacro.
 */
@Component
@Named(BibliographyMacro.MACRO_NAME)
public class BibliographyMacro extends AbstractMacro<BibliographyMacroParameters> {

  /** The Constant DESCRIPTION. */
  private static final String DESCRIPTION = "Print bibliographic entries. Automatically triggered by the cite macro if unspecified.";

  /** The Constant MACRO_NAME. */
  public static final String MACRO_NAME = "bibliography";

  /** The Constant MACRO_BLOCK_MATCHER. */
  public static final MacroBlockMatcher MACRO_BLOCK_MATCHER = new MacroBlockMatcher(MACRO_NAME);

  /** The Constant WYSIWYG_NAME. */
  public static final String WYSIWYG_NAME = "Print the bibliography";

  /** The logger. */
  @Inject
  private Logger logger;

  /** The macro content parser. */
  @Inject
  private MacroContentParser macroContentParser;

  /** The service. */
  @Inject
  private BookToolsService service;

  /** The xwiki context provider. */
  @Inject
  private Provider<XWikiContext> xwikiContextProvider;

  /**
   * Create and initialize the descriptor of the macro.
   */
  public BibliographyMacro() {
    super(WYSIWYG_NAME, DESCRIPTION, BibliographyMacroParameters.class);
    setDefaultCategory(Constants.WYSIWYG_MACRO_CATEGORY);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.rendering.macro.Macro#execute(java.lang.Object,
   * java.lang.String,
   * org.xwiki.rendering.transformation.MacroTransformationContext)
   */
  @Override
  public List<Block> execute(BibliographyMacroParameters parameters, String content,
      MacroTransformationContext transformationContext) throws MacroExecutionException {

    XWikiContext context = xwikiContextProvider.get();

    if ("edit".equals(context.getAction())) {
      return Collections.<Block> emptyList();
    }

    XWikiDocument document = context.getDoc();

    Block root = transformationContext.getXDOM();

    /**** COLLECT CITATION KEYS AND SCOPE ****/

    Scope scope = null;

    StringJoiner allKeys = new StringJoiner(",");

    // search for cite macros
    List<MacroMarkerBlock> cites = root.getBlocks(new ClassBlockMatcher(MacroMarkerBlock.class), Block.Axes.DESCENDANT);
    for (ListIterator<MacroMarkerBlock> it = cites.listIterator(); it.hasNext();) {
      MacroMarkerBlock macro = it.next();
      if (CiteMacro.MACRO_NAME.equals(macro.getId())) { // collect cited keys
        String keys = macro.getParameter(CiteMacroParameters.PARAM_KEYS);
        if (StringUtils.isNoneBlank(keys)) {
          allKeys.add(keys);
        }
        // keep cite macro
        continue;
      } else if (MACRO_NAME.equals(macro.getId())) {
        // save scope
        if (scope != null) {
          logger.warn("Multiple bibliography fond on page {}", document.getDocumentReference());
        }
        scope = Scope.toScope(macro.getParameter("scope"));
        // remove the bibliography macro from the rendered document
        macro.getParent().replaceChild(Collections.<Block> emptyList(), macro);
      }
      // remove anything else from the list
      it.remove();
    }

    /**** UPDATE LOCAL INDEX ****/

    Node node = service.getDocumentWalker().wrapNode(document);

    // Find global index for overrided configuration
    Index index = node.getRootNode().wrapIfIndex();

    // load local index
    LocalIndex localIndex = node.wrapAsLocalIndex(index);

    // decode and save local keys
    localIndex.setKeys(CiteKey.decodeUniqueKeys(allKeys.toString()));

    scope = Scope.toScope(parameters.getScope());
    if (scope == Scope.UNDEFINED) {
      // normalize and update scope
      if (index != null) {
        scope = index.getScope();
      } else {
        scope = service.getDefaultConfiguration(document.getDocumentReference().getWikiReference()).getScope();
      }
    }
    if (scope == Scope.UNDEFINED || scope == null) {
      scope = Scope.HIDDEN;
    }
    localIndex.setIsBookToolsPage(scope == Scope.CITED);

    // save changes if necessary, through listeners, it will trigger an update
    // of the index
    localIndex.save();

    /**** RELOAD INDEX (to get the last updates) ****/
    index = service.getDocumentWalker().wrapNode(document).getRootNode().wrapIfIndex();

    /**** GENERATE CONTENT ****/
    List<Block> results;

    if (index == null) {
      results = Collections.singletonList(parseContent("**Missing bibliography index.**", false));
    } else {
      // update and save index if necessary
      index.update();
      index.getNode().save();

      // generate bibliography with all keys
      CSL csl = service.getCSL(index);
      Bibliography bibliography = csl.makeBibliography();

      // handle cite macro
      for (MacroMarkerBlock cite : cites) {
        cite.getParent().replaceChild(makeCiteBlocks(index, csl, scope, cite), cite);
      }

      // handle bibliography
      switch (scope) {
        case CITED:
          results = makeBookToolsBlocks(index, bibliography, index.getKeys(), scope);
          break;
        case HIDDEN:
          results = Collections.<Block> emptyList();
          break;
        case PAGE:
          results = makeBookToolsBlocks(index, bibliography, localIndex.getKeys(), scope);
          break;
        default:
          results = Collections
              .singletonList(parseContent("**An error occurred while handling bibliography.**", false));
          break;
      }
    }
    return results;
  }

  /**
   * Make bibliography blocks.
   *
   * @param index
   *          the index
   * @param bibliography
   *          the bibliography
   * @param citedKeys
   *          the cited keys
   * @param scope
   *          the scope
   * @return the list
   * @throws MacroExecutionException
   *           the macro execution exception
   */
  private List<Block> makeBookToolsBlocks(Index index, Bibliography bibliography, List<String> citedKeys, Scope scope)
      throws MacroExecutionException {
    Set<String> lookup = new HashSet<>(citedKeys);

    String[] entryIds = bibliography.getEntryIds();
    String[] entries = bibliography.getEntries();

    StringBuilder builder = new StringBuilder();
    if (scope == Scope.PAGE) {
      builder.append("----");
    }
    if (bibliography.getBibStart() != null) {
      builder.append(bibliography.getBibStart());
    }
    if (entries != null) {
      for (int i = 0; i < entryIds.length; ++i) {
        if (lookup.contains(entryIds[i])) {
          String content = entries[i];
          content = content.replaceAll(Constants.ENTRY_TARGET_MARK,
              service.findEntry(index, entryIds[i]).getNode().getDocumentReference().toString());
          builder.append(content);
        }
      }
    }
    if (bibliography.getBibEnd() != null) {
      builder.append(bibliography.getBibEnd());
    }

    return Collections.<Block> singletonList(parseContent(builder.toString(), false));
  }

  /**
   * Make cite blocks.
   *
   * @param index
   *          the index
   * @param csl
   *          the csl
   * @param scope
   *          the scope
   * @param cite
   *          the cite
   * @return the list
   * @throws MacroExecutionException
   *           the macro execution exception
   */
  private List<Block> makeCiteBlocks(Index index, CSL csl, Scope scope, MacroMarkerBlock cite)
      throws MacroExecutionException {

    // extract hidden parameter
    boolean hidden;
    String hiddenParameter = cite.getParameter(CiteMacroParameters.PARAM_HIDDEN);
    if (hiddenParameter == null) {
      hidden = CiteMacroParameters.HIDDEN_DEFAULT;
    } else {
      hidden = Boolean.parseBoolean(hiddenParameter);
    }

    if (hidden) {
      // remove cite block since it is hidden
      return Collections.<Block> emptyList();
    }

    List<CSLCitationItem> citationItems = new ArrayList<>();
    for (CiteKey ck : CiteKey.decode(cite.getParameter(CiteMacroParameters.PARAM_KEYS))) {
      citationItems.add(new CSLCitationItem(ck.getKey(), null, null, null, ck.getLocator(), null, null, null, null,
          null, null, null, null));
    }

    // handle empty keys
    if (citationItems.isEmpty()) {
      // remove cite block since it is empty
      return Collections.<Block> emptyList();
    }

    List<Block> results = new ArrayList<>();
    try {
      for (Citation citation : csl.makeCitation(new CSLCitation(citationItems.toArray(new CSLCitationItem[0])))) {
        String text = citation.getText();
        if (scope == Scope.PAGE) {
          // link to current page
          text = text.replaceAll(Constants.CITE_TARGET_MARK, "");
        } else {
          // link to bibliography page
          text = text.replaceAll(Constants.CITE_TARGET_MARK, index.getBibliographyPage());
        }
        results.add(parseContent(text, true));
      }
    } catch (IllegalArgumentException ex) {
      logger.warn("Could not make citations for " + cite.getParameter(CiteMacroParameters.PARAM_KEYS), ex);
    }
    return results;
  }

  /**
   * Parses the content.
   *
   * @param content
   *          the content
   * @param inline
   *          the inline
   * @return the xdom
   * @throws MacroExecutionException
   *           the macro execution exception
   */
  private XDOM parseContent(String content, boolean inline) throws MacroExecutionException {
    MacroTransformationContext parserContext = new MacroTransformationContext();
    parserContext.setSyntax(Syntax.XWIKI_2_1);
    return macroContentParser.parse(content, parserContext, true, inline);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.rendering.macro.Macro#supportsInlineMode()
   */
  @Override
  public boolean supportsInlineMode() {
    return false;
  }

}
