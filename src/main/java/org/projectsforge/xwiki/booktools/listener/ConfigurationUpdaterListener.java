package org.projectsforge.xwiki.booktools.listener;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.projectsforge.xwiki.booktools.job.ConfigurationUpdatedJob;
import org.projectsforge.xwiki.booktools.job.ConfigurationUpdatedJobRequest;
import org.projectsforge.xwiki.booktools.mapping.Configuration;
import org.projectsforge.xwiki.booktools.service.BookToolsService;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.job.JobExecutor;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * An EventListener used to monitor configuration update to update bibliographic
 * data.
 *
 * @see ConfigurationUpdaterEvent
 */
@Component
@Singleton
@Named("ConfigurationUpdaterListener")
public class ConfigurationUpdaterListener implements EventListener {

  /** The job executor. */
  @Inject
  private JobExecutor jobExecutor;

  /** The booktools service. */
  @Inject
  private BookToolsService service;

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.observation.EventListener#getEvents()
   */
  @Override
  public List<Event> getEvents() {
    return Arrays.<Event> asList(new DocumentUpdatedEvent());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.observation.EventListener#getName()
   */
  @Override
  public String getName() {
    return ConfigurationUpdaterListener.class.getName();
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

    // ConfigurationClass update
    if (document.getXObject(Configuration.CLASS_REFERENCE) != null) {
      // a configuration has been saved => we trigger an update of the entries
      // to be up to date with the configuration

      ConfigurationUpdatedJobRequest request = new ConfigurationUpdatedJobRequest();
      request.setId(Arrays.asList(ConfigurationUpdatedJob.JOB_TYPE, UUID.randomUUID().toString()));
      request.setInteractive(false);
      request.setVerbose(true);
      request.setWikiReference(document.getDocumentReference().getWikiReference());

      try {
        this.jobExecutor.execute(ConfigurationUpdatedJob.JOB_TYPE, request);
      } catch (Exception ex) {
        service.getLogger().warn("An error occurred", ex);
      }
    }
  }

}
