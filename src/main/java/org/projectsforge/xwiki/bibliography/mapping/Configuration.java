package org.projectsforge.xwiki.bibliography.mapping;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.bibliography.Constants;
import org.projectsforge.xwiki.bibliography.macro.Scope;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * The Interface Configuration.
 */
public class Configuration {

  /** The Constant FIELD_SCOPE. */
  public static final String FIELD_SCOPE = "scope";

  /** The Constant FIELD_EXTRA_SOURCES. */
  public static final String FIELD_EXTRA_SOURCES = "extraSources";

  /** The Constant FIELD_BIBLIOGRAPHY_MAIN_STYLE. */
  public static final String FIELD_BIBLIOGRAPHY_MAIN_STYLE = "style";

  /** The Constant FIELD_BIBLIOGRAPHY_ENTRY_STYLE. */
  public static final String FIELD_BIBLIOGRAPHY_ENTRY_STYLE = "entryStyle";

  /**
   * Gets the class reference.
   *
   * @param entityReference
   *          the entity reference
   * @return the class reference
   */
  private static DocumentReference getClassReference(EntityReference entityReference) {
    return new DocumentReference(entityReference.extractReference(EntityType.WIKI).getName(),
        Constants.CODE_SPACE_NAME_AS_LIST, "ConfigurationClass");
  }

  /**
   * Gets the class reference.
   *
   * @param document
   *          the document
   * @return the class reference
   */
  public static DocumentReference getClassReference(XWikiDocument document) {
    return getClassReference(document.getDocumentReference());
  }

  /**
   * Gets the configuration page reference.
   *
   * @param entityReference
   *          the entity reference
   * @return the configuration page reference
   */
  public static DocumentReference getConfigurationPageReference(EntityReference entityReference) {
    return new DocumentReference(entityReference.extractReference(EntityType.WIKI).getName(),
        Constants.CONFIGURATION_SPACE_NAME_AS_LIST, "Configuration");
  }

  /** The xobject. */
  private BaseObject xobject;

  private BibliographyService service;

  /**
   * Instantiates a new configuration.
   *
   * @param service
   *          the service
   * @param document
   *          the document
   */
  public Configuration(BibliographyService service, XWikiDocument document) {
    this.service = service;
    xobject = document.getXObject(Configuration.getClassReference(document));
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
        service.getLogger().warn("Can not find default for style " + fieldName, ex);
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
