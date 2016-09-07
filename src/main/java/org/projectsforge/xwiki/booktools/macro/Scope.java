package org.projectsforge.xwiki.booktools.macro;

/**
 * The Enum Scope.
 */
public enum Scope {

  /** The cited. */
  CITED("cited"),
  /** The hidden. */
  HIDDEN("hidden"),
  /** The page. */
  PAGE("page"),
  /** The undefined. */
  UNDEFINED("");

  /**
   * Convert a string to a scope.
   *
   * @param value
   *          the value
   * @return the scope
   */
  public static Scope toScope(String value) {
    for (Scope scope : Scope.values()) {
      if (scope.value.equalsIgnoreCase(value)) {
        return scope;
      }
    }
    return Scope.UNDEFINED;
  }

  /** The value. */
  private String value;

  /**
   * Instantiates a new scope.
   *
   * @param value
   *          the value
   */
  Scope(String value) {
    this.value = value;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return value;
  }
}
