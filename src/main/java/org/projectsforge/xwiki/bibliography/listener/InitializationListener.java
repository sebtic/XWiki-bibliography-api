package org.projectsforge.xwiki.bibliography.listener;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.projectsforge.xwiki.bibliography.service.BibliographyService;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.event.ComponentDescriptorAddedEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

/**
 * An EventListener used to ensure requirements are met.
 *
 */
@Component
@Singleton
@Named("InitializationListener")
public class InitializationListener implements EventListener {

  @Inject
  private BibliographyService service;

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.observation.EventListener#getEvents()
   */
  @Override
  public List<Event> getEvents() {
    return Arrays.<Event> asList(new ComponentDescriptorAddedEvent());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.observation.EventListener#getName()
   */
  @Override
  public String getName() {
    return InitializationListener.class.getName();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.xwiki.observation.EventListener#onEvent(org.xwiki.observation.event.
   * Event, java.lang.Object, java.lang.Object)
   */
  @Override
  public void onEvent(Event event, Object componentManager, Object componentDescriptor) {

    if (event instanceof ComponentDescriptorAddedEvent) {
      ComponentDescriptorAddedEvent initEvent = (ComponentDescriptorAddedEvent) event;

      if (initEvent.getRoleHint().contains("Bibliography.Code.ApplicationPanelEntry")) {
        service.ensureRequirements();
      }
    }
  }

}
