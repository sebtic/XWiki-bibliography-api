package org.projectsforge.xwiki.bibliography.job;

import org.xwiki.job.AbstractRequest;
import org.xwiki.model.reference.WikiReference;

/**
 * The Class ConfigurationUpdatedJobRequest.
 */
public class ConfigurationUpdatedJobRequest extends AbstractRequest {

  private static final long serialVersionUID = 1L;

  /** The Constant PROPERTY_WIKI_REFERENCE. */
  private static final String PROPERTY_WIKI_REFERENCE = "wikiReference";

  /**
   * Gets the wiki reference.
   *
   * @return the wiki reference
   */
  public WikiReference getWikiReference() {
    return getProperty(PROPERTY_WIKI_REFERENCE);
  }

  /**
   * Sets the wiki reference.
   *
   * @param wikiReference
   *          the new wiki reference
   */
  public void setWikiReference(WikiReference wikiReference) {
    setProperty(PROPERTY_WIKI_REFERENCE, wikiReference);
  }
}
