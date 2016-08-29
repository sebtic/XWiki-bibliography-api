package org.projectsforge.xwiki.bibliography.service;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.model.reference.DocumentReference;

/**
 * The Class SortableDocumentReference.
 */
public class SortableDocumentReference implements Comparable<SortableDocumentReference> {

  /** The document reference. */
  private DocumentReference documentReference;

  /** The description. */
  private String description;

  /**
   * Instantiates a new sortable document reference.
   *
   * @param description
   *          the description
   * @param documentReference
   *          the document reference
   */
  public SortableDocumentReference(String description, DocumentReference documentReference) {
    this.description = description;
    this.documentReference = documentReference;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(SortableDocumentReference other) {
    String first = StringUtils.stripAccents(description);
    String second = StringUtils.stripAccents(other.description);

    int result = first.compareToIgnoreCase(second);
    if (result == 0) {
      result = first.compareTo(second);
    }
    if (result == 0) {
      result = description.compareToIgnoreCase(other.description);
    }
    if (result == 0) {
      result = description.compareTo(other.description);
    }
    if (result == 0) {
      result = documentReference.compareTo(other.documentReference);
    }
    return result;
  }

  /**
   * Equals.
   *
   * @param other
   *          the other
   * @return true, if successful
   */
  boolean equals(SortableDocumentReference other) {
    return compareTo(other) == 0;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the document reference.
   *
   * @return the document reference
   */
  public DocumentReference getDocumentReference() {
    return documentReference;
  }

}
