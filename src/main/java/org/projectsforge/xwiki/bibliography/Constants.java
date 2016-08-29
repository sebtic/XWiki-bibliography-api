package org.projectsforge.xwiki.bibliography;

import java.util.Arrays;
import java.util.List;

/**
 * The Class Constants.
 */
public abstract class Constants {

  /** The Constant EXTENSION_SPACE_NAME. */
  public static final String EXTENSION_SPACE_NAME = "Bibliography";
  /** The Constant SPACE_NAME. */
  public static final List<String> CODE_SPACE_NAME_AS_LIST = Arrays.asList(EXTENSION_SPACE_NAME, "Code");

  /** The Constant CODE_SPACE_NAME_AS_STRING. */
  public static final String CODE_SPACE_NAME_AS_STRING = EXTENSION_SPACE_NAME + "." + "Code";

  /** The Constant CONFIGURATION_SPACE_NAME_AS_LIST. */
  public static final List<String> CONFIGURATION_SPACE_NAME_AS_LIST = Arrays.asList(EXTENSION_SPACE_NAME,
      "Configuration");

  /** The Constant CONFIGURATION_SPACE_NAME_AS_STRING. */
  public static final String CONFIGURATION_SPACE_NAME_AS_STRING = EXTENSION_SPACE_NAME + "." + "Configuration";

  /** The Constant PERSONS_SPACE_NAME_AS_LIST. */
  public static final List<String> PERSONS_SPACE_NAME_AS_LIST = Arrays.asList(EXTENSION_SPACE_NAME, "Data", "Persons");

  /** The Constant PERSONS_SPACE_NAME_AS_STRING. */
  public static final String PERSONS_SPACE_NAME_AS_STRING = EXTENSION_SPACE_NAME + "." + "Data" + "." + "Persons";

  /** The Constant ENTRIES_SPACE_NAME_AS_LIST. */
  public static final List<String> ENTRIES_SPACE_NAME_AS_LIST = Arrays.asList(EXTENSION_SPACE_NAME, "Data", "Entries");

  /** The Constant ENTRIES_SPACE_NAME_AS_STRING. */
  public static final String ENTRIES_SPACE_NAME_AS_STRING = EXTENSION_SPACE_NAME + "." + "Data" + "." + "Entries";

  /** The Constant WYSIWYG_MACRO_CATEGORY. */
  public static final String WYSIWYG_MACRO_CATEGORY = "Bibliography";

  /**
   * The mark replaced by the wiki bibliography page in citeproc output for
   * citation.
   **/
  public static final String CITE_TARGET_MARK = "BIBLIOGRAPHY_CITE_TARGET_MARK";

  /**
   * The mark replaced by the document reference of the entry in citeproc
   * output.
   */
  public static final String ENTRY_TARGET_MARK = "BIBLIOGRAPHY_ENTRY_TARGET_MARK";

  /** The Constant CONTEXT_BIBLIOGRAPHY_ERROR. */
  public static final String CONTEXT_BIBLIOGRAPHY_ERROR = "bibliography_error";

  /**
   * Instantiates a new bibliography constants.
   */
  private Constants() {
  }
}
