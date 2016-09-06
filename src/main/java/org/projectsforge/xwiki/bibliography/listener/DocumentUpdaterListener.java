package org.projectsforge.xwiki.bibliography.listener;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.projectsforge.xwiki.bibliography.mapping.Annotation;
import org.projectsforge.xwiki.bibliography.mapping.Attachment;
import org.projectsforge.xwiki.bibliography.mapping.DocumentWalker.Node;
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
 * @see DocumentUpdaterEvent
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

    Node node = bibliographyService.getDocumentWalker().wrapNode(document);

    // IndexClass update
    if (node.isIndex()) {
      // it's an index, expire it then update
      Index index = node.wrapAsIndex();
      index.setExpired(true);
      index.update();
    }

    // PersonClass update
    if (document.getXObject(Person.CLASS_REFERENCE) != null) {
      new Person(node).update();
    }

    // EntryClass update
    if (document.getXObject(Entry.CLASS_REFERENCE) != null) {
      new Entry(node).update();
    }

    // AnnotationClass update
    if (document.getXObject(Annotation.CLASS_REFERENCE) != null) {
      new Annotation(node).update();
    }

    // AttachmentClass update
    if (document.getXObject(Attachment.CLASS_REFERENCE) != null) {
      new Attachment(node).update();
    }

    if (document.isNew()) {
      if (node.getRootNode().isIndex() && node.getParent() != null) {
        int last = Integer.MIN_VALUE;
        for (Node child : node.getParent().getChildren()) {
          int order = child.getOrder();
          if (order > last && order != Integer.MAX_VALUE) {
            last = order;
          }
        }
        if (last == Integer.MIN_VALUE) {
          node.setOrder(0);
        } else {
          node.setOrder(last + 1);
        }
      }
    }
  }

}
