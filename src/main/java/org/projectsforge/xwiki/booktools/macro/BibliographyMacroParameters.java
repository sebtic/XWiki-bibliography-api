package org.projectsforge.xwiki.booktools.macro;

import org.xwiki.properties.annotation.PropertyDescription;

/**
 * The Class MacroParameters.
 */
public class BibliographyMacroParameters {

  /** The Constant PARAM_SCOPE. */
  public static final String PARAM_SCOPE = "scope";

  /** The scope default. */
  public static final Scope SCOPE_DEFAULT = Scope.CITED;

  /** The scope. */
  private String scope = SCOPE_DEFAULT.toString();

  /**
   * Gets the scope.
   *
   * @return the scope
   */
  public String getScope() {
    return scope;
  }

  /**
   * Sets the scope.
   *
   * @param scope
   *          the new scope
   */
  @PropertyDescription("Define the scope of of printed bibliography (page/cited(default))")
  public void setScope(String scope) {
    this.scope = scope;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "BibliographyMacroParameters [scope=" + scope + "]";
  }

}
