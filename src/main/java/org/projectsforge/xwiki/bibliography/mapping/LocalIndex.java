package org.projectsforge.xwiki.bibliography.mapping;

import java.util.Collections;
import java.util.List;

import org.projectsforge.xwiki.bibliography.Constants;
import org.projectsforge.xwiki.bibliography.Error;
import org.projectsforge.xwiki.bibliography.Utils;
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

/**
 * The Class LocalIndex.
 */
public class LocalIndex {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(LocalIndex.class);

  /** The Constant FIELD_IS_BIBLIOGRAPHY_PAGE. */
  public static final String FIELD_IS_BIBLIOGRAPHY_PAGE = "isBibliographyPage";

  /** The Constant FIELD_KEYS. */
  public static final String FIELD_KEYS = "keys";

  /**
   * Gets the class reference.
   *
   * @param entityReference
   *          the entity reference
   * @return the class reference
   */
  public static DocumentReference getClassReference(EntityReference entityReference) {
    return new DocumentReference(entityReference.extractReference(EntityType.WIKI).getName(),
        Constants.CODE_SPACE_NAME_AS_LIST, "LocalIndexClass");
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

  /**
   * Gets the class reference as string.
   *
   * @return the class reference as string
   */
  public static Object getClassReferenceAsString() {
    return Constants.CODE_SPACE_NAME_AS_STRING + ".LocalIndexClass";
  }

  /** The xobject. */
  private BaseObject xobject;

  /** The document. */
  private XWikiDocument document;

  /** The index. */
  private Index index;

  /** The service. */
  private BibliographyService service;

  /** The is bibliography page. */
  private Boolean isBibliographyPage;

  /** The keys. */
  private List<String> keys;

  /** The dirty. */
  private boolean dirty;

  /**
   * Instantiates a new local index.
   *
   * @param service
   *          the service
   * @param document
   *          the document
   * @param index
   *          the index
   */
  public LocalIndex(BibliographyService service, XWikiDocument document, Index index) {
    this.service = service;
    this.document = document;
    this.xobject = document.getXObject(getClassReference(document));
    this.index = index;

    if (xobject != null) {
      this.isBibliographyPage = xobject.getIntValue(FIELD_IS_BIBLIOGRAPHY_PAGE) == 1;
      this.keys = Utils.deserializeKeys(service, xobject.getLargeStringValue(FIELD_KEYS));
    } else {
      isBibliographyPage = Boolean.FALSE;
      this.keys = Collections.emptyList();
    }
    this.dirty = false;
  }

  /**
   * Tests if the document is a bibliography page.
   *
   * @return true if the document is a bibliography page
   */
  public boolean getIsBibliographyPage() {
    return isBibliographyPage;
  }

  /**
   * Gets the keys.
   *
   * @return the keys
   */
  public List<String> getKeys() {
    return keys;
  }

  /**
   * Save.
   */
  public void save() {
    try {
      if (dirty) {
        XWikiContext context = service.getContext();
        if (keys.isEmpty() && !isBibliographyPage) {
          // the local index is no more necessary => remove it
          document.removeXObject(xobject);
        } else {
          if (xobject == null) {
            xobject = document.newXObject(getClassReference(document), context);
          }
          xobject.setIntValue(FIELD_IS_BIBLIOGRAPHY_PAGE, isBibliographyPage ? 1 : 0);
          xobject.setLargeStringValue(FIELD_KEYS, Utils.serializeKeys(service, keys));
        }
        context.getWiki().saveDocument(document, context);
        // IndexUpdaterListener is triggered so index is marked as dirty
        if (index != null) {
          index.setExpired(true);
        }
      }
    } catch (XWikiException ex) {
      service.addError(Error.SAVE_DOCUMENT, document.getDocumentReference());
      logger.warn("Failed saving document", ex);
    }
  }

  /**
   * Sets if the document is a bibliography page.
   *
   * @param isBibliographyPage
   *          if the document is a bibliography page
   */
  public void setIsBibliographyPage(boolean isBibliographyPage) {
    if (this.isBibliographyPage != isBibliographyPage) {
      dirty = true;
    }
    this.isBibliographyPage = isBibliographyPage;
  }

  /**
   * Sets the keys.
   *
   * @param keys
   *          the new keys
   */
  public void setKeys(List<String> keys) {
    if (!this.keys.equals(keys)) {
      dirty = true;
    }
    this.keys = keys;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "LocalIndex [document=" + document.getDocumentReference() + ", xobject=" + xobject + ", index=" + index
        + "]";
  }
}
