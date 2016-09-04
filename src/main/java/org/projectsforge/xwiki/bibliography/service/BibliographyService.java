package org.projectsforge.xwiki.bibliography.service;

import java.util.List;
import java.util.Map;

import org.projectsforge.xwiki.bibliography.Error;
import org.projectsforge.xwiki.bibliography.mapping.Configuration;
import org.projectsforge.xwiki.bibliography.mapping.Entry;
import org.projectsforge.xwiki.bibliography.mapping.Index;
import org.projectsforge.xwiki.bibliography.mapping.Person;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLName;

/**
 * Interface (aka Role) of the Component.
 */
@Role
public interface BibliographyService {

  /**
   * Adds the error.
   *
   * @param id
   *          the id
   * @param params
   *          the params
   */
  void addError(String id, Object... params);

  /**
   * Clear errors.
   */
  void clearErrors();

  /**
   * Creates the entry from the CSLItemData on the current wiki.
   *
   * @param data
   *          the data
   * @return the document reference
   */
  DocumentReference createEntryFromCSLItemData(CSLItemData data);

  /**
   * Create a person in the database populated with the given CSLName.
   *
   * @param name
   *          the CSL data
   * @return the document reference or null if there is an exception
   */
  DocumentReference createPersonFromCSLName(CSLName name);

  /**
   * Ensure requirements.
   */
  void ensureRequirements();

  /**
   * Find entry.
   *
   * @param index
   *          the index
   * @param key
   *          the key
   * @return the entry
   */
  Entry findEntry(Index index, String key);

  /**
   * Find entry reference.
   *
   * @param index
   *          the index
   * @param key
   *          the key
   * @return the document reference
   */
  DocumentReference findEntryReference(Index index, String key);

  /**
   * Find entry reference on wiki.
   *
   * @param wikiReference
   *          the wiki reference
   * @param key
   *          the key
   * @return the document reference
   */
  DocumentReference findEntryReferenceOnWiki(WikiReference wikiReference, String key);

  /**
   * Find index.
   *
   * @param document
   *          the document
   * @return the index
   */
  Index findIndex(XWikiDocument document);

  /**
   * Find person from CSL name.
   *
   * @param wikiReference
   *          the wiki reference
   * @param name
   *          the name
   * @return the document reference
   */
  DocumentReference findPersonFromCSLName(WikiReference wikiReference, CSLName name);

  /**
   * Gets the context.
   *
   * @return the context
   */
  XWikiContext getContext();

  /**
   * Gets the CSL object.
   *
   * @param index
   *          the index
   * @return the CSL object
   */
  CSL getCSL(Index index);

  /**
   * Gets the default configuration.
   *
   * @param wikiReference
   *          the wiki reference
   * @return the default configuration
   */
  Configuration getDefaultConfiguration(WikiReference wikiReference);

  /**
   * Gets the documents referencing an entry. Since two entries on two different
   * wikis can share the same id, the list from other wiki can not be considered
   * as reliable.
   *
   * @param entryId
   *          the entry citation id
   * @return the documents referencing entry by wiki
   */
  Map<String, List<DocumentReference>> getDocumentReferencingEntry(String entryId);

  /**
   * Gets the entries.
   *
   * @param index
   *          the index
   * @return the entries
   */
  List<SortableDocumentReference> getEntries(Index index);

  /**
   * Gets the entries on wiki.
   *
   * @param wikiReference
   *          the wiki reference
   * @return the entries on wiki
   */
  List<SortableDocumentReference> getEntriesOnWiki(WikiReference wikiReference);

  /**
   * Gets the entry referencing a person on all wikis.
   *
   * @param personRef
   *          the person reference (wiki reference)
   * @return the entries referencing the person by wiki
   */
  Map<String, List<DocumentReference>> getEntryReferencingAPerson(String personRef);

  /**
   * Gets the errors.
   *
   * @return the errors
   */
  List<Error> getErrors();

  /**
   * Gets the logger.
   *
   * @return the logger
   */
  Logger getLogger();

  /**
   * Gets the new annotation reference.
   *
   * @param entry
   *          the entry
   * @return the new annotation reference
   */
  DocumentReference getNewAnnotationReference(DocumentReference entry);

  /**
   * Gets the new attachment reference.
   *
   * @param entry
   *          the entry
   * @return the new attachment reference
   */
  DocumentReference getNewAttachmentReference(DocumentReference entry);

  /**
   * Gets the new entry reference.
   *
   * @return the new entry reference
   */
  DocumentReference getNewEntryReference();

  /**
   * Gets the new person reference.
   *
   * @return the new person reference
   */
  DocumentReference getNewPersonReference();

  /**
   * Gets the person.
   *
   * @param reference
   *          the reference
   * @return the person
   */
  Person getPerson(String reference);

  /**
   * Merge persons.
   *
   * @param source
   *          the source
   * @param destination
   *          the destination
   * @return true, if successful
   */
  boolean mergePersons(String source, String destination);

  /**
   * Parses the BibTeX data.
   *
   * @param bibtex
   *          the BibTeX data
   * @return the list of entries
   */
  List<CSLItemData> parseBibTeX(String bibtex);

  /**
   * Validate entry.
   *
   * @param doc
   *          the doc
   * @return null if successfull, the error code otherwise
   */
  String validateEntry(XWikiDocument doc);

}
