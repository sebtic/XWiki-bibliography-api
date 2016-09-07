package org.projectsforge.xwiki.booktools;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.mapping.Person;
import org.projectsforge.xwiki.booktools.service.BookToolsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.internal.reference.DefaultSymbolScheme;
import org.xwiki.model.internal.reference.LocalStringEntityReferenceSerializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLDateBuilder;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.helper.json.StringJsonBuilderFactory;

/**
 * The Class Utils.
 */
public class Utils {

  /** Serialize document reference without the wikiname. */
  public static final LocalStringEntityReferenceSerializer LOCAL_REFERENCE_SERIALIZER = new LocalStringEntityReferenceSerializer(
      new DefaultSymbolScheme());

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(Utils.class);

  /** The Constant STRING_JSON_BUILDER_FACTORY. */
  private static final StringJsonBuilderFactory STRING_JSON_BUILDER_FACTORY = new StringJsonBuilderFactory();

  /**
   * Convert a date to a string representation (handle single date or range,
   * partial dates (day, month, year).
   *
   * @param date
   *          the data
   * @return the string representation
   */
  public static String convertCSLDateToString(CSLDate date) {
    if (date == null) {
      return null;
    }

    int[][] parts = date.getDateParts();
    if (parts != null) {
      StringJoiner joiner = new StringJoiner("-");
      for (int element : parts[0]) {
        joiner.add(Integer.toString(element));
      }
      String result = joiner.toString();
      if (parts.length == 2) {
        joiner = new StringJoiner("-");
        for (int element : parts[1]) {
          joiner.add(Integer.toString(element));
        }
        result = result + '/' + joiner.toString();
      }
      return result;
    } else {
      return date.getRaw();
    }
  }

  /**
   * Convert value to CSL date.
   *
   * @param value
   *          the value
   * @return the CSL date
   */
  public static CSLDate convertValueToCSLDate(String value) {
    if (StringUtils.isBlank(value)) {
      return null;
    }

    String[] parts = value.split("/");
    CSLDateBuilder builder = new CSLDateBuilder();

    List<int[]> dateParts = new ArrayList<>();

    for (String part : parts) {
      String[] elements = part.split("-");
      int[] result = new int[elements.length];
      for (int i = 0; i < elements.length; ++i) {
        result[i] = Integer.parseInt(elements[i]);
      }
      if (elements.length > 0) {
        dateParts.add(result);
      }
    }

    builder.dateParts(dateParts.toArray(new int[0][]));
    return builder.build();
  }

  /**
   * Convert value to CSL names.
   *
   * @param service
   *          the service
   * @param value
   *          the value
   * @return the CSL name[]
   */
  public static CSLName[] convertValueToCSLNames(BookToolsService service, String value) {
    if (StringUtils.isBlank(value)) {
      return new CSLName[0];
    }

    List<CSLName> names = new ArrayList<>();

    for (String reference : StringUtils.defaultString(value).split("\\|")) {
      Person person = service.getPerson(reference);
      if (person != null) {
        names.add(person.getCSLObject());
      } else {
        service.addError(Error.PERSON_NOT_FOUND, reference);
      }
    }
    return names.toArray(new CSLName[0]);
  }

  /**
   * Deserialize CSL item data.
   *
   * @param service
   *          the service
   * @param value
   *          the value
   * @return the CSL item data
   */
  public static CSLItemData deserializeCSLItemData(BookToolsService service, String value) {
    if (StringUtils.isNotBlank(value)) {
      try {
        return CSLItemData.fromJson(new JsonParser(new JsonLexer(new StringReader(value))).parseObject());
      } catch (IOException ex) {
        service.addError(Error.JSON_DECODING, value);
        logger.warn("Could not decode JSON data", ex);
      }
    }
    return new CSLItemData();
  }

  /**
   * Deserialize the list of CSLItemData.
   *
   * @param value
   *          the value
   * @return the list
   */
  public static List<CSLItemData> deserializeCSLItemDatas(String value) {
    if (StringUtils.isBlank(value)) {
      return Collections.emptyList();
    }
    List<CSLItemData> entries = new ArrayList<>();
    StringReader stream = new StringReader(value);
    while (true) {
      try {
        entries.add(CSLItemData.fromJson(new JsonParser(new JsonLexer(stream)).parseObject()));
      } catch (IOException ex) {
        // an error will occur at end of data so silently ignore error ex here
        break;
      }
    }

    return entries;
  }

  /**
   * Deserialize keys.
   *
   * @param service
   *          the service
   * @param value
   *          the value
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public static List<String> deserializeKeys(BookToolsService service, String value) {
    if (StringUtils.isNotBlank(value)) {
      try {
        return new ObjectMapper().readValue(value, List.class);
      } catch (IOException ex) {
        service.addError(Error.JSON_DECODING, value);
        logger.warn("Failed decoding keys (" + value + ")", ex);
      }
    }
    return Collections.emptyList();
  }

  /**
   * Serialize CSL item data.
   *
   * @param data
   *          the data
   * @return the string
   */
  public static String serializeCSLItemData(CSLItemData data) {
    return (String) data.toJson(STRING_JSON_BUILDER_FACTORY.createJsonBuilder());
  }

  /**
   * Serialize CSL name.
   *
   * @param name
   *          the name
   * @return the string
   */
  public static String serializeCSLName(CSLName name) {
    return (String) name.toJson(STRING_JSON_BUILDER_FACTORY.createJsonBuilder());
  }

  /**
   * Serialized CSL item datas.
   *
   * @param data
   *          the data
   * @return the string
   */
  public static String serializedCSLItemDatas(List<CSLItemData> data) {
    StringBuilder builder = new StringBuilder();
    if (data != null) {
      StringJsonBuilderFactory factory = new StringJsonBuilderFactory();
      for (CSLItemData item : data) {
        builder.append(item.toJson(factory.createJsonBuilder()));
      }
    }
    return builder.toString();
  }

  /**
   * Serialize keys.
   *
   * @param service
   *          the service
   * @param keys
   *          the keys
   * @return the string
   */
  public static String serializeKeys(BookToolsService service, List<String> keys) {
    try {
      if (!keys.isEmpty()) {
        return new ObjectMapper().writeValueAsString(keys);
      }
    } catch (JsonProcessingException ex) {
      service.addError(Error.JSON_ENCODING, keys);
      logger.debug("Can not serialize keys", ex);
    }
    return "";
  }

  /**
   * Instantiates a new utils.
   */
  private Utils() {
  }
}
