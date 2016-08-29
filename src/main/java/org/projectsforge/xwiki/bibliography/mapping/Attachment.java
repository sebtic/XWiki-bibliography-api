package org.projectsforge.xwiki.bibliography.mapping;

import org.projectsforge.xwiki.bibliography.Constants;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * The Class Annotation.
 */
public class Attachment extends RestrictedAccessClass {

  /**
   * Gets the class reference.
   *
   * @param entityReference
   *          the entity reference
   * @return the class reference
   */
  public static DocumentReference getClassReference(EntityReference entityReference) {
    return new DocumentReference(entityReference.extractReference(EntityType.WIKI).getName(),
        Constants.CODE_SPACE_NAME_AS_LIST, "AttachmentClass");
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
    return Constants.CODE_SPACE_NAME_AS_STRING + ".AttachmentClass";
  }

  /**
   * Instantiates a new annotation.
   *
   * @param service
   *          the service
   * @param document
   *          the document
   */
  public Attachment(BibliographyService service, XWikiDocument document) {
    super(service, document, document.getXObject(getClassReference(document), true, service.getContext()));
  }
}
