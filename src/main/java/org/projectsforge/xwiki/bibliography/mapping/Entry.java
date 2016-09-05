package org.projectsforge.xwiki.bibliography.mapping;

import java.io.IOException;

import org.projectsforge.xwiki.bibliography.Constants;
import org.projectsforge.xwiki.bibliography.Error;
import org.projectsforge.xwiki.bibliography.Utils;
import org.projectsforge.xwiki.bibliography.biblatex.BibLaTeXExporter;
import org.projectsforge.xwiki.bibliography.fields.CSLCategoriesFields;
import org.projectsforge.xwiki.bibliography.fields.CSLDateFields;
import org.projectsforge.xwiki.bibliography.fields.CSLNameFields;
import org.projectsforge.xwiki.bibliography.fields.CSLStringFields;
import org.projectsforge.xwiki.bibliography.fields.CSLTypeFields;
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

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ListItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.output.Bibliography;

/**
 * The Class Entry.
 */
public class Entry {

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

  /** The Constant FIELD_BIBLATEX. */
  private static final String FIELD_BIBLATEX = "biblatex";

  /**
   * Gets the class reference.
   *
   * @param entityReference
   *          the entity reference
   * @return the class reference
   */
  public static DocumentReference getClassReference(EntityReference entityReference) {
    return new DocumentReference(entityReference.extractReference(EntityType.WIKI).getName(),
        Constants.CODE_SPACE_NAME_AS_LIST, "EntryClass");
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
    return Constants.CODE_SPACE_NAME_AS_STRING + ".EntryClass";
  }

  /** The document. */
  private XWikiDocument document;

  /** The service. */
  private BibliographyService service;

  /** The xobject. */
  private BaseObject xobject;

  /**
   * Instantiates a new bibliography entry.
   *
   * @param service
   *          the service
   * @param document
   *          the document
   */
  public Entry(BibliographyService service, XWikiDocument document) {
    this.service = service;
    this.document = document;
    this.xobject = document.getXObject(getClassReference(document), true, service.getContext());
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
      field.fillFromCSLObject(service, xobject, itemData);
    }

    for (CSLStringFields field : CSLStringFields.values()) {
      field.fillFromCSLObject(service, xobject, itemData);
    }

    for (CSLNameFields field : CSLNameFields.values()) {
      field.fillFromCSLObject(service, xobject, itemData, authorReference);
    }

    for (CSLDateFields field : CSLDateFields.values()) {
      field.fillFromCSLObject(service, xobject, itemData);
    }

    for (CSLCategoriesFields field : CSLCategoriesFields.values()) {
      field.fillFromCSLObject(service, xobject, itemData);
    }
  }

  /**
   * Gets the CSL item data.
   *
   * @return the CSL item data
   */
  public CSLItemData getCSLItemData() {
    return Utils.deserializeCSLItemData(service, xobject.getLargeStringValue(FIELD_CSL_ITEM_DATA));
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
   * Save.
   *
   * @param authorReference
   *          the author reference
   */
  public void save(DocumentReference authorReference) {
    XWikiContext context = service.getContext();
    DocumentReference oldUser = context.getUserReference();
    context.setUserReference(authorReference);
    try {
      try {
        context.getWiki().saveDocument(document, context);
      } catch (XWikiException ex) {
        logger.warn("Failed saving document", ex);
        service.addError(Error.SAVE_DOCUMENT, document.getDocumentReference());
      }
    } finally {
      context.setUserReference(oldUser);
    }
  }

  /**
   * Update some fields (triggered when the document is saved through events).
   */
  public void update() {
    try {
      CSLItemDataBuilder builder = new CSLItemDataBuilder();

      for (CSLTypeFields field : CSLTypeFields.values()) {
        field.fillFromXObject(service, builder, xobject);
      }

      for (CSLStringFields field : CSLStringFields.values()) {
        field.fillFromXObject(service, builder, xobject);
      }

      for (CSLNameFields field : CSLNameFields.values()) {
        field.fillFromXObject(service, builder, xobject);
      }

      for (CSLDateFields field : CSLDateFields.values()) {
        field.fillFromXObject(service, builder, xobject);
      }

      for (CSLCategoriesFields field : CSLCategoriesFields.values()) {
        field.fillFromXObject(service, builder, xobject);
      }

      CSLItemData itemData = builder.build();
      xobject.setLargeStringValue(FIELD_CSL_ITEM_DATA, Utils.serializeCSLItemData(itemData));

      CSL csl = new CSL(new ListItemDataProvider(itemData),
          service.getDefaultConfiguration(document.getDocumentReference().getWikiReference())
              .getBibliographyStyle(Configuration.FIELD_BIBLIOGRAPHY_ENTRY_STYLE));
      csl.registerCitationItems(itemData.getId());
      csl.setOutputFormat("text");
      Bibliography bibiography = csl.makeBibliography();
      String rendered = bibiography.getEntries()[0].trim();
      rendered = rendered.replaceAll(Constants.ENTRY_TARGET_MARK, document.getDocumentReference().toString());

      document.setTitle(itemData.getId());
      xobject.setLargeStringValue(FIELD_RENDERED, rendered);
      xobject.setLargeStringValue(FIELD_BIBLATEX, BibLaTeXExporter.export(itemData));
    } catch (IOException ex) {
      service.addError(Error.CSL, document.getDocumentReference(), xobject, ex.getMessage());
      logger.warn("Can not format title", ex);
    } catch (Exception ex) {
      service.addError(Error.BUILD_CSLDATAITEM, document.getDocumentReference(), xobject, ex.getMessage());
      logger.warn("An error occurred", ex);
    }
  }

}
