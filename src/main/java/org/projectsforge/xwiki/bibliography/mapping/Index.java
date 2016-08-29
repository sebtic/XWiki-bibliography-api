package org.projectsforge.xwiki.bibliography.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.bibliography.Constants;
import org.projectsforge.xwiki.bibliography.Error;
import org.projectsforge.xwiki.bibliography.Utils;
import org.projectsforge.xwiki.bibliography.macro.Scope;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import de.undercouch.citeproc.csl.CSLItemData;

/**
 * The Class Index.
 */
public class Index {

  /** The Constant FIELD_BIBLIOGRAPHY_PAGE. */
  public static final String FIELD_BIBLIOGRAPHY_PAGE = "bibliographyPage";

  /** The Constant FIELD_ENTRIES. */
  public static final String FIELD_ENTRIES = "entries";

  /** The Constant FIELD_EXPIRED. */
  public static final String FIELD_EXPIRED = "expired";

  /** The Constant FIELD_KEYS. */
  public static final String FIELD_KEYS = "keys";

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(Index.class);

  /**
   * Gets the class reference.
   *
   * @param entityReference
   *          the entity reference
   * @return the class reference
   */
  public static DocumentReference getClassReference(EntityReference entityReference) {
    return new DocumentReference(entityReference.extractReference(EntityType.WIKI).getName(),
        Constants.CODE_SPACE_NAME_AS_LIST, "IndexClass");
  }

  /**
   * Gets the class reference.
   *
   * @param document
   *          the document
   * @return the class reference
   */
  public static DocumentReference getClassReference(XWikiDocument document) {
    return getClassReference(document.getDocumentReference());
  }

  /** The document. */
  private XWikiDocument document;

  /** The service. */
  private BibliographyService service;

  /** The xobject. */
  private BaseObject xobject;

  /**
   * Instantiates a new index.
   *
   * @param service
   *          the service
   * @param document
   *          the document
   */
  public Index(BibliographyService service, XWikiDocument document) {
    this.service = service;
    this.document = document;
    this.xobject = document.getXObject(getClassReference(document), true, service.getContext());
  }

  /**
   * Gets the bibliography page.
   *
   * @return the bibliography page
   */
  public String getBibliographyPage() {
    return StringUtils.defaultString(xobject.getStringValue(FIELD_BIBLIOGRAPHY_PAGE));
  }

  /**
   * Gets the bibliography style.
   *
   * @return the bibliography style
   */
  public String getBibliographyStyle() {
    String style = "";
    if (xobject != null) {
      style = xobject.getLargeStringValue(Configuration.FIELD_BIBLIOGRAPHY_MAIN_STYLE);
    }
    if (StringUtils.isBlank(style)) {
      style = service.getDefaultConfiguration(document.getDocumentReference().getWikiReference())
          .getBibliographyStyle(Configuration.FIELD_BIBLIOGRAPHY_MAIN_STYLE);
    }
    if (StringUtils.isBlank(style)) {
      style = "ieee";
    }
    return style;
  }

  /**
   * Gets the document.
   *
   * @return the document
   */
  public XWikiDocument getDocument() {
    return document;
  }

  /**
   * Gets the entries.
   *
   * @return the entries
   */
  public List<CSLItemData> getEntries() {
    return Utils.deserializeCSLItemDatas(xobject.getLargeStringValue(FIELD_ENTRIES));
  }

  /**
   * Gets the extra wiki sources from which entries are retrieved.
   *
   * @return the extra wiki sources
   */
  public List<String> getExtraWikiSources() {
    List<String> results = new ArrayList<>();
    if (xobject != null) {
      results.addAll(Arrays.asList(StringUtils
          .defaultString(xobject.getLargeStringValue(Configuration.FIELD_EXTRA_SOURCES)).trim().split("\\|")));
    }
    results.addAll(
        service.getDefaultConfiguration(document.getDocumentReference().getWikiReference()).getExtraWikiSources());
    return results;
  }

  /**
   * Gets the keys.
   *
   * @return the keys
   */
  public List<String> getKeys() {
    return Utils.deserializeKeys(service, xobject.getLargeStringValue(FIELD_KEYS));
  }

  /**
   * Gets the scope.
   *
   * @return the scope
   */
  public Scope getScope() {
    Scope scope = Scope.UNDEFINED;

    if (xobject != null) {
      scope = Scope.toScope(StringUtils.defaultString(xobject.getStringValue(Configuration.FIELD_SCOPE)).trim());
    }
    if (scope == Scope.UNDEFINED) {
      scope = service.getDefaultConfiguration(document.getDocumentReference().getWikiReference()).getScope();
    }
    return scope;
  }

  /**
   * Checks if is expired.
   *
   * @return true, if is expired
   */
  public boolean isExpired() {
    return xobject.getIntValue(FIELD_EXPIRED, 0) == 1;
  }

  /**
   * Save.
   */
  public void save() {
    // ensure only one update is done at a time
    synchronized (Index.class) {
      // save changes
      XWikiContext context = service.getContext();
      try {
        context.getWiki().saveDocument(document, context);
      } catch (XWikiException ex) {
        service.addError(Error.SAVE_DOCUMENT, document.getDocumentReference());
        logger.warn("An error occurred while saving index", ex);
      }
    }
  }

  /**
   * Sets the bibliography page.
   *
   * @param bibliographyPage
   *          the new bibliography page
   */
  public void setBibliographyPage(String bibliographyPage) {
    xobject.setStringValue(FIELD_BIBLIOGRAPHY_PAGE, bibliographyPage);
  }

  /**
   * Sets the CSL entries.
   *
   * @param entries
   *          the new CSL entries
   */
  public void setCSLEntries(List<CSLItemData> entries) {
    xobject.setLargeStringValue(FIELD_ENTRIES, Utils.serializedCSLItemDatas(entries));
  }

  /**
   * Sets the expired.
   *
   * @param expired
   *          the new expired
   */
  public void setExpired(boolean expired) {
    xobject.setIntValue(FIELD_EXPIRED, expired ? 1 : 0);
  }

  /**
   * Sets the keys.
   *
   * @param keys
   *          the new keys
   */
  public void setKeys(List<String> keys) {
    xobject.setLargeStringValue(FIELD_KEYS, Utils.serializeKeys(service, keys));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Index [document=" + document.getDocumentReference() + ", xobject=" + xobject + "]";
  }

  /**
   * Update index if necessary.
   */
  public void update() {
    if (!isExpired()) {
      return;
    }

    // ensure only one update is done at a time
    synchronized (Index.class) {
      // collect all page tree from this index (included)
      List<XWikiDocument> tree = Utils.getDocumentTree(service, document);

      // collect informations
      List<String> keys = new ArrayList<>();
      Set<String> keysSet = new HashSet<>();
      DocumentReference bibliographyPage = null;

      for (XWikiDocument page : tree) {
        LocalIndex localIndex = new LocalIndex(service, page, this);
        // collect cited keys in order
        for (String key : localIndex.getKeys()) {
          if (!keysSet.contains(key)) {
            keys.add(key);
            keysSet.add(key);
          }
        }
        if (localIndex.getIsBibliographyPage()) {
          if (bibliographyPage != null) {
            logger.warn("Multiple bibliography page found {} : {}", this, localIndex);
          }
          bibliographyPage = page.getDocumentReference();
        }
      }
      setKeys(keys);

      setBibliographyPage(bibliographyPage == null ? "" : bibliographyPage.toString());

      // load entries
      List<CSLItemData> entries = new ArrayList<>();
      for (String key : keys) {
        Entry entry = service.findEntry(this, key);
        if (entry != null) {
          entries.add(entry.getCSLItemData());
        }
      }
      // save all entries for fast access
      setCSLEntries(entries);

      // all update are done
      setExpired(false);
    }
  }

}
