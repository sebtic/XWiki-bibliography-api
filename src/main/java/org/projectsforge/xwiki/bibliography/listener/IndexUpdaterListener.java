package org.projectsforge.xwiki.bibliography.listener;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.projectsforge.xwiki.bibliography.mapping.Index;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * An EventListener used to monitor document creation, deletion and update to
 * trigger update on the index.
 *
 */
@Component
@Singleton
@Named("IndexUpdaterListener")
public class IndexUpdaterListener implements EventListener {

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
    return Arrays.<Event> asList(new DocumentCreatedEvent(), new DocumentUpdatedEvent(), new DocumentDeletedEvent());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.observation.EventListener#getName()
   */
  @Override
  public String getName() {
    return IndexUpdaterListener.class.getName();
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

    Index index = bibliographyService.findIndex(document);

    if (index != null && document.getXObject(Index.getClassReference(document)) == null) {
      // a document with an associated index but not holding the index itself
      // has been found
      // we need to expire the index and save the change.
      // on save, the index will be updated.
      index.setExpired(true);
      index.save();
    }
  }

}
