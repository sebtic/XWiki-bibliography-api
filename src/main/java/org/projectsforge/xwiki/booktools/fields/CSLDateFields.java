package org.projectsforge.xwiki.booktools.fields;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.Utils;
import org.projectsforge.xwiki.booktools.service.BookToolsService;

import com.xpn.xwiki.objects.BaseObject;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;

/**
 * The Enum CSLDateFields.
 */
public enum CSLDateFields {

  /** The accessed. */
  ACCESSED("accessed", (b, v) -> b.accessed(v), i -> i.getAccessed(), CSLDateFields::isValidSingleOrEmpty),
  /** The event date. */
  EVENT_DATE("eventDate", (b, v) -> b.eventDate(v), i -> i.getEventDate(), CSLDateFields::isValidSingleOrRangeOrEmpty),
  /** The issued. */
  ISSUED("issued", (b, v) -> b.issued(v), i -> i.getIssued(), CSLDateFields::isValidSingleOrEmpty),
  /** The original date. */
  ORIGINAL_DATE("originalDate", (b, v) -> b.originalDate(v), i -> i.getOriginalDate(), CSLDateFields::isValidSingleOrEmpty),
  /** The submitted. */
  SUBMITTED("submitted", (b, v) -> b.submitted(v), i -> i.getSubmitted(), CSLDateFields::isValidSingleOrEmpty)

  ;

  /**
   * From string.
   *
   * @param name
   *          the name
   * @return the enum value
   */
  public static CSLDateFields fromString(String name) {
    final String value;
    int index = name.indexOf('[');
    if (index != -1) {
      value = name.substring(0, index);
    } else {
      value = name;
    }
    for (CSLDateFields field : values()) {
      if (field.name.equalsIgnoreCase(value)) {
        return field;
      }
    }
    return null;
  }

  /**
   * Checks if it is a valid single date or if it is empty.
   *
   * @param value
   *          the value
   * @return true, if is valid single or empty
   */
  private static boolean isValidSingleOrEmpty(String value) {
    String regex = "^(((19|20)[0-9][0-9])(-(0?[1-9]|1[012]))?(-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]))?)?$";
    String trimmed = StringUtils.trimToEmpty(value);
    return StringUtils.isBlank(trimmed) || Pattern.matches(regex, trimmed);
  }

  /**
   * Checks if it is a valid single date or a range or if it is empty.
   *
   * @param value
   *          the value
   * @return true, if is valid single or range or empty
   */
  private static boolean isValidSingleOrRangeOrEmpty(String value) {
    String regex = "^(((19|20)[0-9][0-9])(-(0?[1-9]|1[012]))?(-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]))?(/((19|20)[0-9][0-9])(-(0?[1-9]|1[012]))?(-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]))?)?)?$";
    String trimmed = StringUtils.trimToEmpty(value);
    return StringUtils.isBlank(trimmed) || Pattern.matches(regex, trimmed);
  }

  /** The getter. */
  private Function<CSLItemData, CSLDate> getter;

  /** The value. */
  private String name;

  /** The setter. */
  private BiConsumer<CSLItemDataBuilder, CSLDate> setter;

  /** The validator. */
  private Function<String, Boolean> validator;

  /**
   * Instantiates a new CSL date fields.
   *
   * @param name
   *          the name
   * @param setter
   *          the setter
   * @param getter
   *          the getter
   * @param validator
   *          the validator
   */
  CSLDateFields(String name, BiConsumer<CSLItemDataBuilder, CSLDate> setter, Function<CSLItemData, CSLDate> getter,
      Function<String, Boolean> validator) {
    this.name = name;
    this.setter = setter;
    this.getter = getter;
    this.validator = validator;
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
    xobject.setStringValue(toString(), StringUtils.defaultString(Utils.convertCSLDateToString(get(itemData))));
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
    CSLDate value = Utils.convertValueToCSLDate(xobject.getStringValue(toString()));
    set(builder, value);
  }

  /**
   * Gets the date in the CSLItemData.
   *
   * @param itemData
   *          the item data
   * @return the CSL date
   */
  public CSLDate get(CSLItemData itemData) {
    return getter.apply(itemData);
  }

  /**
   * Checks if the date is valid.
   *
   * @param value
   *          the value
   * @return true, if is valid
   */
  public boolean isValid(String value) {
    return validator.apply(value);
  }

  /**
   * Sets the value in the CSLItemData.
   *
   * @param builder
   *          the builder
   * @param value
   *          the value
   */
  public void set(CSLItemDataBuilder builder, CSLDate value) {
    setter.accept(builder, value);
  }

  /**
   * To string.
   *
   * @return the string
   */
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
