package org.projectsforge.xwiki.booktools.mapping;

import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.Constants;
import org.projectsforge.xwiki.booktools.mapping.DocumentWalker.Node;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.objects.BaseObject;

import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.CSLNameBuilder;

/**
 * The Class Person.
 */
public class Person {
  /** The Constant CLASS_REFERENCE. */
  public static final EntityReference CLASS_REFERENCE = new EntityReference("PersonClass", EntityType.DOCUMENT,
      Constants.CODE_SPACE_REFERENCE);

  /** The Constant CLASS_REFERENCE_AS_STRING. */
  public static final String CLASS_REFERENCE_AS_STRING = Constants.CODE_SPACE_NAME_AS_STRING + ".PersonClass";

  /** The Constant FIELD_DROPPING_PARTICLE. */
  public static final String FIELD_DROPPING_PARTICLE = "droppingParticle";

  /** The Constant FIELD_FAMILY. */
  public static final String FIELD_FAMILY = "family";

  /** The Constant FIELD_GIVEN. */
  public static final String FIELD_GIVEN = "given";

  /** The Constant FIELD_NON_DROPPING_PARTICLE. */
  public static final String FIELD_NON_DROPPING_PARTICLE = "nonDroppingParticle";

  /** The Constant FIELD_RENDERED_FAMILY_FIRST. */
  private static final String FIELD_RENDERED_FAMILY_FIRST = "renderedFamilyFirst";

  /** The Constant FIELD_RENDERED_GIVEN_FIRST. */
  private static final String FIELD_RENDERED_GIVEN_FIRST = "renderedGivenFirst";

  /** The Constant FIELD_SUFFIX. */
  public static final String FIELD_SUFFIX = "suffix";

  /** The Constant NAME_PREFIX. */
  public static final String NAME_PREFIX = Constants.PERSONS_SPACE_NAME_AS_STRING + ".Person-";

  /** The Constant NAME_SUFFIX. */
  public static final String NAME_SUFFIX = ".WebHome";

  /** The node. */
  private Node node;

  /** The xobject. */
  private BaseObject xobject;

  /**
   * Instantiates a new person.
   *
   * @param node
   *          the node
   */
  public Person(Node node) {
    this.node = node;
    this.xobject = node.getXObject(CLASS_REFERENCE, true);
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

    return new CSLNameBuilder().family(family).given(given).droppingParticle(droppingParticle)
        .nonDroppingParticle(nonDroppingParticle).suffix(suffix).build();
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

    node.getXWikiDocument().setTitle(StringUtils.substring(renderedFamilyFirst, 0, 250));
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
