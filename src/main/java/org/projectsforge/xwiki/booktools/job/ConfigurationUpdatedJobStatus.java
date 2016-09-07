package org.projectsforge.xwiki.booktools.job;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.LoggerManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.ObservationManager;

/**
 * The Class ConfigurationUpdatedJobStatus.
 */
public class ConfigurationUpdatedJobStatus extends DefaultJobStatus<ConfigurationUpdatedJobRequest> {

  /** The updated entries. */
  private List<DocumentReference> updatedEntries = new ArrayList<>();

  /**
   * Instantiates a new configuration updated job status.
   *
   * @param request
   *          the request
   * @param parentJobStatus
   *          the parent job status
   * @param observationManager
   *          the observation manager
   * @param loggerManager
   *          the logger manager
   */
  public ConfigurationUpdatedJobStatus(ConfigurationUpdatedJobRequest request, JobStatus parentJobStatus,
      ObservationManager observationManager, LoggerManager loggerManager) {
    super(request, parentJobStatus, observationManager, loggerManager);
  }

  /**
   * Gets the updated entries.
   *
   * @return the updated entries
   */
  public List<DocumentReference> getUpdatedEntries() {
    return updatedEntries;
  }
}
