package org.projectsforge.xwiki.booktools.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.Constants;
import org.projectsforge.xwiki.booktools.Utils;
import org.projectsforge.xwiki.booktools.macro.Scope;
import org.projectsforge.xwiki.booktools.mapping.DocumentWalker.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.objects.BaseObject;

import de.undercouch.citeproc.csl.CSLItemData;

/**
 * The Class Index.
 */
public class Index {

  /** The Constant CLASS_REFERENCE. */
  public static final EntityReference CLASS_REFERENCE = new EntityReference("IndexClass", EntityType.DOCUMENT,
      Constants.CODE_SPACE_REFERENCE);

  /** The Constant CLASS_REFERENCE_AS_STRING. */
  public static final String CLASS_REFERENCE_AS_STRING = Constants.CODE_SPACE_NAME_AS_STRING + ".IndexClass";

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

  /** The node. */
  private Node node;

  /** The xobject. */
  private BaseObject xobject;

  /**
   * Instantiates a new index.
   *
   * @param node
   *          the node
   */
  Index(Node node) {
    this.node = node;
    this.xobject = node.getXObject(CLASS_REFERENCE, true);
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
      style = node.getService().getDefaultConfiguration(node.getDocumentReference().getWikiReference())
          .getBibliographyStyle(Configuration.FIELD_BIBLIOGRAPHY_MAIN_STYLE);
    }
    if (StringUtils.isBlank(style)) {
      style = "ieee";
    }
    return style;
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
    results.addAll(node.getService().getDefaultConfiguration(node.getDocumentReference().getWikiReference())
        .getExtraWikiSources());
    return results;
  }

  /**
   * Gets the keys.
   *
   * @return the keys
   */
  public List<String> getKeys() {
    return Utils.deserializeKeys(node.getService(), xobject.getLargeStringValue(FIELD_KEYS));
  }

  /**
   * Gets the node.
   *
   * @return the node
   */
  public Node getNode() {
    return node;
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
      scope = node.getService().getDefaultConfiguration(node.getDocumentReference().getWikiReference()).getScope();
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
    xobject.setLargeStringValue(FIELD_KEYS, Utils.serializeKeys(node.getService(), keys));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Index [document=" + node.getDocumentReference() + ", xobject=" + xobject + "]";
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
      List<Node> tree = node.getTree();

      // collect informations
      List<String> keys = new ArrayList<>();
      Set<String> keysSet = new HashSet<>();
      DocumentReference bibliographyPage = null;

      for (Node page : tree) {
        LocalIndex localIndex = page.wrapAsLocalIndex(this);
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
        Entry entry = node.getService().findEntry(this, key);
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
