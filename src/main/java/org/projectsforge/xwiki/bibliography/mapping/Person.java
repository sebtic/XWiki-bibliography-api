package org.projectsforge.xwiki.bibliography.mapping;

import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.bibliography.Constants;
import org.projectsforge.xwiki.bibliography.Error;
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

import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.CSLNameBuilder;

/**
 * The Class Person.
 */
public class Person {
  /** The Constant FIELD_FAMILY. */
  public static final String FIELD_FAMILY = "family";

  /** The Constant FIELD_GIVEN. */
  public static final String FIELD_GIVEN = "given";

  /** The Constant FIELD_DROPPING_PARTICLE. */
  public static final String FIELD_DROPPING_PARTICLE = "droppingParticle";

  /** The Constant FIELD_NON_DROPPING_PARTICLE. */
  public static final String FIELD_NON_DROPPING_PARTICLE = "nonDroppingParticle";

  /** The Constant FIELD_SUFFIX. */
  public static final String FIELD_SUFFIX = "suffix";

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(Person.class);

  /** The Constant NAME_PREFIX. */
  public static final String NAME_PREFIX = Constants.PERSONS_SPACE_NAME_AS_STRING + ".Person-";

  /** The Constant NAME_SUFFIX. */
  public static final String NAME_SUFFIX = ".WebHome";

  /** The Constant FIELD_RENDERED_GIVEN_FIRST. */
  private static final String FIELD_RENDERED_GIVEN_FIRST = "renderedGivenFirst";

  /** The Constant FIELD_RENDERED_FAMILY_FIRST. */
  private static final String FIELD_RENDERED_FAMILY_FIRST = "renderedFamilyFirst";

  /**
   * Gets the class reference.
   *
   * @param entityReference
   *          the entity reference
   * @return the class reference
   */
  public static DocumentReference getClassReference(EntityReference entityReference) {
    return new DocumentReference(entityReference.extractReference(EntityType.WIKI).getName(),
        Constants.CODE_SPACE_NAME_AS_LIST, "PersonClass");
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
    return Constants.CODE_SPACE_NAME_AS_STRING + ".PersonClass";
  }

  /** The xobject. */
  private BaseObject xobject;

  /** The document. */
  private XWikiDocument document;

  /** The service. */
  private BibliographyService service;

  /**
   * Instantiates a new person.
   *
   * @param service
   *          the service
   * @param document
   *          the document
   */
  public Person(BibliographyService service, XWikiDocument document) {
    this.service = service;
    this.document = document;
    this.xobject = document.getXObject(getClassReference(document), true, service.getContext());
  }

  /**
   * Fill from CSL.
   *
   * @param name
   *          the name
   */
  public void fillFromCSLObject(CSLName name) {
    xobject.setStringValue(FIELD_FAMILY, StringUtils.defaultString(name.getFamily()));
    xobject.setStringValue(FIELD_GIVEN, StringUtils.defaultString(name.getGiven()));
    xobject.setStringValue(FIELD_DROPPING_PARTICLE, StringUtils.defaultString(name.getDroppingParticle()));
    xobject.setStringValue(FIELD_NON_DROPPING_PARTICLE, StringUtils.defaultString(name.getNonDroppingParticle()));
    xobject.setStringValue(FIELD_SUFFIX, StringUtils.defaultString(name.getSuffix()));
  }

  /**
   * Gets the CSL object.
   *
   * @return the CSL object
   */
  public CSLName getCSLObject() {
    String family = StringUtils.defaultString(xobject.getStringValue(FIELD_FAMILY));
    String given = StringUtils.defaultString(xobject.getStringValue(FIELD_GIVEN));
    String droppingParticle = StringUtils.defaultString(xobject.getStringValue(FIELD_DROPPING_PARTICLE));
    String nonDroppingParticle = StringUtils.defaultString(xobject.getStringValue(FIELD_NON_DROPPING_PARTICLE));
    String suffix = StringUtils.defaultString(xobject.getStringValue(FIELD_SUFFIX));

    // clear empty fields
    if (StringUtils.isBlank(family)) {
      family = null;
    }
    if (StringUtils.isBlank(given)) {
      given = null;
    }
    if (StringUtils.isBlank(droppingParticle)) {
      droppingParticle = null;
    }
    if (StringUtils.isBlank(nonDroppingParticle)) {
      nonDroppingParticle = null;
    }
    if (StringUtils.isBlank(suffix)) {
      suffix = null;
    }

    CSLName name = new CSLNameBuilder().family(family).given(given).droppingParticle(droppingParticle)
        .nonDroppingParticle(nonDroppingParticle).suffix(suffix).build();

    return name;
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
        document.setAuthorReference(authorReference);
        document.setContentAuthorReference(authorReference);
        context.getWiki().saveDocument(document, context);
      } catch (XWikiException ex) {
        service.addError(Error.SAVE_DOCUMENT, document.getDocumentReference());
        logger.warn("Failed saving document", ex);
      }
    } finally {
      context.setUserReference(oldUser);
    }
  }

  /**
   * Update some fields (triggered when the document is saved through events).
   */
  public void update() {
    String family = StringUtils.defaultString(xobject.getStringValue(FIELD_FAMILY));
    String given = StringUtils.defaultString(xobject.getStringValue(FIELD_GIVEN));
    String droppingParticle = StringUtils.defaultString(xobject.getStringValue(FIELD_DROPPING_PARTICLE));
    String nonDroppingParticle = StringUtils.defaultString(xobject.getStringValue(FIELD_NON_DROPPING_PARTICLE));
    String suffix = StringUtils.defaultString(xobject.getStringValue(FIELD_SUFFIX));

    StringBuilder renderedFamilyFirstBuilder = new StringBuilder("");

    if (StringUtils.isNotBlank(nonDroppingParticle)) {
      renderedFamilyFirstBuilder.append(nonDroppingParticle).append(" ");
    }
    if (StringUtils.isNotBlank(family)) {
      renderedFamilyFirstBuilder.append(family);
    }

    if (StringUtils.isNotBlank(given)) {
      renderedFamilyFirstBuilder.append(", ").append(given);
    }

    if (StringUtils.isNotBlank(suffix)) {
      renderedFamilyFirstBuilder.append(", ").append(suffix);
    }

    String renderedFamilyFirst = renderedFamilyFirstBuilder.toString().trim();

    document.setTitle(StringUtils.substring(renderedFamilyFirst, 0, 250));
    xobject.setLargeStringValue(FIELD_RENDERED_FAMILY_FIRST, renderedFamilyFirst);

    StringJoiner renderedGivenFirstBuilder = new StringJoiner(" ");
    if (StringUtils.isNotBlank(given)) {
      renderedGivenFirstBuilder.add(given);
    }

    if (StringUtils.isNotBlank(droppingParticle)) {
      renderedGivenFirstBuilder.add(droppingParticle);
    }

    if (StringUtils.isNotBlank(nonDroppingParticle)) {
      renderedGivenFirstBuilder.add(nonDroppingParticle);
    }

    if (StringUtils.isNotBlank(family)) {
      renderedGivenFirstBuilder.add(family);
    }

    if (StringUtils.isNotBlank(suffix)) {
      renderedGivenFirstBuilder.add(suffix);
    }

    xobject.setLargeStringValue(FIELD_RENDERED_GIVEN_FIRST, renderedGivenFirstBuilder.toString().trim());
  }

}
