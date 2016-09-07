package org.projectsforge.xwiki.booktools.mapping;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.Constants;
import org.projectsforge.xwiki.booktools.macro.Scope;
import org.projectsforge.xwiki.booktools.mapping.DocumentWalker.Node;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.objects.BaseObject;

/**
 * The Interface Configuration.
 */
public class Configuration {

  /** The Constant CLASS_REFERENCE. */
  public static final EntityReference CLASS_REFERENCE = new EntityReference("ConfigurationClass", EntityType.DOCUMENT,
      Constants.CODE_SPACE_REFERENCE);

  /** The Constant CLASS_REFERENCE_AS_STRING. */
  public static final String CLASS_REFERENCE_AS_STRING = Constants.CODE_SPACE_NAME_AS_STRING + ".ConfigurationClass";

  /** The Constant FIELD_BIBLIOGRAPHY_ENTRY_STYLE. */
  public static final String FIELD_BIBLIOGRAPHY_ENTRY_STYLE = "entryStyle";

  /** The Constant FIELD_BIBLIOGRAPHY_MAIN_STYLE. */
  public static final String FIELD_BIBLIOGRAPHY_MAIN_STYLE = "style";

  /** The Constant FIELD_EXTRA_SOURCES. */
  public static final String FIELD_EXTRA_SOURCES = "extraSources";

  /** The Constant FIELD_SCOPE. */
  public static final String FIELD_SCOPE = "scope";

  /** The node. */
  private Node node;

  /** The xobject. */
  private BaseObject xobject;

  /**
   * Instantiates a new configuration.
   *
   * @param node
   *          the node
   */
  public Configuration(Node node) {
    this.node = node;
    this.xobject = node.getXObject(CLASS_REFERENCE);
  }

  /**
   * Gets the bibliography style.
   *
   * @param fieldName
   *          the field name
   * @return the bibliography style
   */
  public String getBibliographyStyle(String fieldName) {
    String style = null;
    if (xobject != null) {
      style = StringUtils.defaultIfBlank(xobject.getLargeStringValue(fieldName), null);
    }
    if (StringUtils.isBlank(style)) {
      try {
        style = IOUtils.toString(getClass().getResource("/csl/" + fieldName + ".csl"), Charset.forName("UTF-8"));
      } catch (IOException ex) {
        node.getService().getLogger().warn("Can not find default for style " + fieldName, ex);
      }
    }
    if (StringUtils.isBlank(style)) {
      style = "ieee";
    }
    return style;
  }

  /**
   * Gets the extra wiki sources from which entries are retrieved.
   *
   * @return the extra wiki sources
   */
  @SuppressWarnings("unchecked")
  public List<String> getExtraWikiSources() {
    List<String> result = null;
    if (xobject != null) {
      result = xobject.getListValue(FIELD_EXTRA_SOURCES);
    }
    if (result == null) {
      result = Collections.emptyList();
    }
    return result;
  }

  /**
   * Gets the scope.
   *
   * @return the scope
   */
  public Scope getScope() {
    if (xobject != null) {
      return Scope.toScope(StringUtils.defaultString(xobject.getStringValue(FIELD_SCOPE)).trim());
    }
    return Scope.UNDEFINED;
  }

}
