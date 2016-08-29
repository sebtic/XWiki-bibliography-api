package org.projectsforge.xwiki.bibliography;

import java.util.Arrays;
import java.util.List;

/**
 * The Class Error.
 */
public class Error {

  /** The Constant IOEXCEPTION. */
  public static final String IOEXCEPTION = "bibliography.error.ioexception";

  /** The Constant JSON_DECODING. */
  public static final String JSON_DECODING = "bibliography.error.json-decoding";

  /** The Constant XWIKI_GET_DOCUMENT. */
  public static final String XWIKI_GET_DOCUMENT = "bibliography.error.xwiki-get-document";

  /** The Constant JSON_ENCODING. */
  public static final String JSON_ENCODING = "bibliography.error.json-encoding";

  /** The Constant SAVE_DOCUMENT. */
  public static final String SAVE_DOCUMENT = "bibliography.error.save-document";

  /** The Constant CSLTYPE_FROM_STRING. */
  public static final String CSLTYPE_FROM_STRING = "bibliography.error.csltype-from-string";

  /** The Constant PERSON_NOT_FOUND. */
  public static final String PERSON_NOT_FOUND = "bibliography.error.person-not-found";

  /** The Constant GET_OR_ADD_PERSON. */
  public static final String GET_OR_ADD_PERSON = "bibliography.error.get-or-add-person";

  /** The Constant BUILD_CSLDATAITEM. */
  public static final String BUILD_CSLDATAITEM = "bibliography.error.build-csldataitem";

  /** The Constant CSL. */
  public static final String CSL = "bibliography.error.csl";

  /** The Constant QUERY. */
  public static final String QUERY = "bibliography.error.query";

  /** The Constant PARSE_BIBTEX. */
  public static final String PARSE_BIBTEX = "bibliography.error.parse-bitex";

  /** The Constant EMPTY_ID. */
  public static final String EMPTY_ID = "bibliography.error.empty-id";

  /** The Constant ID_ALREADY_EXISTS. */
  public static final String ID_ALREADY_EXISTS = "bibliography.error.id-already-exists";

  /** The Constant INVALID_ID_FORMAT. */
  public static final String INVALID_ID_FORMAT = "bibliography.error.invalid-id-format";

  /** The Constant UNSUPPORTED_ENTRY_TYPE. */
  public static final String UNSUPPORTED_ENTRY_TYPE = "bibliography.error.unsupported-entry-type";

  /** The Constant INVALID_DATE. */
  public static final String INVALID_DATE = "bibliography.error.invalid-date";

  /** The id. */
  private String id;

  /** The params. */
  private List<Object> params;

  /**
   * Instantiates a new error.
   *
   * @param id
   *          the id
   * @param params
   *          the params
   */
  public Error(String id, Object[] params) {
    this.id = id;
    this.params = Arrays.asList(params);
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the params.
   *
   * @return the params
   */
  public List<Object> getParams() {
    return params;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return id + " " + params;
  }
}
