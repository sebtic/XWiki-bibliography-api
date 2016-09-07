package org.projectsforge.xwiki.booktools.biblatex;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.service.BookToolsService;

import de.undercouch.citeproc.csl.CSLItemDataBuilder;

/**
 * The Class FieldProvider.
 *
 * @param <T>
 *          the generic type
 */
@SuppressWarnings("rawtypes")
public abstract class FieldProvider<T extends Enum> {

  /** The bib fields. */
  private String[] bibFields;

  /** The csl field. */
  private T cslField;

  /**
   * Instantiates a new field provider.
   *
   * @param cslField
   *          the csl field
   * @param bibFields
   *          the bib fields
   */
  public FieldProvider(T cslField, String... bibFields) {
    this.cslField = cslField;
    this.bibFields = bibFields;
  }

  /**
   * Convert.
   *
   * @param service
   *          the service
   * @param builder
   *          the builder
   * @param entries
   *          the entries
   */
  public abstract void convert(BookToolsService service, CSLItemDataBuilder builder, Map<String, String> entries);

  /**
   * Gets the bib fields.
   *
   * @return the bib fields
   */
  public String[] getBibFields() {
    return bibFields;
  }

  /**
   * Gets the csl field.
   *
   * @return the csl field
   */
  public T getCslField() {
    return cslField;
  }

  /**
   * Merge fields.
   *
   * @param entries
   *          the entries
   * @return the string
   */
  protected String mergeFields(Map<String, String> entries) {
    for (String field : bibFields) {
      String value = entries.get(field);
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
    }
    return null;
  }
}
