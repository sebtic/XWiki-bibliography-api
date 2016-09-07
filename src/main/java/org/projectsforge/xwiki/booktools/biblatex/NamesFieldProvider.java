package org.projectsforge.xwiki.booktools.biblatex;

import java.util.Map;

import org.projectsforge.xwiki.booktools.fields.CSLNameFields;
import org.projectsforge.xwiki.booktools.service.BookToolsService;

import de.undercouch.citeproc.bibtex.NameParser;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;

/**
 * The Class NamesFieldProvider.
 */
public class NamesFieldProvider extends FieldProvider<CSLNameFields> {

  /**
   * Instantiates a new names field provider.
   *
   * @param cslField
   *          the csl field
   * @param bibFields
   *          the bib fields
   */
  public NamesFieldProvider(CSLNameFields cslField, String... bibFields) {
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
    getCslField().set(builder, NameParser.parse(value));
  }

}
