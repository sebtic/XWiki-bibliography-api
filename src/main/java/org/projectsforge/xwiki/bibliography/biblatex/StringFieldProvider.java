package org.projectsforge.xwiki.bibliography.biblatex;

import java.util.Map;

import org.projectsforge.xwiki.bibliography.fields.CSLStringFields;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;

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
   * @see org.projectsforge.xwiki.bibliography.FieldProvider#convert(org.
   * projectsforge.xwiki.bibliography.BibliographyService,
   * de.undercouch.citeproc.csl.CSLItemDataBuilder, java.util.Map)
   */
  @Override
  public void convert(BibliographyService service, CSLItemDataBuilder builder, Map<String, String> entries) {
    String value = mergeFields(entries);
    if (value == null) {
      return;
    }
    getCslField().set(builder, value);
  }

}
