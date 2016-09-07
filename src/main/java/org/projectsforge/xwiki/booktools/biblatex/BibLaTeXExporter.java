package org.projectsforge.xwiki.booktools.biblatex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLName;

/**
 * The Class BibLaTeXExporter.
 */
public class BibLaTeXExporter {

  /** The Constant BIBLATEX_ID. */
  private static final String BIBLATEX_ID = "id";

  /** The Constant BIBLATEX_TYPE. */
  private static final String BIBLATEX_TYPE = "biblatex-type";

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(BibLaTeXExporter.class);

  /**
   * Adds the escaped field.
   *
   * @param entry
   *          the entry
   * @param field
   *          the field
   * @param text
   *          the text
   */
  private static void addEscapedField(Map<String, String> entry, String field, String text) {
    if (StringUtils.isNotBlank(text)) {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < text.length(); ++i) {
        char c = text.charAt(i);
        if (Character.isUpperCase(c)) {
          builder.append('{').append(c).append('}');
        } else {
          builder.append(c);
        }
      }
      entry.put(field, builder.toString());
    }
  }

  /**
   * Adds the field.
   *
   * @param entry
   *          the entry
   * @param field
   *          the field
   * @param names
   *          the names
   */
  private static void addField(Map<String, String> entry, String field, CSLName[] names) {
    StringJoiner allNames = new StringJoiner(" and ");
    if (names != null && names.length > 0) {
      for (CSLName name : names) {
        StringJoiner joiner = new StringJoiner(",");

        if (logger.isDebugEnabled()) {
          logger.debug("CSLName {}", Utils.serializeCSLName(name));
        }
        String family = "";
        if (StringUtils.isNotBlank(name.getDroppingParticle())) {
          family = family + escapeUpperAndComa(name.getDroppingParticle()) + " ";
        }
        if (StringUtils.isNotBlank(name.getNonDroppingParticle())) {
          family = family + escapeUpperAndComa(name.getNonDroppingParticle()) + " ";
        }
        joiner.add(family + escapeUpperAndComa(name.getFamily()));

        if (StringUtils.isNotBlank(name.getSuffix())) {
          joiner.add(escapeUpperAndComa(name.getSuffix()));
        }

        joiner.add(" " + escapeUpperAndComa(name.getGiven()));

        allNames.add(joiner.toString());
      }
      entry.put(field, allNames.toString());
    }
  }

  /**
   * Adds the field.
   *
   * @param entry
   *          the entry
   * @param field
   *          the field
   * @param text
   *          the text
   */
  private static void addField(Map<String, String> entry, String field, String text) {
    if (StringUtils.isNotBlank(text)) {
      entry.put(field, text);
    }
  }

  /**
   * Builds the bib te X entry.
   *
   * @param itemData
   *          the item data
   * @return the bib te X entry
   */
  private static Map<String, String> buildBibTeXEntry(CSLItemData itemData) {
    Map<String, String> entry = new HashedMap<>();

    String type = "article";

    switch (itemData.getType()) {
      case ARTICLE:
        switch (StringUtils.defaultString(itemData.getGenre())) {
          case "software":
            type = "software";
            break;
          case "unpublished":
            type = "unpublished";
            break;
          default:
            type = "misc";
        }
        break;
      case ARTICLE_MAGAZINE:
      case ARTICLE_NEWSPAPER:
      case ARTICLE_JOURNAL:
        type = "article";
        break;
      case MANUSCRIPT:
      case BOOK:
        type = "book";
        if (StringUtils.isNotBlank(itemData.getContainerTitle())) {
          type = "periodical";
        }
        if ("manual".equals(itemData.getGenre())) {
          type = "manual";
        }
        if (StringUtils.isNotBlank(itemData.getEvent()) || StringUtils.isNotBlank(itemData.getEventplace())
            || itemData.getEventDate() != null) {
          type = "proceedings";
        }
        break;
      case BROADCAST:
        type = "performance";
        break;
      case CHAPTER:
        type = "inbook";
        break;
      case BILL:
      case DATASET:
      case ENTRY:
      case ENTRY_DICTIONARY:
      case ENTRY_ENCYCLOPEDIA:
      case MAP:
        type = "misc";
        break;
      case FIGURE:
      case GRAPHIC:
        type = "image";
        break;
      case INTERVIEW:
        break;
      case LEGAL_CASE:
        type = "commentary";
        break;
      case LEGISLATION:
        type = "legislation";
        break;
      case MOTION_PICTURE:
        type = "movie";
        break;
      case SPEECH:
      case SONG:
      case MUSICAL_SCORE:
        type = "music";
        break;
      case PAMPHLET:
        type = "booklet";
        break;
      case PAPER_CONFERENCE:
        type = "inproceedings";
        break;
      case PATENT:
        type = "patent";
        break;
      case PERSONAL_COMMUNICATION:
        type = "letter";
        break;
      case REPORT:
        type = "techreport";
        break;
      case REVIEW_BOOK:
      case REVIEW:
        type = "review";
        break;
      case THESIS:
        switch (StringUtils.defaultIfBlank(itemData.getGenre(), "").toLowerCase()) {
          case "master's thesis":
            type = "mastersthesis";
            break;
          case "phd thesis":
            type = "phdthesis";
            break;
          default:
            type = "thesis";
        }
        break;
      case TREATY:
        type = "legal";
        if ("standard".equalsIgnoreCase(itemData.getGenre())) {
          type = "standard";
        }
        break;
      case POST:
      case POST_WEBLOG:
      case WEBPAGE:
        type = "online";
        break;
    }

    addField(entry, BIBLATEX_TYPE, type);
    addField(entry, BIBLATEX_ID, itemData.getId());

    addField(entry, "author", itemData.getAuthor());
    addField(entry, "editor", itemData.getEditor());
    switch (type) {
      case "booklet":
      case "misc":
        addEscapedField(entry, "howpublished", itemData.getPublisher());
        break;
      case "thesis":
      case "mastersthesis":
      case "phdthesis":
        addEscapedField(entry, "school", itemData.getPublisher());
        break;
      case "report":
      case "techreport":
        addEscapedField(entry, "institution", itemData.getPublisher());
        break;
      case "online":
      case "manual":
        addEscapedField(entry, "organization", itemData.getPublisher());
        break;
      default:
        addEscapedField(entry, "published", itemData.getPublisher());
    }
    addEscapedField(entry, "title", itemData.getTitle());

    addField(entry, "date", Utils.convertCSLDateToString(itemData.getIssued()));
    CSLDate issued = itemData.getIssued();
    if (issued != null) {
      int[][] parts = issued.getDateParts();
      if (parts.length == 1) {
        if (parts[0].length >= 1) {
          addField(entry, "year", Integer.toString(parts[0][0]));
        }
        if (parts[0].length >= 2) {
          addField(entry, "month", Integer.toString(parts[0][1]));
        }
        if (parts[0].length >= 3) {
          addField(entry, "day", Integer.toString(parts[0][2]));
        }
      } else {
        if (parts[0].length >= 1) {
          addField(entry, "year", Integer.toString(parts[0][0]) + "/" + Integer.toString(parts[1][0]));
        }
        if (parts[0].length >= 2) {
          addField(entry, "month", Integer.toString(parts[0][1]));
        }
        if (parts[0].length >= 3) {
          addField(entry, "day", Integer.toString(parts[0][2]));
        }
      }
    }

    addField(entry, "translator", itemData.getTranslator());

    addEscapedField(entry, "series", itemData.getCollectionTitle());

    addField(entry, "number", itemData.getNumber());

    addField(entry, "volume", itemData.getVolume());

    addField(entry, "edition", itemData.getEdition());

    addField(entry, "volumes", itemData.getNumberOfVolumes());

    addEscapedField(entry, "issue", itemData.getIssue());

    addField(entry, "isbn", itemData.getISBN());

    addField(entry, "issn", itemData.getISSN());

    addField(entry, "pages", itemData.getPage());

    addField(entry, "pagetotal", itemData.getNumberOfPages());

    addField(entry, "type", itemData.getGenre());

    addField(entry, "version", itemData.getVersion());

    addEscapedField(entry, "address", itemData.getPublisherPlace());

    addField(entry, "status", itemData.getStatus());

    addField(entry, "doi", itemData.getDOI());

    addField(entry, "url", itemData.getURL());

    addField(entry, "urldate", Utils.convertCSLDateToString(itemData.getAccessed()));

    addField(entry, "language", itemData.getLanguage());

    addEscapedField(entry, "note", itemData.getNote());

    addField(entry, "abstract", itemData.getAbstrct());

    switch (type) {
      case "article":
        addEscapedField(entry, "journaltitle", itemData.getCollectionTitle());
        addEscapedField(entry, "issuetitle", itemData.getContainerTitle());
        break;
      case "inbook":
        addField(entry, "bookauthor", itemData.getContainerAuthor());
        addEscapedField(entry, "booktitle", itemData.getContainerTitle());
        break;
      case "patent":
        addEscapedField(entry, "holder", itemData.getPublisher());
        break;
      case "periodical":
        addEscapedField(entry, "issuetitle", itemData.getContainerTitle());
        break;
      case "proceedings":
        addEscapedField(entry, "booktitle", itemData.getEvent());
        addEscapedField(entry, "eventtitle", itemData.getEvent());
        addEscapedField(entry, "venue", itemData.getEventplace());
        addField(entry, "eventdate", Utils.convertCSLDateToString(itemData.getEventDate()));
        break;
      case "inproceedings":
        addEscapedField(entry, "booktitle", itemData.getContainerTitle());
        addEscapedField(entry, "eventtitle", itemData.getEvent());
        addEscapedField(entry, "venue", itemData.getEventplace());
        addField(entry, "eventdate", Utils.convertCSLDateToString(itemData.getEventDate()));
        break;
      case "software":
      case "misc":
      case "legal":
      case "standard":
      case "legislation":
      case "movie":
      case "music":
      case "commentary":
      case "image":
      case "letter":
      case "performance":
        addEscapedField(entry, "booktitle", itemData.getContainerTitle());
        break;
      case "review":
        addEscapedField(entry, "journaltitle", itemData.getCollectionTitle());
        addEscapedField(entry, "issuetitle", itemData.getContainerTitle());
        addEscapedField(entry, "booktitle", itemData.getContainerTitle());
        break;
    }

    return entry;
  }

  /**
   * Escape upper and coma.
   *
   * @param text
   *          the text
   * @return the string
   */
  private static String escapeUpperAndComa(String text) {
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < text.length(); ++i) {
      char c = text.charAt(i);
      if (Character.isUpperCase(c) || c == ',') {
        builder.append('{').append(c).append('}');
      } else {
        builder.append(c);
      }
    }
    return builder.toString();
  }

  /**
   * Export.
   *
   * @param itemData
   *          the item data
   * @return the string
   */
  public static String export(CSLItemData itemData) {

    if (logger.isDebugEnabled()) {
      logger.debug("Exporting {}", Utils.serializeCSLItemData(itemData));
    }

    Map<String, String> entry = buildBibTeXEntry(itemData);

    StringJoiner joiner = new StringJoiner(",\n    ");
    joiner.add("@" + entry.get(BIBLATEX_TYPE) + '{' + entry.get("id"));
    ArrayList<String> keys = new ArrayList<>(entry.keySet());
    Collections.sort(keys);
    for (String key : keys) {
      String value = entry.get(key);

      if (!BIBLATEX_TYPE.equals(key) && !BIBLATEX_ID.equals(key)) {
        joiner.add(key + " = {" + value + "}");
      }
    }
    return joiner.toString() + "\n}";
  }

  /**
   * Instantiates a new bib la te X exporter.
   */
  private BibLaTeXExporter() {
  }
}
