package org.projectsforge.xwiki.bibliography.listener;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.projectsforge.xwiki.bibliography.mapping.Annotation;
import org.projectsforge.xwiki.bibliography.mapping.Attachment;
import org.projectsforge.xwiki.bibliography.mapping.Entry;
import org.projectsforge.xwiki.bibliography.mapping.Index;
import org.projectsforge.xwiki.bibliography.mapping.Person;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * An EventListener used to monitor document creation, deletion and update to
 * update bibliographic data (do modification before storage on disk).
 *
 */
@Component
@Singleton
@Named("DocumentUpdaterListener")
public class DocumentUpdaterListener implements EventListener {

  /** The bibliography service. */
  @Inject
  private BibliographyService bibliographyService;

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.observation.EventListener#getEvents()
   */
  @Override
  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentCreatingEvent(), new DocumentUpdatingEvent(), new DocumentDeletingEvent());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.observation.EventListener#getName()
   */
  @Override
  public String getName() {
    return DocumentUpdaterListener.class.getName();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.xwiki.observation.EventListener#onEvent(org.xwiki.observation.event.
   * Event, java.lang.Object, java.lang.Object)
   */
  @Override
  public void onEvent(Event event, Object sourceDocument, Object sourceContext) {
    // this method is called before the document is saved to the database
    XWikiDocument document = (XWikiDocument) sourceDocument;

    // IndexClass update
    if (document.getXObject(Index.getClassReference(document)) != null) {
      // it's an index, expire it then update
      Index index = new Index(bibliographyService, document);
      index.setExpired(true);
      index.update();
    }

    // PersonClass update
    if (document.getXObject(Person.getClassReference(document)) != null) {
      new Person(bibliographyService, document).update();
    }

    // EntryClass update
    if (document.getXObject(Entry.getClassReference(document)) != null) {
      new Entry(bibliographyService, document).update();
    }

    // AnnotationClass update
    if (document.getXObject(Annotation.getClassReference(document)) != null) {
      new Annotation(bibliographyService, document).update();
    }

    // AttachmentClass update
    if (document.getXObject(Attachment.getClassReference(document)) != null) {
      new Attachment(bibliographyService, document).update();
    }
  }

}
