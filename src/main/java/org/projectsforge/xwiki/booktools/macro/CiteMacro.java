package org.projectsforge.xwiki.booktools.macro;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.projectsforge.xwiki.booktools.Constants;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xpn.xwiki.XWikiContext;

// TODO: Auto-generated Javadoc
/**
 * The Cite macro implementation .
 */
@Component
@Named(CiteMacro.MACRO_NAME)
public class CiteMacro extends AbstractMacro<CiteMacroParameters> {

  /** The Constant DESCRIPTION. */
  private static final String DESCRIPTION = "Cite bibliographic entries. "
      + "Multiple entries can be defined if separated by acoma "
      + "and a locator can be specified using bracket without "
      + "space after the key between the opening bracket and the citation key. Example: key1,key2[chapter 6],key3[p. 10-12]";

  /** The Constant MACRO_NAME. */
  public static final String MACRO_NAME = "cite";

  /** The Constant MACRO_BLOCK_MATCHER. */
  public static final MacroBlockMatcher MACRO_BLOCK_MATCHER = new MacroBlockMatcher(MACRO_NAME);

  /** The Constant WYSIWYG_NAME. */
  public static final String WYSIWYG_NAME = "Cite a bibliographic entry";

  /** The component manager provider. */
  @Inject
  @Named("context")
  private Provider<ComponentManager> componentManagerProvider;

  /** The xwiki context provider. */
  @Inject
  private Provider<XWikiContext> xwikiContextProvider;

  /**
   * Create and initialize the descriptor of the macro.
   */
  public CiteMacro() {
    super(WYSIWYG_NAME, DESCRIPTION, CiteMacroParameters.class);
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
  public List<Block> execute(CiteMacroParameters parameters, String content, MacroTransformationContext context)
      throws MacroExecutionException {

    XWikiContext xcontext = xwikiContextProvider.get();

    if ("edit".equals(xcontext.getAction())) {
      // in edit mode, we simply display the keys
      List<Block> blocks = parse("**[" + ((parameters.getKeys() == null) ? "" : parameters.getKeys().trim())
          + (parameters.isHidden() ? "|HIDDEN" : "") + "]**", "xwiki/2.1").getChildren();
      if (context.isInline()) {
        // remove the ParagraphBlock
        blocks = blocks.get(0).getChildren();
      }
      return blocks;
    } else {
      Block root = context.getXDOM();
      // search for an existing bibliography macro
      Block matchingBlock = root.getFirstBlock(BibliographyMacro.MACRO_BLOCK_MATCHER, Block.Axes.DESCENDANT);
      if (matchingBlock == null) {
        // add the bibliography macro
        root.addChild(parse("{{bibliography scope=\"hidden\"/}}", "xwiki/2.1"));
      }
      return Collections.emptyList();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.rendering.macro.AbstractMacro#getPriority()
   */
  @Override
  public int getPriority() {
    // need to be processed early to add the macro bibliography for further
    // processing
    // the value is chosen in order to add the bibliography just after footnote
    return 499;
  }

  /**
   * Parses the content in given syntax.
   *
   * @param content
   *          the content
   * @param syntax
   *          the syntax
   * @return the xdom
   */
  private XDOM parse(String content, String syntax) {
    XDOM result;
    try {
      Parser parser = componentManagerProvider.get().getInstance(Parser.class, syntax);
      result = parser.parse(new StringReader(content));
    } catch (Exception e) {
      result = null;
    }
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.rendering.macro.Macro#supportsInlineMode()
   */
  @Override
  public boolean supportsInlineMode() {
    return true;
  }

}
