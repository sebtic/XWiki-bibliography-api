package org.projectsforge.xwiki.booktools.fields;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.projectsforge.xwiki.booktools.service.BookToolsService;

import com.xpn.xwiki.objects.BaseObject;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;

/**
 * The Enum CSLCategoriesFields.
 */
public enum CSLCategoriesFields {

  /** The categories. */
  CATEGORIES("categories", (b, v) -> b.categories(v), i -> i.getCategories());

  /**
   * From string.
   *
   * @param name
   *          the name
   * @return the enum value
   */
  public static CSLCategoriesFields fromString(String name) {
    for (CSLCategoriesFields field : values()) {
      if (field.name.equalsIgnoreCase(name)) {
        return field;
      }
    }
    return null;
  }

  /** The getter. */
  private Function<CSLItemData, String[]> getter;

  /** The name. */
  private String name;

  /** The setter. */
  private BiConsumer<CSLItemDataBuilder, String[]> setter;

  /**
   * Instantiates a new CSL categories fields.
   *
   * @param name
   *          the name
   * @param setter
   *          the setter
   * @param getter
   *          the getter
   */
  CSLCategoriesFields(String name, BiConsumer<CSLItemDataBuilder, String[]> setter,
      Function<CSLItemData, String[]> getter) {
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
    String[] values = get(itemData);
    if (values == null) {
      values = new String[0];
    }
    xobject.setStringListValue(toString(), Arrays.asList(values));
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
    @SuppressWarnings("unchecked")
    String[] values = ((List<String>) xobject.getListValue(toString())).toArray(new String[0]);
    set(builder, values);
  }

  /**
   * Gets the.
   *
   * @param itemData
   *          the item data
   * @return the string[]
   */
  public String[] get(CSLItemData itemData) {
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
  public void set(CSLItemDataBuilder builder, String[] values) {
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
