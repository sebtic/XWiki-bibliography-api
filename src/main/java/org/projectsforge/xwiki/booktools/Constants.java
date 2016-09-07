package org.projectsforge.xwiki.booktools;

import java.util.Arrays;
import java.util.List;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

/**
 * The Class Constants.
 */
public abstract class Constants {

  /** The Constant EXTENSION_SPACE_NAME. */
  public static final String EXTENSION_SPACE_NAME = "BookTools";

  /** The Constant EXTENSION_SPACE_REFERENCE. */
  public static final EntityReference EXTENSION_SPACE_REFERENCE = new EntityReference(EXTENSION_SPACE_NAME,
      EntityType.SPACE);

  /**
   * The mark replaced by the wiki bibliography page in citeproc output for
   * citation.
   **/
  public static final String CITE_TARGET_MARK = "BIBLIOGRAPHY_CITE_TARGET_MARK";

  /** The Constant CODE_SPACE_NAME_AS_STRING. */
  public static final String CODE_SPACE_NAME_AS_STRING = EXTENSION_SPACE_NAME + "." + "Code";

  /** The Constant CODE_SPACE_REFERENCE. */
  public static final EntityReference CODE_SPACE_REFERENCE = new EntityReference("Code", EntityType.SPACE,
      EXTENSION_SPACE_REFERENCE);

  /** The Constant CONFIGURATION_SPACE_NAME_AS_LIST. */
  public static final List<String> CONFIGURATION_SPACE_NAME_AS_LIST = Arrays.asList(EXTENSION_SPACE_NAME,
      "Configuration");

  /** The Constant CONFIGURATION_SPACE_NAME_AS_STRING. */
  public static final String CONFIGURATION_SPACE_NAME_AS_STRING = EXTENSION_SPACE_NAME + "." + "Configuration";

  /** The Constant CONFIGURATION_SPACE_REFERENCE. */
  public static final EntityReference CONFIGURATION_SPACE_REFERENCE = new EntityReference("Configuration",
      EntityType.SPACE, EXTENSION_SPACE_REFERENCE);

  /** The Constant CONTEXT_BIBLIOGRAPHY_ERROR. */
  public static final String CONTEXT_BIBLIOGRAPHY_ERROR = "bibliography_error";

  /** The Constant ENTRIES_SPACE_NAME_AS_STRING. */
  public static final String ENTRIES_SPACE_NAME_AS_STRING = EXTENSION_SPACE_NAME + "." + "Data" + "." + "Entries";

  /** The Constant ENTRIES_SPACE_REFERENCE. */
  public static final EntityReference ENTRIES_SPACE_REFERENCE = new EntityReference("Entries", EntityType.SPACE,
      new EntityReference("Data", EntityType.SPACE, EXTENSION_SPACE_REFERENCE));

  /**
   * The mark replaced by the document reference of the entry in citeproc
   * output.
   */
  public static final String ENTRY_TARGET_MARK = "BIBLIOGRAPHY_ENTRY_TARGET_MARK";

  /** The Constant PERSONS_SPACE_NAME_AS_STRING. */
  public static final String PERSONS_SPACE_NAME_AS_STRING = EXTENSION_SPACE_NAME + "." + "Data" + "." + "Persons";

  /** The Constant PERSONS_SPACE_REFERENCE. */
  public static final EntityReference PERSONS_SPACE_REFERENCE = new EntityReference("Persons", EntityType.SPACE,
      new EntityReference("Data", EntityType.SPACE, EXTENSION_SPACE_REFERENCE));

  /** The Constant WYSIWYG_MACRO_CATEGORY. */
  public static final String WYSIWYG_MACRO_CATEGORY = "Book tools";

  /**
   * Hide constructor.
   */
  private Constants() {
  }
}
