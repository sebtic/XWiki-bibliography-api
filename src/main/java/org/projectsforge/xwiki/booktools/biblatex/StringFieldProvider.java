package org.projectsforge.xwiki.booktools.biblatex;

import java.util.Map;

import org.projectsforge.xwiki.booktools.fields.CSLStringFields;
import org.projectsforge.xwiki.booktools.service.BookToolsService;

import de.undercouch.citeproc.csl.CSLItemDataBuilder;

/**
 * The Class StringFieldProvider.
 */
public class StringFieldProvider extends FieldProvider<CSLStringFields> {

  /**
   * Instantiates a new string field provider.
   *
   * @param cslField
   *          the csl field
   * @param bibFields
   *          the bib fields
   */
  public StringFieldProvider(CSLStringFields cslField, String... bibFields) {
    super(cslField, bibFields);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.FieldProvider#convert(org.
   * projectsforge.xwiki.booktools.BookToolsService,
   * de.undercouch.citeproc.csl.CSLItemDataBuilder, java.util.Map)
   */
  @Override
  public void convert(BookToolsService service, CSLItemDataBuilder builder, Map<String, String> entries) {
    String value = mergeFields(entries);
    if (value == null) {
      return;
    }
    getCslField().set(builder, value);
  }

}
