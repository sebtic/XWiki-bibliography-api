package org.projectsforge.xwiki.bibliography.listener;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.projectsforge.xwiki.bibliography.mapping.Person;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;
import org.xwiki.bridge.event.DocumentDeletingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * An EventListener used to ensure data integrity.
 *
 */
@Component
@Singleton
@Named("IntegrityListener")
public class IntegrityListener implements EventListener {

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
    return Arrays.<Event> asList(new DocumentDeletingEvent());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.observation.EventListener#getName()
   */
  @Override
  public String getName() {
    return IntegrityListener.class.getName();
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

    // PersonClass update
    if (document.getXObject(Person.getClassReference(document)) != null) {
      if (!bibliographyService.getEntryReferencingAPerson(document.getDocumentReference().toString()).isEmpty()) {
        ((DocumentDeletingEvent) event).cancel();
      }
    }
  }

}
