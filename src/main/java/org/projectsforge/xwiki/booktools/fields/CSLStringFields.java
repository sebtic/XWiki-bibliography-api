package org.projectsforge.xwiki.booktools.fields;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.service.BookToolsService;

import com.xpn.xwiki.objects.BaseObject;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;

/**
 * The Enum CSLStringFields.
 */
public enum CSLStringFields {

  /** The abstract. */
  ABSTRACT("abstract", true, (b, v) -> b.abstrct(v), i -> i.getAbstrct()),
  /** The archive. */
  ARCHIVE("archive", (b, v) -> b.archive(v), i -> i.getArchive()),
  /** The archive location. */
  ARCHIVE_LOCATION("archiveLocation", (b, v) -> b.archiveLocation(v), i -> i.getArchiveLocation()),
  /** The archive place. */
  ARCHIVE_PLACE("archivePlace", (b, v) -> b.archivePlace(v), i -> i.getArchivePlace()),
  /** The authority. */
  AUTHORITY("authority", (b, v) -> b.authority(v), i -> i.getAuthority()),
  /** The call number. */
  CALL_NUMBER("callNumber", (b, v) -> b.callNumber(v), i -> i.getCallNumber()),
  /** The chapter number. */
  CHAPTER_NUMBER("chapterNumber", (b, v) -> b.chapterNumber(v), i -> i.getChapterNumber()),
  /** The collection number. */
  COLLECTION_NUMBER("collectionNumber", (b, v) -> b.collectionNumber(v), i -> i.getCollectionNumber()),
  /** The collection title. */
  COLLECTION_TITLE("collectionTitle", (b, v) -> b.collectionTitle(v), i -> i.getCollectionTitle()),
  /** The container title. */
  CONTAINER_TITLE("containerTitle", (b, v) -> b.containerTitle(v), i -> i.getContainerTitle()),
  /** The container title short. */
  CONTAINER_TITLE_SHORT("containerTitleShort", (b, v) -> b.containerTitleShort(v), i -> i.getContainerTitleShort()),
  /** The dimensions. */
  DIMENSIONS("dimensions", (b, v) -> b.dimensions(v), i -> i.getDimensions()),
  /** The doi. */
  DOI("DOI", (b, v) -> b.DOI(v), i -> i.getDOI()),
  /** The edition. */
  EDITION("edition", (b, v) -> b.edition(v), i -> i.getEdition()),
  /** The event. */
  EVENT("event", (b, v) -> b.event(v), i -> i.getEvent()),
  /** The event place. */
  EVENT_PLACE("eventPlace", (b, v) -> b.eventplace(v), i -> i.getEventplace()),
  /** The genre. */
  GENRE("genre", (b, v) -> b.genre(v), i -> i.getGenre()),
  /** The id. */
  ID("id", (b, v) -> b.id(v), i -> i.getId()),
  /** The isbn. */
  ISBN("ISBN", (b, v) -> b.ISBN(v), i -> i.getISBN()),
  /** The issn. */
  ISSN("ISSN", (b, v) -> b.ISSN(v), i -> i.getISSN()),
  /** The issue. */
  ISSUE("issue", (b, v) -> b.issue(v), i -> i.getIssue()),
  /** The journal abbreviation. */
  JOURNAL_ABBREVIATION("journalAbbreviation", (b, v) -> b.journalAbbreviation(v), i -> i.getJournalAbbreviation()),
  /** The jurisdiction. */
  JURISDICTION("jurisdiction", (b, v) -> b.jurisdiction(v), i -> i.getJurisdiction()),
  /** The language. */
  LANGUAGE("language", (b, v) -> b.language(v), i -> i.getLanguage()),
  /** The medium. */
  MEDIUM("medium", (b, v) -> b.medium(v), i -> i.getMedium()),
  /** The note. */
  NOTE("note", (b, v) -> b.note(v), i -> i.getNote()),
  /** The number. */
  NUMBER("number", (b, v) -> b.number(v), i -> i.getNumber()),
  /** The number of pages. */
  NUMBER_OF_PAGES("numberOfPages", (b, v) -> b.numberOfPages(v), i -> i.getNumberOfPages()),
  /** The number of volumes. */
  NUMBER_OF_VOLUMES("numberOfVolumes", (b, v) -> b.numberOfVolumes(v), i -> i.getNumberOfVolumes()),
  /** The page. */
  PAGE("page", (b, v) -> b.page(v), i -> i.getPage()),
  /** The publisher. */
  PUBLISHER("publisher", (b, v) -> b.publisher(v), i -> i.getPublisher()),
  /** The publisher place. */
  PUBLISHER_PLACE("publisherPlace", (b, v) -> b.publisherPlace(v), i -> i.getPublisherPlace()),
  /** The references. */
  REFERENCES("references", true, (b, v) -> b.references(v), i -> i.getReferences()),
  /** The reviewed title. */
  REVIEWED_TITLE("reviewedTitle", (b, v) -> b.reviewedTitle(v), i -> i.getReviewedTitle()),
  /** The scale. */
  SCALE("scale", (b, v) -> b.scale(v), i -> i.getScale()),
  /** The section. */
  SECTION("section", (b, v) -> b.section(v), i -> i.getSection()),
  /** The short title. */
  SHORT_TITLE("shortTitle", (b, v) -> b.shortTitle(v), i -> i.getShortTitle()),
  /** The source. */
  SOURCE("source", (b, v) -> b.source(v), i -> i.getSource()),
  /** The status. */
  STATUS("status", (b, v) -> b.status(v), i -> i.getStatus()),
  /** The title. */
  TITLE("title", (b, v) -> b.title(v), i -> i.getTitle()),
  /** The title short. */
  TITLE_SHORT("titleShort", (b, v) -> b.titleShort(v), i -> i.getTitleShort()),
  /** The url. */
  URL("URL", true, (b, v) -> b.URL(v), i -> i.getURL()),
  /** The version. */
  VERSION("version", (b, v) -> b.version(v), i -> i.getVersion()),
  /** The volume. */
  VOLUME("volume", (b, v) -> b.volume(v), i -> i.getVolume());

  /**
   * From string.
   *
   * @param name
   *          the name
   * @return the enum value
   */
  public static CSLStringFields fromString(String name) {
    for (CSLStringFields field : values()) {
      if (field.name.equalsIgnoreCase(name)) {
        return field;
      }
    }
    return null;
  }

  /** The getter. */
  private Function<CSLItemData, String> getter;

  /** The large. */
  private boolean large;

  /** The name. */
  private String name;

  /** The setter. */
  private BiConsumer<CSLItemDataBuilder, String> setter;

  /**
   * Instantiates a new CSL string fields.
   *
   * @param value
   *          the value
   * @param setter
   *          the setter
   * @param getter
   *          the getter
   */
  CSLStringFields(String value, BiConsumer<CSLItemDataBuilder, String> setter, Function<CSLItemData, String> getter) {
    this(value, false, setter, getter);
  }

  /**
   * Instantiates a new CSL string fields.
   *
   * @param value
   *          the value
   * @param large
   *          the large
   * @param setter
   *          the setter
   * @param getter
   *          the getter
   */
  CSLStringFields(String value, boolean large, BiConsumer<CSLItemDataBuilder, String> setter,
      Function<CSLItemData, String> getter) {
    this.name = value;
    this.setter = setter;
    this.getter = getter;
    this.large = large;
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
  public void fillFromCSLObject(BookToolsService service, BaseObject xobject, CSLItemData itemData) {
    if (isLarge()) {
      xobject.setLargeStringValue(toString(), StringUtils.defaultString(get(itemData)));
    } else {
      xobject.setStringValue(toString(), StringUtils.defaultString(get(itemData)));
    }
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
  public void fillFromXObject(BookToolsService service, CSLItemDataBuilder builder, BaseObject xobject) {
    String value;
    if (isLarge()) {
      value = StringUtils.defaultIfBlank(xobject.getLargeStringValue(toString()), null);
    } else {
      value = StringUtils.defaultIfBlank(xobject.getStringValue(toString()), null);
    }
    if (!(this == CSLStringFields.ID && value == null)) {
      set(builder, value);
    }

  }

  /**
   * Gets the.
   *
   * @param itemData
   *          the item data
   * @return the string
   */
  public String get(CSLItemData itemData) {
    return getter.apply(itemData);
  }

  /**
   * Checks if is large.
   *
   * @return true, if is large
   */
  public boolean isLarge() {
    return large;
  }

  /**
   * Sets the.
   *
   * @param builder
   *          the builder
   * @param value
   *          the value
   */
  public void set(CSLItemDataBuilder builder, String value) {
    setter.accept(builder, value);
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
