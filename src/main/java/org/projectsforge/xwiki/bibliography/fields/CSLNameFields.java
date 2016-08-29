package org.projectsforge.xwiki.bibliography.fields;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.projectsforge.xwiki.bibliography.Error;
import org.projectsforge.xwiki.bibliography.Utils;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.objects.BaseObject;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLName;

/**
 * The Enum CSLNameFields.
 */
public enum CSLNameFields {

  /** The authors. */
  AUTHORS("authors", (b, v) -> b.author(v), i -> i.getAuthor()),
  /** The collection editors. */
  COLLECTION_EDITORS("collectionEditors", (b, v) -> b.collectionEditor(v), i -> i.getCollectionEditor()),
  /** The composers. */
  COMPOSERS("composers", (b, v) -> b.composer(v), i -> i.getComposer()),
  /** The container authors. */
  CONTAINER_AUTHORS("containerAuthors", (b, v) -> b.containerAuthor(v), i -> i.getContainerAuthor()),
  /** The directors. */
  DIRECTORS("directors", (b, v) -> b.director(v), i -> i.getDirector()),
  /** The editorial directors. */
  EDITORIAL_DIRECTORS("editorialDirectors", (b, v) -> b.editorialDirector(v), i -> i.getEditorialDirector()),
  /** The editors. */
  EDITORS("editors", (b, v) -> b.editor(v), i -> i.getEditor()),
  /** The illustrator. */
  ILLUSTRATORS("illustrators", (b, v) -> b.illustrator(v), i -> i.getIllustrator()),
  /** The interviewers. */
  INTERVIEWERS("interviewers", (b, v) -> b.interviewer(v), i -> i.getInterviewer()),
  /** The recipients. */
  RECIPIENTS("recipients", (b, v) -> b.recipient(v), i -> i.getRecipient()),
  /** The reviewed authors. */
  REVIEWED_AUTHORS("reviewedAuthors", (b, v) -> b.reviewedAuthor(v), i -> i.getReviewedAuthor()),
  /** The translators. */
  TRANSLATORS("translators", (b, v) -> b.translator(v), i -> i.getTranslator());

  /**
   * From string.
   *
   * @param name
   *          the name
   * @return the enum value
   */
  public static CSLNameFields fromString(String name) {
    for (CSLNameFields field : values()) {
      if (field.name.equalsIgnoreCase(name)) {
        return field;
      }
    }
    return null;
  }

  /** The setter. */
  private BiConsumer<CSLItemDataBuilder, CSLName[]> setter;

  /** The name. */
  private String name;

  /** The getter. */
  private Function<CSLItemData, CSLName[]> getter;

  /**
   * Instantiates a new CSL name fields.
   *
   * @param name
   *          the name
   * @param setter
   *          the setter
   * @param getter
   *          the getter
   */
  CSLNameFields(String name, BiConsumer<CSLItemDataBuilder, CSLName[]> setter,
      Function<CSLItemData, CSLName[]> getter) {
    this.name = name;
    this.setter = setter;
    this.getter = getter;
  }

  /**
   * Decode.
   *
   * @param xobject
   *          the xobject
   * @return the list
   */
  public List<String> decode(BaseObject xobject) {
    String value = StringUtils.defaultIfBlank(xobject.getLargeStringValue(toString()), "");
    return Arrays.asList(value.split("\\|"));
  }

  /**
   * Encode.
   *
   * @param xobject
   *          the xobject
   * @param values
   *          the values
   */
  public void encode(BaseObject xobject, List<String> values) {
    StringJoiner joiner = new StringJoiner("|");
    values.forEach(joiner::add);
    xobject.setLargeStringValue(toString(), joiner.toString());
  }

  /**
   * Fill from CSL object.
   *
   * @param service
   *          the service
   * @param xobject
   *          the xobject
   * @param itemData
   *          the item data
   */
  public void fillFromCSLObject(BibliographyService service, BaseObject xobject, CSLItemData itemData) {
    CSLName[] names = get(itemData);
    if (names == null || names.length == 0) {
      xobject.setLargeStringValue(toString(), "");
      return;
    }

    StringJoiner joiner = new StringJoiner("|");
    for (CSLName value : names) {
      DocumentReference docRef = service.findPersonFromCSLName(new WikiReference(service.getContext().getWikiId()),
          value);
      if (docRef == null) {
        docRef = service.createPersonFromCSLName(value);
      }
      if (docRef != null) {
        joiner.add(Utils.LOCAL_REFERENCE_SERIALIZER.serialize(docRef));
      } else {
        service.addError(Error.GET_OR_ADD_PERSON, Utils.serializeCSLName(value));
      }
    }
    xobject.setLargeStringValue(toString(), joiner.toString());
  }

  /**
   * Fill from X object.
   *
   * @param service
   *          the service
   * @param builder
   *          the builder
   * @param xobject
   *          the xobject
   */
  public void fillFromXObject(BibliographyService service, CSLItemDataBuilder builder, BaseObject xobject) {
    CSLName[] value = Utils.convertValueToCSLNames(service, xobject.getLargeStringValue(toString()));
    set(builder, value);
  }

  /**
   * Gets the.
   *
   * @param itemData
   *          the item data
   * @return the CSL name[]
   */
  public CSLName[] get(CSLItemData itemData) {
    return getter.apply(itemData);
  }

  /**
   * Sets the.
   *
   * @param builder
   *          the builder
   * @param values
   *          the values
   */
  public void set(CSLItemDataBuilder builder, CSLName[] values) {
    setter.accept(builder, values);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return name;
  }
}
