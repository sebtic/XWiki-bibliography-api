package org.projectsforge.xwiki.booktools.service;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.projectsforge.xwiki.booktools.Error;
import org.projectsforge.xwiki.booktools.mapping.DocumentWalker;
import org.projectsforge.xwiki.booktools.mapping.Index;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.QueryException;
import org.xwiki.script.service.ScriptService;
import org.xwiki.wiki.manager.WikiManagerException;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

import de.undercouch.citeproc.csl.CSLItemData;

/**
 * The Class BookToolsScriptService.
 */
@Component
@Singleton
@Named("booktools")
public class BookToolsScriptService implements ScriptService {

  /** The service. */
  @Inject
  private BookToolsService service;

  /**
   * Adds the error.
   *
   * @param id
   *          the id
   * @param params
   *          the params
   */
  public void addError(String id, Object... params) {
    service.addError(id, params);
  }

  /**
   * Clear errors.
   */
  public void clearErrors() {
    service.clearErrors();
  }

  /**
   * Creates the entry from CSL item data.
   *
   * @param authorReference
   *          the author reference
   * @param data
   *          the data
   * @return the document reference
   */
  public DocumentReference createEntryFromCSLItemData(DocumentReference authorReference, CSLItemData data) {
    return service.createEntryFromCSLItemData(authorReference, data);
  }

  /**
   * Find entry reference.
   *
   * @param index
   *          the index
   * @param key
   *          the key
   * @return the document reference
   */
  public DocumentReference findEntryReference(Index index, String key) {
    return service.findEntryReference(index, key);
  }

  /**
   * Find entry reference on wiki.
   *
   * @param wikiReference
   *          the wiki reference
   * @param key
   *          the key
   * @return the document reference
   */
  public DocumentReference findEntryReferenceOnWiki(WikiReference wikiReference, String key) {
    return service.findEntryReferenceOnWiki(wikiReference, key);
  }

  /**
   * Gets the document referencing entry.
   *
   * @param entryId
   *          the entry id
   * @return the document referencing entry
   * @throws WikiManagerException
   *           the wiki manager exception
   * @throws QueryException
   *           the query exception
   * @throws XWikiException
   *           the x wiki exception
   */
  public Map<String, List<DocumentReference>> getDocumentReferencingEntry(String entryId)
      throws WikiManagerException, QueryException, XWikiException {
    return service.getDocumentReferencingEntry(entryId);
  }

  /**
   * Gets the document walker.
   *
   * @return the document walker
   */
  public DocumentWalker getDocumentWalker() {
    return service.getDocumentWalker();
  }

  /**
   * Gets the documents referencing an entry. Since two entries on two different
   * wikis can share the same id, the list from other wiki can not be considered
   * as reliable.
   *
   * @param personRef
   *          the person ref
   * @return the entry referencing A person
   * @throws WikiManagerException
   *           the wiki manager exception
   * @throws QueryException
   *           the query exception
   * @throws XWikiException
   *           the x wiki exception
   */
  public Map<String, List<DocumentReference>> getEntryReferencingAPerson(String personRef)
      throws WikiManagerException, QueryException, XWikiException {
    return service.getEntryReferencingAPerson(personRef);
  }

  /**
   * Gets the errors.
   *
   * @return the errors
   */
  public List<Error> getErrors() {
    return service.getErrors();
  }

  /**
   * Gets the new annotation reference.
   *
   * @param entry
   *          the entry
   * @return the new annotation reference
   */
  public DocumentReference getNewAnnotationReference(DocumentReference entry) {
    return service.getNewAnnotationReference(entry);
  }

  /**
   * Gets the new attachment reference.
   *
   * @param entry
   *          the entry
   * @return the new attachment reference
   */
  public DocumentReference getNewAttachmentReference(DocumentReference entry) {
    return service.getNewAttachmentReference(entry);
  }

  /**
   * Gets the new entry reference.
   *
   * @return the new entry reference
   */
  public DocumentReference getNewEntryReference() {
    return service.getNewEntryReference();
  }

  /**
   * Gets the new person reference.
   *
   * @return the new person reference
   */
  public DocumentReference getNewPersonReference() {
    return service.getNewPersonReference();
  }

  /**
   * Merge persons.
   *
   * @param source
   *          the source
   * @param destination
   *          the destination
   * @return true, if successful
   */
  public boolean mergePersons(String source, String destination) {
    return service.mergePersons(source, destination);
  }

  /**
   * Parses the bib te X.
   *
   * @param bibtex
   *          the bibtex
   * @return the list
   */
  public List<CSLItemData> parseBibTeX(String bibtex) {
    return service.parseBibTeX(bibtex);
  }

  /**
   * Validate entry.
   *
   * @param doc
   *          the doc
   * @return null if successfull, the error code otherwise
   */
  public String validateEntry(XWikiDocument doc) {
    return service.validateEntry(doc);
  }

}
