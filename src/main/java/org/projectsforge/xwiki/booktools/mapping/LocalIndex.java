package org.projectsforge.xwiki.booktools.mapping;

import java.util.Collections;
import java.util.List;

import org.projectsforge.xwiki.booktools.Constants;
import org.projectsforge.xwiki.booktools.Utils;
import org.projectsforge.xwiki.booktools.mapping.DocumentWalker.Node;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.objects.BaseObject;

/**
 * The Class LocalIndex.
 */
public class LocalIndex {

  /** The Constant CLASS_REFERENCE. */
  public static final EntityReference CLASS_REFERENCE = new EntityReference("LocalIndexClass", EntityType.DOCUMENT,
      Constants.CODE_SPACE_REFERENCE);

  /** The Constant CLASS_REFERENCE_AS_STRING. */
  public static final String CLASS_REFERENCE_AS_STRING = Constants.CODE_SPACE_NAME_AS_STRING + ".LocalIndexClass";

  /** The Constant FIELD_IS_BIBLIOGRAPHY_PAGE. */
  public static final String FIELD_IS_BIBLIOGRAPHY_PAGE = "isBibliographyPage";

  /** The Constant FIELD_KEYS. */
  public static final String FIELD_KEYS = "keys";

  /** The dirty. */
  private boolean dirty;

  /** The index. */
  private Index index;

  /** The is bibliography page. */
  private Boolean isBibliographyPage;

  /** The keys. */
  private List<String> keys;

  /** The node. */
  private Node node;

  /** The xobject. */
  private BaseObject xobject;

  /**
   * Instantiates a new local index.
   *
   * @param node
   *          the node
   * @param index
   *          the index
   */
  public LocalIndex(Node node, Index index) {
    this.node = node;
    this.xobject = node.getXObject(CLASS_REFERENCE);
    this.index = index;

    if (xobject != null) {
      this.isBibliographyPage = xobject.getIntValue(FIELD_IS_BIBLIOGRAPHY_PAGE) == 1;
      this.keys = Utils.deserializeKeys(node.getService(), xobject.getLargeStringValue(FIELD_KEYS));
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
    if (dirty) {
      if (keys.isEmpty() && !isBibliographyPage) {
        // the local index is no more necessary => remove it
        node.removeXObjects(CLASS_REFERENCE);
      } else {
        if (xobject == null) {
          xobject = node.newXObject(CLASS_REFERENCE);
        }
        xobject.setIntValue(FIELD_IS_BIBLIOGRAPHY_PAGE, isBibliographyPage ? 1 : 0);
        xobject.setLargeStringValue(FIELD_KEYS, Utils.serializeKeys(node.getService(), keys));
      }
      node.save();
      // IndexUpdaterListener is triggered so index is marked as dirty
      if (index != null) {
        index.setExpired(true);
      }
    }
  }

  /**
   * Sets if the document is a bibliography page.
   *
   * @param isBibliographyPage
   *          if the document is a bibliography page
   */
  public void setIsBookToolsPage(boolean isBibliographyPage) {
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
    return "LocalIndex [document=" + node.getDocumentReference() + ", xobject=" + xobject + ", index=" + index + "]";
  }
}
