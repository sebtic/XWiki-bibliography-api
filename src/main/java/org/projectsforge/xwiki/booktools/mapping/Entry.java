package org.projectsforge.xwiki.booktools.mapping;

import java.io.IOException;

import org.projectsforge.xwiki.booktools.Constants;
import org.projectsforge.xwiki.booktools.Error;
import org.projectsforge.xwiki.booktools.Utils;
import org.projectsforge.xwiki.booktools.biblatex.BibLaTeXExporter;
import org.projectsforge.xwiki.booktools.fields.CSLCategoriesFields;
import org.projectsforge.xwiki.booktools.fields.CSLDateFields;
import org.projectsforge.xwiki.booktools.fields.CSLNameFields;
import org.projectsforge.xwiki.booktools.fields.CSLStringFields;
import org.projectsforge.xwiki.booktools.fields.CSLTypeFields;
import org.projectsforge.xwiki.booktools.mapping.DocumentWalker.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.objects.BaseObject;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ListItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.output.Bibliography;

/**
 * The Class Entry.
 */
public class Entry {

  /** The Constant CLASS_REFERENCE. */
  public static final EntityReference CLASS_REFERENCE = new EntityReference("EntryClass", EntityType.DOCUMENT,
      Constants.CODE_SPACE_REFERENCE);

  /** The Constant CLASS_REFERENCE_AS_STRING. */
  public static final String CLASS_REFERENCE_AS_STRING = Constants.CODE_SPACE_NAME_AS_STRING + ".EntryClass";

  /** The Constant FIELD_BIBLATEX. */
  private static final String FIELD_BIBLATEX = "biblatex";

  /** The Constant FIELD_CSL_ITEM_DATA. */
  public static final String FIELD_CSL_ITEM_DATA = "CSLItemData";

  /** The Constant FIELD_RENDERED. */
  private static final String FIELD_RENDERED = "rendered";

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(Entry.class);

  /** The Constant NAME_PREFIX. */
  public static final String NAME_PREFIX = Constants.ENTRIES_SPACE_NAME_AS_STRING + ".Entry-";

  /** The Constant NAME_SUFFIX. */
  public static final String NAME_SUFFIX = ".WebHome";

  /** The node. */
  private Node node;

  /** The xobject. */
  private BaseObject xobject;

  /**
   * Instantiates a new entry.
   *
   * @param node
   *          the node
   */
  public Entry(Node node) {
    this.node = node;
    this.xobject = node.getXObject(CLASS_REFERENCE, true);
  }

  /**
   * Fill from CSL object.
   *
   * @param authorReference
   *          the author reference
   * @param itemData
   *          the item data
   */
  public void fillFromCSLObject(DocumentReference authorReference, CSLItemData itemData) {
    for (CSLTypeFields field : CSLTypeFields.values()) {
      field.fillFromCSLObject(node.getService(), xobject, itemData);
    }

    for (CSLStringFields field : CSLStringFields.values()) {
      field.fillFromCSLObject(node.getService(), xobject, itemData);
    }

    for (CSLNameFields field : CSLNameFields.values()) {
      field.fillFromCSLObject(node.getService(), xobject, itemData, authorReference);
    }

    for (CSLDateFields field : CSLDateFields.values()) {
      field.fillFromCSLObject(node.getService(), xobject, itemData);
    }

    for (CSLCategoriesFields field : CSLCategoriesFields.values()) {
      field.fillFromCSLObject(node.getService(), xobject, itemData);
    }
  }

  /**
   * Gets the CSL item data.
   *
   * @return the CSL item data
   */
  public CSLItemData getCSLItemData() {
    return Utils.deserializeCSLItemData(node.getService(), xobject.getLargeStringValue(FIELD_CSL_ITEM_DATA));
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
   * Gets the x object.
   *
   * @return the x object
   */
  public BaseObject getXObject() {
    return xobject;
  }

  /**
   * Update some fields (triggered when the document is saved through events).
   */
  public void update() {
    try {
      CSLItemDataBuilder builder = new CSLItemDataBuilder();

      for (CSLTypeFields field : CSLTypeFields.values()) {
        field.fillFromXObject(node.getService(), builder, xobject);
      }

      for (CSLStringFields field : CSLStringFields.values()) {
        field.fillFromXObject(node.getService(), builder, xobject);
      }

      for (CSLNameFields field : CSLNameFields.values()) {
        field.fillFromXObject(node.getService(), builder, xobject);
      }

      for (CSLDateFields field : CSLDateFields.values()) {
        field.fillFromXObject(node.getService(), builder, xobject);
      }

      for (CSLCategoriesFields field : CSLCategoriesFields.values()) {
        field.fillFromXObject(node.getService(), builder, xobject);
      }

      CSLItemData itemData = builder.build();
      xobject.setLargeStringValue(FIELD_CSL_ITEM_DATA, Utils.serializeCSLItemData(itemData));

      CSL csl = new CSL(new ListItemDataProvider(itemData),
          node.getService().getDefaultConfiguration(node.getDocumentReference().getWikiReference())
              .getBibliographyStyle(Configuration.FIELD_BIBLIOGRAPHY_ENTRY_STYLE));
      csl.registerCitationItems(itemData.getId());
      csl.setOutputFormat("text");
      Bibliography bibiography = csl.makeBibliography();
      String rendered = bibiography.getEntries()[0].trim();
      rendered = rendered.replaceAll(Constants.ENTRY_TARGET_MARK, node.getDocumentReference().toString());

      node.getXWikiDocument().setTitle(itemData.getId());
      xobject.setLargeStringValue(FIELD_RENDERED, rendered);
      xobject.setLargeStringValue(FIELD_BIBLATEX, BibLaTeXExporter.export(itemData));
    } catch (IOException ex) {
      node.getService().addError(Error.CSL, node.getDocumentReference(), xobject, ex.getMessage());
      logger.warn("Can not format title", ex);
    } catch (Exception ex) {
      node.getService().addError(Error.BUILD_CSLDATAITEM, node.getDocumentReference(), xobject, ex.getMessage());
      logger.warn("An error occurred", ex);
    }
  }

}
