package org.projectsforge.xwiki.bibliography.biblatex;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.bibliography.fields.CSLDateFields;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;

import de.undercouch.citeproc.bibtex.DateParser;
import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;

/**
 * The Class DateThenYearMonthFieldProvider.
 */
public class DateThenYearMonthFieldProvider extends FieldProvider<CSLDateFields> {

  /**
   * Instantiates a new date then year month field provider.
   *
   * @param cslField
   *          the csl field
   * @param bibFields
   *          the bib fields
   */
  public DateThenYearMonthFieldProvider(CSLDateFields cslField, String... bibFields) {
    super(cslField, bibFields);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.projectsforge.xwiki.bibliography.FieldProvider#convert(org.
   * projectsforge.xwiki.bibliography.BibliographyService,
   * de.undercouch.citeproc.csl.CSLItemDataBuilder, java.util.Map)
   */
  @Override
  public void convert(BibliographyService service, CSLItemDataBuilder builder, Map<String, String> entries) {
    String value = mergeFields(entries);
    CSLDate date;

    // TODO handle day

    if (StringUtils.isNotBlank(value)) {
      date = DateParser.toDate(value);
    } else {
      String year = entries.get("year");
      String month = StringUtils.defaultIfBlank(entries.get("month"), null);
      date = DateParser.toDate(year, month);
    }

    if (date != null) {
      getCslField().set(builder, date);
    }
  }

}
