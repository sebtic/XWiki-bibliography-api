package org.projectsforge.xwiki.booktools.macro;

import org.xwiki.properties.annotation.PropertyDescription;

/**
 * The Class CiteMacroParameters.
 */
public class CiteMacroParameters {

  /** The Constant HIDDEN_DEFAULT. */
  public static final boolean HIDDEN_DEFAULT = false;

  /** The Constant HIDDEN. */
  public static final String PARAM_HIDDEN = "hidden";

  /** The Constant KEYS. */
  public static final String PARAM_KEYS = "keys";

  /** The hidden. */
  private boolean hidden = HIDDEN_DEFAULT;

  /** The keys. */
  private String keys;

  /**
   * Gets the keys.
   *
   * @return the keys
   */
  public String getKeys() {
    return this.keys;
  }

  /**
   * Checks if is hidden.
   *
   * @return true, if is hidden
   */
  public boolean isHidden() {
    return hidden;
  }

  /**
   * Sets the hidden.
   *
   * @param hidden
   *          the new hidden
   */
  @PropertyDescription("Define if the bibliographic citations must be printed.")
  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  /**
   * Sets the keys.
   *
   * @param keys
   *          the new keys
   */
  @PropertyDescription("Define bibliographic entry keys separated by a comma.")
  public void setKeys(String keys) {
    this.keys = keys;
  }
}
