package org.projectsforge.xwiki.booktools.fields;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.projectsforge.xwiki.booktools.Error;
import org.projectsforge.xwiki.booktools.service.BookToolsService;

import com.xpn.xwiki.objects.BaseObject;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;

/**
 * The Enum CSLTypeFields.
 */
public enum CSLTypeFields {

  /** The type. */
  TYPE("type", (b, v) -> b.type(v), i -> i.getType());

  /**
   * From string.
   *
   * @param name
   *          the name
   * @return the enum value
   */
  public static CSLTypeFields fromString(String name) {
    for (CSLTypeFields field : values()) {
      if (field.name.equalsIgnoreCase(name)) {
        return field;
      }
    }
    return null;
  }

  /** The getter. */
  private Function<CSLItemData, CSLType> getter;

  /** The name. */
  private String name;

  /** The setter. */
  private BiConsumer<CSLItemDataBuilder, CSLType> setter;

  /**
   * Instantiates a new CSL type fields.
   *
   * @param name
   *          the name
   * @param setter
   *          the setter
   * @param getter
   *          the getter
   */
  CSLTypeFields(String name, BiConsumer<CSLItemDataBuilder, CSLType> setter, Function<CSLItemData, CSLType> getter) {
    this.name = name;
    this.setter = setter;
    this.getter = getter;
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
    xobject.setStringValue(toString(), get(itemData).toString());
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
    try {
      CSLType value = CSLType.fromString(xobject.getStringValue(toString()));
      set(builder, value);
    } catch (Exception ex) {
      service.getLogger().warn("An error occurred", ex);
      service.addError(Error.CSLTYPE_FROM_STRING, xobject.getStringValue(toString()));
    }
  }

  /**
   * Gets the.
   *
   * @param itemData
   *          the item data
   * @return the CSL type
   */
  public CSLType get(CSLItemData itemData) {
    return getter.apply(itemData);
  }

  /**
   * Sets the.
   *
   * @param builder
   *          the builder
   * @param value
   *          the value
   */
  public void set(CSLItemDataBuilder builder, CSLType value) {
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
