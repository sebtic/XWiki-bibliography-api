/*
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.projectsforge.xwiki.booktools.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.Constants;
import org.projectsforge.xwiki.booktools.Error;
import org.projectsforge.xwiki.booktools.Utils;
import org.projectsforge.xwiki.booktools.biblatex.BibLaTeXImporter;
import org.projectsforge.xwiki.booktools.fields.CSLDateFields;
import org.projectsforge.xwiki.booktools.fields.CSLNameFields;
import org.projectsforge.xwiki.booktools.fields.CSLStringFields;
import org.projectsforge.xwiki.booktools.mapping.Configuration;
import org.projectsforge.xwiki.booktools.mapping.DocumentWalker;
import org.projectsforge.xwiki.booktools.mapping.Entry;
import org.projectsforge.xwiki.booktools.mapping.Index;
import org.projectsforge.xwiki.booktools.mapping.LocalIndex;
import org.projectsforge.xwiki.booktools.mapping.Person;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;
import org.xwiki.wiki.manager.WikiManagerException;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ListItemDataProvider;
import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLName;

/**
 * Implementation of a <tt>BookToolsService</tt> component.
 */
@Component
public class DefaultBookToolsService implements BookToolsService {

  /** The Constant DOCUMENT_WALKER. */
  private static final String DOCUMENT_WALKER = "booktools-document-walker";

  /** The id regex. */
  private static Pattern ID_REGEX = Pattern.compile("^[a-zA-Z\\.0-9:\\-_]{2,50}$");

  /** The Constant XWIKI_GROUPS_CLASS. */
  private static final EntityReference XWIKI_GROUPS_CLASS = new EntityReference("XWikiGroups", EntityType.DOCUMENT,
      new EntityReference("XWiki", EntityType.SPACE));

  /** The biblatex importer. */
  private BibLaTeXImporter biblatexImporter = new BibLaTeXImporter();

  /** The context provider. */
  @Inject
  private Provider<XWikiContext> contextProvider;

  /** The document reference resolver. */
  @Inject
  private DocumentReferenceResolver<String> documentReferenceResolver;

  /** The logger. */
  @Inject
  private Logger logger;

  /** The query manager. */
  @Inject
  private QueryManager queryManager;

  /** The wiki descriptor manager. */
  @Inject
  private WikiDescriptorManager wikiDescriptorManager;

  @Inject
  private AuthorizationManager authorizationManager;

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.BookToolsService#addError(java.lang
   * .String)
   */
  @Override
  public void addError(String id, Object... params) {
    @SuppressWarnings("unchecked")
    List<Error> list = (List<Error>) getContext().get(Constants.CONTEXT_BIBLIOGRAPHY_ERROR);
    if (list == null) {
      list = new ArrayList<>();
      getContext().put(Constants.CONTEXT_BIBLIOGRAPHY_ERROR, list);
    }
    list.add(new Error(id, params));
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.BookToolsService#clearError()
   */
  @Override
  public void clearErrors() {
    @SuppressWarnings("unchecked")
    List<Error> list = (List<Error>) getContext().get(Constants.CONTEXT_BIBLIOGRAPHY_ERROR);
    if (list != null) {
      list.clear();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.BookToolsService#
   * createEntryFromCSLItemData(de.undercouch.citeproc.csl.CSLItemData)
   */
  @Override
  public synchronized DocumentReference createEntryFromCSLItemData(DocumentReference authorReference,
      CSLItemData data) {

    if (!ID_REGEX.matcher(data.getId()).matches()) {
      addError(Error.INVALID_ID_FORMAT, data.getId());
      return null;
    }

    Entry entry = getDocumentWalker().getNode(getNewEntryReference()).wrapAsEntry();
    entry.fillFromCSLObject(authorReference, data);
    entry.getNode().save();
    return entry.getNode().getDocumentReference();

  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.BookToolsService#
   * createPersonFromCSLName(de.undercouch.citeproc.csl.CSLName)
   */
  @Override
  public synchronized DocumentReference createPersonFromCSLName(DocumentReference authorReference, CSLName name) {
    Person person = getDocumentWalker().getNode(getNewPersonReference()).wrapAsPerson();
    person.fillFromCSLObject(name);
    person.getNode().save();
    return person.getNode().getDocumentReference();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * ensureRequirements()
   */
  @Override
  public void ensureRequirements() {
    XWikiContext context = getContext();
    XWiki wiki = context.getWiki();

    DocumentReference adminGroupRef = new DocumentReference(context.getWikiId(), "XWiki", "BookToolsAdminGroup");
    if (!wiki.exists(adminGroupRef, context)) {
      // the group does not exist, then we need to create it
      try {
        XWikiDocument adminGroupDoc = wiki.getDocument(adminGroupRef, context);
        adminGroupDoc.setHidden(true);
        adminGroupDoc.newXObject(XWIKI_GROUPS_CLASS, context);
        wiki.saveDocument(adminGroupDoc, context);
      } catch (XWikiException ex) {
        logger.warn("An error occurred", ex);
      }
    }

  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.BookToolsService#findEntry(org.
   * projectsforge.xwiki.booktools.Index, java.lang.String)
   */
  @Override
  public Entry findEntry(Index index, String key) {
    DocumentReference docRef = findEntryReference(index, key);
    if (docRef != null) {
      return getDocumentWalker().getNode(docRef).wrapAsEntry();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.BookToolsService#findEntryReference
   * (org.projectsforge.xwiki.booktools.Index, java.lang.String)
   */
  @Override
  public DocumentReference findEntryReference(Index index, String key) {
    DocumentReference reference = findEntryReferenceOnWiki(index.getNode().getDocumentReference().getWikiReference(),
        key);
    if (reference != null) {
      return reference;
    }
    for (String wikiName : index.getExtraWikiSources()) {
      if (StringUtils.isNotBlank(wikiName)) {
        reference = findEntryReferenceOnWiki(new WikiReference(wikiName), key);
        if (reference != null) {
          break;
        }
      }
    }
    return reference;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * findEntryReferenceOnWiki(org.xwiki.model.reference.WikiReference,
   * java.lang.String)
   */
  @Override
  public DocumentReference findEntryReferenceOnWiki(WikiReference wikiReference, String key) {
    try {
      Query query = queryManager
          .createQuery(
              String.format("from doc.object(%s) as entry where entry.id = :key", Entry.CLASS_REFERENCE_AS_STRING),
              Query.XWQL)
          .bindValue("key", StringUtils.trim(key)).setWiki(StringUtils.defaultIfBlank(wikiReference.getName(), null))
          .setLimit(1);
      List<String> results = query.execute();
      logger.debug("findEntryReferenceOnWiki {} ({}) => {}", key, wikiReference, results);
      if (results != null && !results.isEmpty()) {
        if (results.size() > 1) {
          logger.warn("Multiple bibliographic entry for key {} on wiki {} : {}", key, wikiReference.getName(), results);
        }
        return documentReferenceResolver.resolve(results.get(0), wikiReference);
      }
    } catch (QueryException ex) {
      logger.warn("An error occurred while executing the query", ex);
      addError(Error.QUERY, ex.getMessage());
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * findPersonFromCSLNameOnWiki(org.xwiki.model.reference.WikiReference,
   * de.undercouch.citeproc.csl.CSLName)
   */
  @Override
  public DocumentReference findPersonFromCSLName(WikiReference wikiReference, CSLName name) {
    try {
      List<String> results = queryManager
          .createQuery(String.format(
              "from doc.object(%s) as person where " + "person.family = :family and " + "person.given = :given and "
                  + "person.droppingParticle = :droppingParticle and "
                  + "person.nonDroppingParticle = :nonDroppingParticle and " + "person.suffix = :suffix",
              Person.CLASS_REFERENCE_AS_STRING), Query.XWQL)
          .bindValue("family", StringUtils.defaultString(name.getFamily()))
          .bindValue("given", StringUtils.defaultString(name.getGiven()))
          .bindValue("droppingParticle", StringUtils.defaultString(name.getDroppingParticle()))
          .bindValue("nonDroppingParticle", StringUtils.defaultString(name.getNonDroppingParticle()))
          .bindValue("suffix", StringUtils.defaultString(name.getSuffix()))
          .setWiki(wikiReference == null ? null : wikiReference.getName()).execute();
      if (results.size() > 1) {
        logger.warn("Multiple identical Person found for ({}) : {}", Utils.serializeCSLName(name), results);
      }
      if (!results.isEmpty()) {
        return documentReferenceResolver.resolve(results.get(0), wikiReference);
      }
    } catch (QueryException ex) {
      addError(Error.QUERY, ex.getMessage());
      logger.warn("An error occurred while querying database", ex);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.BookToolsService#getContext()
   */
  @Override
  public XWikiContext getContext() {
    return contextProvider.get();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.BookToolsService#getCSL(org.
   * projectsforge.xwiki.booktools.Index, java.lang.String)
   */
  @Override
  public CSL getCSL(Index index) {
    try {
      List<CSLItemData> itemDatas = index.getEntries();

      // build CSL object from CSLItemData with current locale
      CSL csl = new CSL(new ListItemDataProvider(itemDatas.toArray(new CSLItemData[0])), index.getBibliographyStyle(),
          getContext().getLocale().toString());

      // build the list of all keys in order and register their usage
      List<String> keys = new ArrayList<>();
      itemDatas.forEach(e -> keys.add(e.getId()));
      csl.registerCitationItems(keys.toArray(new String[0]), false);

      // build the citation in order to produce a proper numbering including all
      // keys
      for (String key : keys) {
        csl.makeCitation(new CSLCitation(new CSLCitationItem(key)));
      }

      csl.setConvertLinks(true);
      csl.setOutputFormat("text");
      return csl;
    } catch (IOException ex) {
      addError(Error.CSL, ex.getMessage());
      logger.warn("Can not create CSL instance", ex);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * getDefaultConfiguration(org.xwiki.model.reference.WikiReference)
   */
  @Override
  public Configuration getDefaultConfiguration(WikiReference wikiReference) {
    DocumentReference docRef = new DocumentReference(wikiReference.getName(),
        Constants.CONFIGURATION_SPACE_NAME_AS_LIST, "Configuration");
    return getDocumentWalker().getNode(docRef).wrapAsConfiguration();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * getDocumentReferencingEntry(java.lang.String)
   */
  @Override
  public Map<String, List<DocumentReference>> getDocumentReferencingEntry(String entryId) {
    Map<String, List<DocumentReference>> results = new HashMap<>();
    try {
      for (String wikiId : wikiDescriptorManager.getAllIds()) {
        WikiReference wikiReference = new WikiReference(wikiId);
        List<DocumentReference> referencing = new ArrayList<>();

        List<String> docs = queryManager
            .createQuery(String.format("from doc.object(%s) as localindex", LocalIndex.CLASS_REFERENCE_AS_STRING),
                Query.XWQL)
            .setWiki(wikiId).execute();
        if (docs == null) {
          docs = Collections.emptyList();
        }

        DocumentWalker documentWalker = getDocumentWalker();

        for (String docId : docs) {
          DocumentReference docRef = documentReferenceResolver.resolve(docId, wikiReference);
          List<String> keys = documentWalker.getNode(docRef).wrapAsLocalIndex(null).getKeys();
          if (keys.contains(entryId) && !referencing.contains(docRef)) {
            referencing.add(docRef);
          }
        }
        if (!referencing.isEmpty()) {
          results.put(wikiId, referencing);
        }
      }
    } catch (WikiManagerException | QueryException ex) {
      logger.warn("An error occurred", ex);
    }
    return results;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * getDocumentWalker()
   */
  @Override
  public synchronized DocumentWalker getDocumentWalker() {
    XWikiContext context = getContext();
    DocumentWalker result = (DocumentWalker) context.get(DOCUMENT_WALKER);
    if (result == null) {
      result = new DocumentWalker(this, documentReferenceResolver, queryManager, authorizationManager);
    }
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * getEntryReferencingAPerson(java.lang.String)
   */
  @Override
  public Map<String, List<DocumentReference>> getEntryReferencingAPerson(String personRef) {

    XWikiContext context = getContext();

    // ensure the reference is local to the wiki
    String personId = Utils.LOCAL_REFERENCE_SERIALIZER
        .serialize(documentReferenceResolver.resolve(personRef, context.getWikiReference()));

    Map<String, List<DocumentReference>> results = new HashMap<>();
    try {
      for (String wikiId : wikiDescriptorManager.getAllIds()) {
        WikiReference wikiReference = new WikiReference(wikiId);

        List<DocumentReference> referencing = new ArrayList<>();

        List<String> entries = queryManager
            .createQuery(String.format("from doc.object(%s) as entry", Entry.CLASS_REFERENCE_AS_STRING), Query.XWQL)
            .setWiki(wikiId).execute();
        if (entries == null) {
          entries = Collections.emptyList();
        }

        for (String entryId : entries) {
          Entry entry = getDocumentWalker().getNode(documentReferenceResolver.resolve(entryId, wikiReference))
              .wrapAsEntry();

          for (CSLNameFields field : CSLNameFields.values()) {
            if (field.decode(entry).contains(personId)
                && !referencing.contains(entry.getNode().getDocumentReference())) {
              referencing.add(entry.getNode().getDocumentReference());
            }
          }
        }
        if (!referencing.isEmpty()) {
          results.put(wikiId, referencing);
        }
      }
    } catch (WikiManagerException | QueryException ex) {
      logger.warn("An error occurred", ex);
    }
    return results;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.BookToolsService#getErrors()
   */
  @Override
  public List<Error> getErrors() {
    @SuppressWarnings("unchecked")
    List<Error> list = (List<Error>) getContext().get(Constants.CONTEXT_BIBLIOGRAPHY_ERROR);
    if (list == null) {
      list = Collections.emptyList();
    }
    return list;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#getLogger(
   * )
   */
  @Override
  public Logger getLogger() {
    return logger;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * getNewAnnotationReference(org.xwiki.model.reference.DocumentReference)
   */
  @Override
  public DocumentReference getNewAnnotationReference(DocumentReference entry) {
    XWikiContext context = getContext();
    XWiki xwiki = context.getWiki();

    int counter = 0;
    DocumentReference docRef;
    do {
      counter++;
      docRef = documentReferenceResolver.resolve("Annotation-" + counter, entry);
    } while (xwiki.exists(docRef, context));
    return docRef;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * getNewAttachmentReference(org.xwiki.model.reference.DocumentReference)
   */
  @Override
  public DocumentReference getNewAttachmentReference(DocumentReference entry) {
    XWikiContext context = getContext();
    XWiki xwiki = context.getWiki();

    int counter = 0;
    DocumentReference docRef;
    do {
      counter++;
      docRef = documentReferenceResolver.resolve("Attachment-" + counter, entry);
    } while (xwiki.exists(docRef, context));
    return docRef;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * getNewEntryReference()
   */
  @Override
  public DocumentReference getNewEntryReference() {
    XWikiContext context = getContext();
    List<String> results = null;
    try {
      results = queryManager
          .createQuery(String.format("from doc.object(%s) as entry", Entry.CLASS_REFERENCE_AS_STRING), Query.XWQL)
          .setWiki(context.getWikiId()).execute();
    } catch (QueryException ex) {
      addError(Error.QUERY, ex.getMessage());
      logger.warn("An error occurred while executing query ", ex);
    }

    int counter = 0;
    if (results != null) {
      for (String id : results) {
        if (id.startsWith(Entry.NAME_PREFIX) && id.endsWith(Entry.NAME_SUFFIX)) {
          String number = id.substring(0, id.length() - Entry.NAME_SUFFIX.length())
              .substring(Entry.NAME_PREFIX.length());
          try {
            counter = Math.max(counter, Integer.parseInt(number));
          } catch (NumberFormatException ex) {
            logger.warn("Can not extract number", ex);
          }
        }
      }
    }
    counter++;
    return documentReferenceResolver.resolve(Entry.NAME_PREFIX + counter + Entry.NAME_SUFFIX,
        context.getWikiReference());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * getNewPersonReference()
   */
  @Override
  public DocumentReference getNewPersonReference() {
    XWikiContext context = getContext();
    List<String> results = null;
    try {
      results = queryManager
          .createQuery(String.format("from doc.object(%s) as person", Person.CLASS_REFERENCE_AS_STRING), Query.XWQL)
          .setWiki(context.getWikiId()).execute();
    } catch (QueryException ex) {
      addError(Error.QUERY, ex.getMessage());
      logger.warn("An error occurred while executing query ", ex);
    }

    int counter = 0;
    if (results != null) {
      for (String id : results) {
        if (id.startsWith(Person.NAME_PREFIX) && id.endsWith(Person.NAME_SUFFIX)) {
          String number = id.substring(0, id.length() - Person.NAME_SUFFIX.length())
              .substring(Person.NAME_PREFIX.length());
          try {
            counter = Math.max(counter, Integer.parseInt(number));
          } catch (NumberFormatException ex) {
            logger.warn("Can not extract number", ex);
          }
        }
      }
    }
    counter++;
    return documentReferenceResolver.resolve(Person.NAME_PREFIX + counter + Person.NAME_SUFFIX,
        context.getWikiReference());
  }

  /**
   * Gets the person.
   *
   * @param personRef
   *          the person ref
   * @return the person
   */
  public Person getPerson(DocumentReference personRef) {
    return getDocumentWalker().getNode(personRef).wrapIfPerson();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.BookToolsService#getPerson(java.
   * lang.String)
   */
  @Override
  public Person getPerson(String reference) {
    XWikiContext context = getContext();
    return getPerson(documentReferenceResolver.resolve(reference, context.getWikiReference()));
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * mergePersons(java.lang.String, java.lang.String)
   */
  @Override
  public boolean mergePersons(String source, String destination) {
    try {
      XWikiContext context = getContext();
      XWiki wiki = context.getWiki();
      logger.debug("MergePersons {} => {}", source, destination);

      DocumentReference destinationRef = documentReferenceResolver.resolve(destination,
          getContext().getWikiReference());

      // check that the destination exists and is a person
      if (!wiki.exists(destinationRef, context)) {
        return false;
      }
      if (getDocumentWalker().getNode(destinationRef).getXObject(Entry.CLASS_REFERENCE) == null) {
        return false;
      }

      List<String> entries = queryManager
          .createQuery(String.format("from doc.object(%s) as entry", Entry.CLASS_REFERENCE_AS_STRING), Query.XWQL)
          .setWiki(context.getWikiId()).execute();
      if (entries == null) {
        entries = Collections.emptyList();
      }

      for (String entryId : entries) {
        Entry entry = getDocumentWalker()
            .getNode(documentReferenceResolver.resolve(entryId, context.getWikiReference())).wrapAsEntry();
        boolean dirty = false;

        for (CSLNameFields field : CSLNameFields.values()) {
          List<String> persons = field.decode(entry);
          int index = persons.indexOf(source);
          if (index != -1) {
            persons.set(index, destination);
            field.encode(entry, persons);
            dirty = true;
          }
        }
        if (dirty) {
          entry.getNode().save();
        }
      }
      return true;
    } catch (QueryException ex) {
      logger.warn("An error occurred", ex);
      return false;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.BookToolsService#parseBibTeX(com.
   * xpn.xwiki.XWikiContext, java.lang.String)
   */
  @Override
  public List<CSLItemData> parseBibTeX(String bibtex) {
    return biblatexImporter.parseBibTeX(this, bibtex);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.booktools.service.BookToolsService#
   * validateEntry(com.xpn.xwiki.doc.XWikiDocument)
   */
  @Override
  public String validateEntry(XWikiDocument doc) {
    BaseObject xobject = doc.getXObject(Entry.CLASS_REFERENCE, true, getContext());
    // check for non duplicate id on the current wiki
    String id = xobject.getStringValue(CSLStringFields.ID.toString());

    logger.debug("validateEntryId '{}'", id);

    if (StringUtils.isBlank(id)) {
      addError(Error.EMPTY_ID);
      return Error.EMPTY_ID;
    }

    if (!ID_REGEX.matcher(id).matches()) {
      addError(Error.INVALID_ID_FORMAT, id);
      return Error.INVALID_ID_FORMAT;
    }

    DocumentReference docRefFromId = findEntryReferenceOnWiki(doc.getDocumentReference().getWikiReference(), id);
    if (docRefFromId == null) {
      // it is a creation and we are sure that id is unique
    } else {
      // either it is a creation with a conflicting id or it is an update
      if (doc.getDocumentReference().equals(docRefFromId)) {
        // it is the same document, with the same key => no problem
      } else {
        // two different document with the same key => there is a problem
        addError(Error.ID_ALREADY_EXISTS, id);
        return Error.ID_ALREADY_EXISTS;
      }
    }

    // check date fields
    for (CSLDateFields dateField : CSLDateFields.values()) {
      String value = xobject.getStringValue(dateField.name());
      if (!dateField.isValid(value)) {
        addError(Error.INVALID_DATE, dateField, value);
        return Error.INVALID_DATE;
      }
    }

    logger.debug("validateEntryId '{}' OK", id);
    return null;
  }

}
