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
package org.projectsforge.xwiki.bibliography.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.bibliography.Constants;
import org.projectsforge.xwiki.bibliography.Error;
import org.projectsforge.xwiki.bibliography.Utils;
import org.projectsforge.xwiki.bibliography.biblatex.BibLaTeXImporter;
import org.projectsforge.xwiki.bibliography.fields.CSLDateFields;
import org.projectsforge.xwiki.bibliography.fields.CSLNameFields;
import org.projectsforge.xwiki.bibliography.fields.CSLStringFields;
import org.projectsforge.xwiki.bibliography.mapping.Configuration;
import org.projectsforge.xwiki.bibliography.mapping.Entry;
import org.projectsforge.xwiki.bibliography.mapping.Index;
import org.projectsforge.xwiki.bibliography.mapping.LocalIndex;
import org.projectsforge.xwiki.bibliography.mapping.Person;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
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
 * Implementation of a <tt>BibliographyService</tt> component.
 */
@Component
public class DefaultBibliographyService implements BibliographyService {

  /** The id regex. */
  private static Pattern ID_REGEX = Pattern.compile("^[a-zA-Z0-9:-_]{2,50}$");

  /** The logger. */
  @Inject
  private Logger logger;

  /** The document reference resolver. */
  @Inject
  private DocumentReferenceResolver<String> documentReferenceResolver;

  /** The query manager. */
  @Inject
  private QueryManager queryManager;

  /** The context provider. */
  @Inject
  private Provider<XWikiContext> contextProvider;

  /** The biblatex importer. */
  private BibLaTeXImporter biblatexImporter = new BibLaTeXImporter();

  /** The wiki descriptor manager. */
  @Inject
  private WikiDescriptorManager wikiDescriptorManager;

  /*
   * (non-Javadoc)
   *
   * @see
   * org.projectsforge.xwiki.bibliography.BibliographyService#addError(java.lang
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
   * @see org.projectsforge.xwiki.bibliography.BibliographyService#clearError()
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
   * @see org.projectsforge.xwiki.bibliography.BibliographyService#
   * createEntryFromCSLItemData(de.undercouch.citeproc.csl.CSLItemData)
   */
  @Override
  public synchronized DocumentReference createEntryFromCSLItemData(CSLItemData data) {

    if (!ID_REGEX.matcher(data.getId()).matches()) {
      addError(Error.INVALID_ID_FORMAT, data.getId());
      return null;
    }

    XWikiContext context = getContext();
    XWiki xwiki = context.getWiki();
    DocumentReference docRef = getNewEntryReference();
    XWikiDocument document;
    try {
      document = xwiki.getDocument(docRef, context);
      document.setAuthorReference(context.getUserReference());
      document.setContentAuthorReference(context.getUserReference());
    } catch (XWikiException ex) {
      addError(Error.XWIKI_GET_DOCUMENT, docRef);
      logger.warn("An error occurred while creating entry", ex);
      return null;
    }
    Entry entry = new Entry(this, document);
    entry.fillFromCSLObject(data);
    entry.save();
    return document.getDocumentReference();

  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.BibliographyService#
   * createPersonFromCSLName(de.undercouch.citeproc.csl.CSLName)
   */
  @Override
  public synchronized DocumentReference createPersonFromCSLName(CSLName name) {
    XWikiContext context = getContext();
    XWiki xwiki = context.getWiki();
    DocumentReference docRef = getNewPersonReference();
    XWikiDocument document;
    try {
      document = xwiki.getDocument(docRef, context);
      document.setAuthorReference(context.getUserReference());
      document.setContentAuthorReference(context.getUserReference());
    } catch (XWikiException ex) {
      addError(Error.XWIKI_GET_DOCUMENT, docRef);
      logger.warn("An error occurred while creating person", ex);
      return null;
    }
    Person person = new Person(this, document);
    person.fillFromCSLObject(name);
    person.save();
    return document.getDocumentReference();
  }

  @Override
  public void ensureRequirements() {
    XWikiContext context = getContext();
    XWiki wiki = context.getWiki();

    DocumentReference xwikiGroupsRef = new DocumentReference(context.getWikiId(), "XWiki", "XWikiGroups");

    DocumentReference adminGroupRef = new DocumentReference(context.getWikiId(), "XWiki", "BibliographyAdminGroup");
    if (!wiki.exists(adminGroupRef, context)) {
      // the group does not exist, then we need to create it
      try {
        XWikiDocument adminGroupDoc = wiki.getDocument(adminGroupRef, context);
        adminGroupDoc.newXObject(xwikiGroupsRef, context);
        wiki.saveDocument(adminGroupDoc, context);
      } catch (XWikiException ex) {
        logger.warn("An error occurred", ex);
      }
    }

    DocumentReference userGroupRef = new DocumentReference(context.getWikiId(), "XWiki", "BibliographyUserGroup");
    if (!wiki.exists(userGroupRef, context)) {
      // the group does not exist, then we need to create it
      try {
        XWikiDocument userGroupDoc = wiki.getDocument(userGroupRef, context);
        userGroupDoc.newXObject(xwikiGroupsRef, context);
        wiki.saveDocument(userGroupDoc, context);
      } catch (XWikiException ex) {
        logger.warn("An error occurred", ex);
      }
    }

  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.projectsforge.xwiki.bibliography.BibliographyService#findEntry(org.
   * projectsforge.xwiki.bibliography.Index, java.lang.String)
   */
  @Override
  public Entry findEntry(Index index, String key) {
    DocumentReference docRef = findEntryReference(index, key);
    if (docRef != null) {
      XWikiContext context = getContext();
      try {
        return new Entry(this, context.getWiki().getDocument(docRef, context));
      } catch (XWikiException ex) {
        logger.warn("Can not load entry", ex);
        addError(Error.XWIKI_GET_DOCUMENT, docRef);
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.projectsforge.xwiki.bibliography.BibliographyService#findEntryReference
   * (org.projectsforge.xwiki.bibliography.Index, java.lang.String)
   */
  @Override
  public DocumentReference findEntryReference(Index index, String key) {
    DocumentReference reference = findEntryReferenceOnWiki(
        index.getDocument().getDocumentReference().getWikiReference(), key);
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

  /**
   * Find entry reference on wiki.
   *
   * @param wikiReference
   *          the wiki reference
   * @param key
   *          the key
   * @return the document reference
   */
  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
   * findEntryReferenceOnWiki(org.xwiki.model.reference.WikiReference,
   * java.lang.String)
   */
  public DocumentReference findEntryReferenceOnWiki(WikiReference wikiReference, String key) {
    try {
      Query query = queryManager
          .createQuery(
              String.format("from doc.object(%s) as entry where entry.id = :key", Entry.getClassReferenceAsString()),
              Query.XWQL)
          .bindValue("key", StringUtils.trim(key)).setWiki(StringUtils.defaultIfBlank(wikiReference.getName(), null))
          .setLimit(1);
      List<String> results = query.execute();
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
   * @see
   * org.projectsforge.xwiki.bibliography.BibliographyService#findIndex(com.xpn.
   * xwiki.doc.XWikiDocument)
   */
  @Override
  public Index findIndex(XWikiDocument document) {
    Set<DocumentReference> visited = new HashSet<>();
    XWikiDocument curDoc = document;
    XWikiContext context = getContext();
    XWiki xwiki = context.getWiki();

    visited.add(document.getDocumentReference());
    while (true) {
      if (curDoc.getXObject(Index.getClassReference(document)) != null) {
        // index found
        return new Index(this, curDoc);
      }

      // recurse to parent
      DocumentReference parentRef = curDoc.getParentReference();
      if (parentRef == null) {
        // there is no parent
        return null;
      }
      // protect against loop
      if (visited.contains(parentRef)) {
        return null;
      } else {
        visited.add(parentRef);
      }
      try {
        curDoc = xwiki.getDocument(parentRef, context);
      } catch (XWikiException ex) {
        addError(Error.XWIKI_GET_DOCUMENT, parentRef);
        return null;
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
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
              Person.getClassReferenceAsString()), Query.XWQL)
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
   * @see org.projectsforge.xwiki.bibliography.BibliographyService#getContext()
   */
  @Override
  public XWikiContext getContext() {
    return contextProvider.get();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.BibliographyService#getCSL(org.
   * projectsforge.xwiki.bibliography.Index, java.lang.String)
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
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
   * getDefaultConfiguration(org.xwiki.model.reference.WikiReference)
   */
  @Override
  public Configuration getDefaultConfiguration(WikiReference wikiReference) {
    try {
      XWikiContext context = getContext();
      XWikiDocument document = context.getWiki().getDocument(Configuration.getConfigurationPageReference(wikiReference),
          context);
      return new Configuration(this, document);
    } catch (XWikiException ex) {
      logger.warn("Can not load configuration", ex);
      throw new IllegalStateException("Can not load configuration", ex);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
   * getDocumentReferencingEntry(java.lang.String)
   */
  @Override
  public Map<String, List<DocumentReference>> getDocumentReferencingEntry(String entryId) {
    XWikiContext context = getContext();
    XWiki wiki = context.getWiki();
    Map<String, List<DocumentReference>> results = new HashMap<>();
    try {
      for (String wikiId : wikiDescriptorManager.getAllIds()) {
        WikiReference wikiReference = new WikiReference(wikiId);
        List<DocumentReference> referencing = new ArrayList<>();

        List<String> docs = queryManager
            .createQuery(String.format("from doc.object(%s) as localindex", LocalIndex.getClassReferenceAsString()),
                Query.XWQL)
            .setWiki(wikiId).execute();
        if (docs == null) {
          docs = Collections.emptyList();
        }

        for (String docId : docs) {
          DocumentReference docRef = documentReferenceResolver.resolve(docId, wikiReference);
          XWikiDocument doc = wiki.getDocument(docRef, context);
          List<String> keys = new LocalIndex(this, doc, null).getKeys();
          if (keys.contains(entryId)) {
            referencing.add(docRef);
          }
        }
        if (!referencing.isEmpty()) {
          results.put(wikiId, referencing);
        }
      }
    } catch (XWikiException | WikiManagerException | QueryException ex) {
      logger.warn("An error occurred", ex);
    }
    return results;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.projectsforge.xwiki.bibliography.BibliographyService#getEntries(org.
   * projectsforge.xwiki.bibliography.Index)
   */
  @Override
  public List<SortableDocumentReference> getEntries(Index index) {
    WikiReference wikiReference = index.getDocument().getDocumentReference().getWikiReference();
    List<SortableDocumentReference> results = getEntriesOnWiki(wikiReference);
    for (String wikiName : index.getExtraWikiSources()) {
      if (StringUtils.isNotBlank(wikiName)) {
        wikiReference = new WikiReference(wikiName);
        results.addAll(getEntriesOnWiki(wikiReference));
      }
    }
    Collections.sort(results);
    return results;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
   * getEntriesOnWiki(org.xwiki.model.reference.WikiReference)
   */
  @Override
  public List<SortableDocumentReference> getEntriesOnWiki(WikiReference wikiReference) {
    List<SortableDocumentReference> entries = new ArrayList<>();
    XWikiContext context = getContext();
    XWiki xwiki = context.getWiki();
    List<String> results = null;
    try {
      results = queryManager
          .createQuery(String.format("from doc.object(%s) as entry", Entry.getClassReferenceAsString()), Query.XWQL)
          .setWiki(wikiReference.getName()).execute();
    } catch (QueryException ex) {
      logger.warn("An error occurred while executing query ", ex);
    }
    if (results == null) {
      results = Collections.emptyList();
    }
    for (String result : results) {
      DocumentReference docRef = documentReferenceResolver.resolve(result, wikiReference);
      try {
        XWikiDocument doc = xwiki.getDocument(docRef, context);
        entries.add(new SortableDocumentReference(doc.getTitle(), docRef));
      } catch (XWikiException ex) {
        addError(Error.XWIKI_GET_DOCUMENT, docRef);
      }
    }
    return entries;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
   * getEntryReferencingAPerson(java.lang.String)
   */
  @Override
  public Map<String, List<DocumentReference>> getEntryReferencingAPerson(String personRef) {

    XWikiContext context = getContext();
    XWiki wiki = context.getWiki();

    // ensure the reference is local to the wiki
    String personId = Utils.LOCAL_REFERENCE_SERIALIZER
        .serialize(documentReferenceResolver.resolve(personRef, context.getWikiReference()));

    Map<String, List<DocumentReference>> results = new HashMap<>();
    try {
      for (String wikiId : wikiDescriptorManager.getAllIds()) {
        WikiReference wikiReference = new WikiReference(wikiId);

        List<DocumentReference> referencing = new ArrayList<>();

        List<String> entries = queryManager
            .createQuery(String.format("from doc.object(%s) as entry", Entry.getClassReferenceAsString()), Query.XWQL)
            .setWiki(wikiId).execute();
        if (entries == null) {
          entries = Collections.emptyList();
        }

        for (String entryId : entries) {
          DocumentReference entryRef = documentReferenceResolver.resolve(entryId, wikiReference);
          XWikiDocument entryDoc = wiki.getDocument(entryRef, context);
          BaseObject xobject = entryDoc.getXObject(Entry.getClassReference(entryDoc));

          for (CSLNameFields field : CSLNameFields.values()) {
            if (field.decode(xobject).contains(personId)) {
              referencing.add(entryRef);
            }
          }
        }
        if (!referencing.isEmpty()) {
          results.put(wikiId, referencing);
        }
      }
    } catch (XWikiException | WikiManagerException | QueryException ex) {
      logger.warn("An error occurred", ex);
    }
    return results;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
   * getLocalIndex(com.xpn.xwiki.XWikiContext, com.xpn.xwiki.doc.XWikiDocument,
   * org.projectsforge.xwiki.bibliography.mapping.Index)
   */
  /*
   * @Override public LocalIndex getLocalIndex(XWikiDocument document, Index
   * index) { return new LocalIndex(this, document, index); }
   */

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.BibliographyService#getErrors()
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
   * @see
   * org.projectsforge.xwiki.bibliography.service.BibliographyService#getLogger(
   * )
   */
  @Override
  public Logger getLogger() {
    return logger;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
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
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
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
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
   * getNewEntryReference()
   */
  @Override
  public DocumentReference getNewEntryReference() {
    XWikiContext context = getContext();
    List<String> results = null;
    try {
      results = queryManager
          .createQuery(String.format("from doc.object(%s) as entry", Entry.getClassReferenceAsString()), Query.XWQL)
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
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
   * getNewPersonReference()
   */
  @Override
  public DocumentReference getNewPersonReference() {
    XWikiContext context = getContext();
    List<String> results = null;
    try {
      results = queryManager
          .createQuery(String.format("from doc.object(%s) as person", Person.getClassReferenceAsString()), Query.XWQL)
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
    try {
      XWikiContext context = getContext();
      XWikiDocument personDoc = context.getWiki().getDocument(personRef, context);
      return new Person(this, personDoc);
    } catch (XWikiException ex) {
      addError(Error.XWIKI_GET_DOCUMENT, personRef);
      logger.warn("Can not load Person object", ex);
      return null;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.projectsforge.xwiki.bibliography.BibliographyService#getPerson(java.
   * lang.String)
   */
  @Override
  public Person getPerson(String reference) {
    return getPerson(documentReferenceResolver.resolve(reference));
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
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
      XWikiDocument destinationDoc = wiki.getDocument(destinationRef, context);
      if (destinationDoc != null && destinationDoc.getXObject(Entry.getClassReference(destinationDoc)) != null) {
        return false;
      }

      List<String> entries = queryManager
          .createQuery(String.format("from doc.object(%s) as entry", Entry.getClassReferenceAsString()), Query.XWQL)
          .setWiki(context.getWikiId()).execute();
      if (entries == null) {
        entries = Collections.emptyList();
      }

      for (String entryId : entries) {
        DocumentReference entryRef = documentReferenceResolver.resolve(entryId, context.getWikiReference());
        XWikiDocument entryDoc = wiki.getDocument(entryRef, context);
        BaseObject xobject = entryDoc.getXObject(Entry.getClassReference(entryDoc));
        boolean dirty = false;

        for (CSLNameFields field : CSLNameFields.values()) {
          List<String> persons = field.decode(xobject);
          int index = persons.indexOf(source);
          if (index != -1) {
            persons.set(index, destination);
            field.encode(xobject, persons);
            dirty = true;
          }
        }
        if (dirty) {
          wiki.saveDocument(entryDoc, context);
        }
      }
      return true;
    } catch (XWikiException | QueryException ex) {
      logger.warn("An error occurred", ex);
      return false;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.projectsforge.xwiki.bibliography.BibliographyService#parseBibTeX(com.
   * xpn.xwiki.XWikiContext, java.lang.String)
   */
  @Override
  public List<CSLItemData> parseBibTeX(String bibtex) {
    return biblatexImporter.parseBibTeX(this, bibtex);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.projectsforge.xwiki.bibliography.service.BibliographyService#
   * validateEntry(com.xpn.xwiki.doc.XWikiDocument)
   */
  @Override
  public boolean validateEntry(XWikiDocument doc) {
    BaseObject xobject = doc.getXObject(Entry.getClassReference(doc), true, getContext());
    // check for non duplicate id on the current wiki
    String id = xobject.getStringValue(CSLStringFields.ID.toString());

    logger.debug("validateEntryId '{}'", id);

    if (StringUtils.isBlank(id)) {
      addError(Error.EMPTY_ID);
      return false;
    }

    if (!ID_REGEX.matcher(id).matches()) {
      addError(Error.INVALID_ID_FORMAT, id);
      return false;
    }

    DocumentReference docRefFromId = findEntryReferenceOnWiki(doc.getDocumentReference().getWikiReference(), id);
    if (doc.isNew() && docRefFromId != null) {
      addError(Error.ID_ALREADY_EXISTS, id);
      return false;
    }
    if (!doc.isNew()) {
      if (docRefFromId != null && !Objects.equals(doc.getDocumentReference(), docRefFromId)) {
        addError(Error.ID_ALREADY_EXISTS, id);
        return false;
      }
    }

    // check date fields
    for (CSLDateFields dateField : CSLDateFields.values()) {
      String value = xobject.getStringValue(dateField.name());
      if (!dateField.isValid(value)) {
        addError(Error.INVALID_DATE, dateField, value);
        return false;
      }
    }

    logger.debug("validateEntryId '{}' OK", id);
    return true;
  }
}
