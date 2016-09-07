package org.projectsforge.xwiki.booktools.biblatex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.BibTeXString;
import org.jbibtex.Key;
import org.jbibtex.LaTeXObject;
import org.jbibtex.LaTeXParser;
import org.jbibtex.LaTeXPrinter;
import org.jbibtex.ObjectResolutionException;
import org.jbibtex.ParseException;
import org.jbibtex.TokenMgrException;
import org.jbibtex.Value;
import org.jgroups.util.UUID;
import org.projectsforge.xwiki.booktools.Error;
import org.projectsforge.xwiki.booktools.fields.CSLDateFields;
import org.projectsforge.xwiki.booktools.fields.CSLNameFields;
import org.projectsforge.xwiki.booktools.fields.CSLStringFields;
import org.projectsforge.xwiki.booktools.service.BookToolsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;

/**
 * Convert a BibTeX/BibLaTeX database to CSL. This class is inspired by
 * de.undercouch.citeproc.bibtex.BibTeXConverter and BibLaTeX documentation.
 */
public class BibLaTeXImporter {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(BibLaTeXImporter.class);

  /** The provider abstract. */
  private static FieldProvider<?> PROVIDER_ABSTRACT = new StringFieldProvider(CSLStringFields.ABSTRACT, "abstract");

  /** The provider annote. */
  private static FieldProvider<?> PROVIDER_ANNOTE = new StringFieldProvider(CSLStringFields.STATUS, "annote");

  /** The provider author. */
  private static FieldProvider<?> PROVIDER_AUTHOR = new NamesFieldProvider(CSLNameFields.AUTHORS, "author");

  /** The provider book author. */
  private static FieldProvider<?> PROVIDER_BOOK_AUTHOR = new NamesFieldProvider(CSLNameFields.CONTAINER_AUTHORS,
      "bookauthor");

  /** The provider book title. */
  private static FieldProvider<?> PROVIDER_BOOK_TITLE = new StringFieldProvider(CSLStringFields.CONTAINER_TITLE,
      "booktitle");

  /** The provider chapter number. */
  private static FieldProvider<?> PROVIDER_CHAPTER_NUMBER = new StringFieldProvider(CSLStringFields.TITLE, "part",
      "chapter");

  /** The provider doi. */
  private static FieldProvider<?> PROVIDER_DOI = new StringFieldProvider(CSLStringFields.DOI, "doi");

  /** The provider edition. */
  private static FieldProvider<?> PROVIDER_EDITION = new StringFieldProvider(CSLStringFields.EDITION, "edition");

  /** The provider editor. */
  private static FieldProvider<?> PROVIDER_EDITOR = new NamesFieldProvider(CSLNameFields.EDITORS, "editor");

  /** The provider event. */
  private static FieldProvider<?> PROVIDER_EVENT = new StringFieldProvider(CSLStringFields.EVENT, "eventtitle",
      "booktitle");

  /** The provider event date. */
  private static FieldProvider<?> PROVIDER_EVENT_DATE = new DateFieldProvider(CSLDateFields.EVENT_DATE, "eventdate");

  /** The provider event place. */
  private static FieldProvider<?> PROVIDER_EVENT_PLACE = new StringFieldProvider(CSLStringFields.EVENT_PLACE, "venue");

  /** The provider holder. */
  private static FieldProvider<?> PROVIDER_HOLDER = new StringFieldProvider(CSLStringFields.PUBLISHER, "holder");

  /** The provider isbn. */
  private static FieldProvider<?> PROVIDER_ISBN = new StringFieldProvider(CSLStringFields.ISBN, "isbn");

  /** The provider issn. */
  private static FieldProvider<?> PROVIDER_ISSN = new StringFieldProvider(CSLStringFields.ISSN, "issn");

  /** The provider issue. */
  private static FieldProvider<?> PROVIDER_ISSUE = new StringFieldProvider(CSLStringFields.ISSUE, "issue");

  /** The provider issue title. */
  private static FieldProvider<?> PROVIDER_ISSUE_TITLE = new StringFieldProvider(CSLStringFields.CONTAINER_TITLE,
      "issuetitle", "journaltitle", "journal");

  /** The provider issued from date then year month. */
  private static FieldProvider<?> PROVIDER_ISSUED_FROM_DATE_THEN_YEAR_MONTH = new DateThenYearMonthFieldProvider(
      CSLDateFields.ISSUED, "date", "eventdate");

  /** The provider journal. */
  private static FieldProvider<?> PROVIDER_JOURNAL = new StringFieldProvider(CSLStringFields.COLLECTION_TITLE,
      "journaltitle", "journal");

  /** The provider language. */
  private static FieldProvider<?> PROVIDER_LANGUAGE = new StringFieldProvider(CSLStringFields.LANGUAGE, "language",
      "lang");

  /** The provider note. */
  private static FieldProvider<?> PROVIDER_NOTE = new StringFieldProvider(CSLStringFields.NOTE, "note");

  /** The provider number. */
  private static FieldProvider<?> PROVIDER_NUMBER = new StringFieldProvider(CSLStringFields.NUMBER, "number");

  /** The provider pages. */
  private static FieldProvider<?> PROVIDER_PAGES = new StringFieldProvider(CSLStringFields.PAGE, "pages");

  /** The provider pagetotal. */
  private static FieldProvider<?> PROVIDER_PAGETOTAL = new StringFieldProvider(CSLStringFields.NUMBER_OF_PAGES,
      "pagetotal");

  /** The provider publisher. */
  private static FieldProvider<?> PROVIDER_PUBLISHER = new StringFieldProvider(CSLStringFields.PUBLISHER,
      "howpublished", "school", "institution", "organization", "publisher");

  /** The provider publisher place. */
  private static FieldProvider<?> PROVIDER_PUBLISHER_PLACE = new StringFieldProvider(CSLStringFields.PUBLISHER_PLACE,
      "address", "location");

  /** The provider series. */
  private static FieldProvider<?> PROVIDER_SERIES = new StringFieldProvider(CSLStringFields.COLLECTION_TITLE, "series");

  /** The provider status. */
  private static FieldProvider<?> PROVIDER_STATUS = new StringFieldProvider(CSLStringFields.STATUS, "status");

  /** The provider title. */
  private static FieldProvider<?> PROVIDER_TITLE = new StringFieldProvider(CSLStringFields.TITLE, "title");

  /** The provider translators. */
  private static FieldProvider<?> PROVIDER_TRANSLATORS = new NamesFieldProvider(CSLNameFields.TRANSLATORS,
      "translator");

  /** The provider type. */
  private static FieldProvider<?> PROVIDER_TYPE = new StringFieldProvider(CSLStringFields.GENRE, "type");

  /** The provider url. */
  private static FieldProvider<?> PROVIDER_URL = new StringFieldProvider(CSLStringFields.URL, "url");

  /** The provider url date. */
  private static FieldProvider<?> PROVIDER_URL_DATE = new DateFieldProvider(CSLDateFields.ACCESSED, "urldate");

  /** The provider version. */
  private static FieldProvider<?> PROVIDER_VERSION = new StringFieldProvider(CSLStringFields.VERSION, "version");

  /** The provider volume. */
  private static FieldProvider<?> PROVIDER_VOLUME = new StringFieldProvider(CSLStringFields.VOLUME, "volume");

  /** The provider volumes. */
  private static FieldProvider<?> PROVIDER_VOLUMES = new StringFieldProvider(CSLStringFields.NUMBER_OF_VOLUMES,
      "volumes");

  /** The bibtex parser. */
  private BibTeXParser bibtexParser;

  /** The latex parser. */
  private final LaTeXParser latexParser;

  /** The latex printer. */
  private final LaTeXPrinter latexPrinter;

  /**
   * Instantiates a new bib la te X importer. *
   */
  public BibLaTeXImporter() {
    try {
      latexParser = new LaTeXParser();
      latexPrinter = new LaTeXPrinter();
      bibtexParser = new BibTeXParser() {
        @Override
        public void checkStringResolution(Key key, BibTeXString string) {
          if (string == null) {
            // ignore
          }
        }
      };
    } catch (ParseException ex) {
      throw new IllegalStateException(ex);
    }
  }

  /**
   * Parses the bib te X.
   *
   * @param service
   *          the service
   * @param bibtex
   *          the bibtex
   * @return the list
   */
  public List<CSLItemData> parseBibTeX(BookToolsService service, String bibtex) {
    List<CSLItemData> results = new ArrayList<>();

    try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(bibtex.replaceAll("\r", " ")
        .replaceAll("\n", " ").replaceAll("\\s+", " ").getBytes(Charset.forName("UTF-8"))))) {

      BibTeXDatabase db = bibtexParser.parse(reader);

      for (java.util.Map.Entry<Key, BibTeXEntry> entry : db.getEntries().entrySet()) {
        CSLItemData itemData = toItemData(service, entry.getValue());
        if (itemData != null) {
          results.add(itemData);
        }
      }
      if (StringUtils.isNotBlank(bibtex) && db.getEntries().isEmpty()) {
        service.addError(Error.PARSE_BIBTEX, "No usable content", bibtex);
      }
      return results;
    } catch (IOException | ObjectResolutionException | TokenMgrException | ParseException ex) {
      logger.warn("An error occurred while parsing BibTeX data", ex);
      service.addError(Error.PARSE_BIBTEX, ex.getMessage(), bibtex);
      return Collections.emptyList();
    }
  }

  /**
   * To item data.
   *
   * @param service
   *          the service
   * @param entry
   *          the entry
   * @return the CSL item data
   */
  public CSLItemData toItemData(BookToolsService service, BibTeXEntry entry) {

    // get all fields from the BibTeX entry
    Map<String, String> entries = new HashMap<>();
    for (Map.Entry<Key, Value> field : entry.getFields().entrySet()) {
      String us = field.getValue().toUserString();

      // convert LaTeX string to normal text
      try {
        List<LaTeXObject> objs = latexParser.parse(new StringReader(us));
        us = latexPrinter.print(objs).replaceAll("\\n", " ").replaceAll("\\r", "").trim();
      } catch (ParseException ex) {
        // ignore
      }

      entries.put(field.getKey().getValue().toLowerCase(), us);
    }

    CSLItemDataBuilder builder = new CSLItemDataBuilder();
    builder.id(StringUtils.defaultIfBlank(entry.getKey().getValue(), UUID.randomUUID().toString()));

    // common fields : must be before specific handling
    PROVIDER_EDITOR.convert(service, builder, entries);
    PROVIDER_PUBLISHER.convert(service, builder, entries);
    PROVIDER_AUTHOR.convert(service, builder, entries);
    PROVIDER_TITLE.convert(service, builder, entries);
    PROVIDER_ISSUED_FROM_DATE_THEN_YEAR_MONTH.convert(service, builder, entries);
    PROVIDER_TRANSLATORS.convert(service, builder, entries);
    PROVIDER_SERIES.convert(service, builder, entries);
    PROVIDER_NUMBER.convert(service, builder, entries);
    PROVIDER_VOLUME.convert(service, builder, entries);
    PROVIDER_EDITION.convert(service, builder, entries);
    PROVIDER_VOLUMES.convert(service, builder, entries);
    PROVIDER_ISSUE.convert(service, builder, entries);
    PROVIDER_ISBN.convert(service, builder, entries);
    PROVIDER_ISSN.convert(service, builder, entries);
    PROVIDER_CHAPTER_NUMBER.convert(service, builder, entries);
    PROVIDER_PAGES.convert(service, builder, entries);
    PROVIDER_PAGETOTAL.convert(service, builder, entries);
    PROVIDER_TYPE.convert(service, builder, entries);
    PROVIDER_VERSION.convert(service, builder, entries);
    PROVIDER_PUBLISHER_PLACE.convert(service, builder, entries);
    PROVIDER_DOI.convert(service, builder, entries);
    PROVIDER_URL.convert(service, builder, entries);
    PROVIDER_URL_DATE.convert(service, builder, entries);
    PROVIDER_LANGUAGE.convert(service, builder, entries);
    PROVIDER_NOTE.convert(service, builder, entries);
    PROVIDER_ABSTRACT.convert(service, builder, entries);
    PROVIDER_STATUS.convert(service, builder, entries);
    PROVIDER_ANNOTE.convert(service, builder, entries);

    // specific handling
    switch (entry.getType().getValue().toLowerCase()) {
      case "suppperiodical":
      case "article":
        builder.type(CSLType.ARTICLE_JOURNAL);
        PROVIDER_JOURNAL.convert(service, builder, entries);
        PROVIDER_ISSUE_TITLE.convert(service, builder, entries);
        break;
      case "reference": // like collection
      case "mvreference": // like reference
      case "collection": // like a book but without author
      case "mvcollection": // multi volume collection
      case "mvbook": // multi volume book
      case "book":
        builder.type(CSLType.BOOK);
        break;
      case "bookinbook":
      case "suppbook":
      case "inbook":
        builder.type(CSLType.CHAPTER);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        PROVIDER_BOOK_AUTHOR.convert(service, builder, entries);
        break;
      case "booklet":
        builder.type(CSLType.PAMPHLET);
        break;
      case "inreference": // like incollection
      case "suppcollection":
      case "incollection":
        builder.type(CSLType.CHAPTER);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        break;
      case "manual":
        builder.type(CSLType.BOOK);
        builder.genre("manual");
        break;
      case "software":
        builder.type(CSLType.ARTICLE);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        builder.genre("software");
        break;
      case "misc":
        builder.type(CSLType.ARTICLE);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        break;
      case "www":
      case "electronic":
      case "online":
        builder.type(CSLType.WEBPAGE);
        break;
      case "patent":
        builder.type(CSLType.PATENT);
        PROVIDER_HOLDER.convert(service, builder, entries);
        break;
      case "periodical":
        builder.type(CSLType.BOOK);
        PROVIDER_ISSUE_TITLE.convert(service, builder, entries);
        break;
      case "mvproceedings":
      case "proceedings":
        builder.type(CSLType.BOOK);
        PROVIDER_EVENT.convert(service, builder, entries);
        PROVIDER_EVENT_DATE.convert(service, builder, entries);
        PROVIDER_EVENT_PLACE.convert(service, builder, entries);
        break;
      case "conference":
      case "inproceedings":
        builder.type(CSLType.PAPER_CONFERENCE);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        PROVIDER_EVENT.convert(service, builder, entries);
        PROVIDER_EVENT_DATE.convert(service, builder, entries);
        PROVIDER_EVENT_PLACE.convert(service, builder, entries);
        break;
      case "techreport":
      case "report":
        builder.type(CSLType.REPORT);
        break;
      case "thesis":
        builder.type(CSLType.THESIS);
        break;
      case "mastersthesis":
        builder.type(CSLType.THESIS);
        if (StringUtils.isBlank(entries.get("type"))) {
          builder.genre("Master's thesis");
        }
        break;
      case "phdthesis":
        builder.type(CSLType.THESIS);
        if (StringUtils.isBlank(entries.get("type"))) {
          builder.genre("PhD thesis");
        }
        break;
      case "unpublished":
        // handled like an article
        builder.type(CSLType.ARTICLE);
        CSLStringFields.STATUS.set(builder, "unpublished");
        break;
      case "legal":
        builder.type(CSLType.TREATY);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        break;
      case "standard":
        builder.type(CSLType.TREATY);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        builder.genre("standard");
        break;
      case "jurisdiction":
      case "legislation":
        builder.type(CSLType.LEGISLATION);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        break;
      case "video":
      case "movie":
        builder.type(CSLType.MOTION_PICTURE);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        break;
      case "audio":
      case "music":
        builder.type(CSLType.MUSICAL_SCORE);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        break;
      case "review":
        builder.type(CSLType.REVIEW);
        PROVIDER_JOURNAL.convert(service, builder, entries);
        PROVIDER_ISSUE_TITLE.convert(service, builder, entries);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        break;
      case "commentary":
        builder.type(CSLType.LEGAL_CASE);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        break;
      case "artwork":
      case "image":
        builder.type(CSLType.GRAPHIC);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        break;
      case "letter":
        builder.type(CSLType.PERSONAL_COMMUNICATION);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        break;
      case "performance":
        builder.type(CSLType.BROADCAST);
        PROVIDER_BOOK_TITLE.convert(service, builder, entries);
        break;
      default:
        service.addError(Error.UNSUPPORTED_ENTRY_TYPE, entry.getType().getValue().toLowerCase());
        return null;
    }
    return builder.build();
  }

}
