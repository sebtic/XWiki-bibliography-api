package org.projectsforge.xwiki.booktools.job;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.mapping.Entry;
import org.projectsforge.xwiki.booktools.service.BookToolsService;
import org.xwiki.component.annotation.Component;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.JobGroupPath;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

/**
 * The Class ConfigurationUpdatedJob.
 */
@Component
@Named(ConfigurationUpdatedJob.JOB_TYPE)
public class ConfigurationUpdatedJob
    extends AbstractJob<ConfigurationUpdatedJobRequest, DefaultJobStatus<ConfigurationUpdatedJobRequest>>
    implements GroupedJob {

  /** The Constant JOB_TYPE. */
  public static final String JOB_TYPE = "booktools-configuration-updated";

  /** The document reference resolver. */
  @Inject
  private DocumentReferenceResolver<String> documentReferenceResolver;

  /** The query manager. */
  @Inject
  private QueryManager queryManager;

  /** The service. */
  @Inject
  private BookToolsService service;

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.job.GroupedJob#getGroupPath()
   */
  @Override
  public JobGroupPath getGroupPath() {
    String wiki = this.request.getWikiReference().getName();
    return new JobGroupPath(Arrays.asList(JOB_TYPE, wiki));
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.job.Job#getType()
   */
  @Override
  public String getType() {
    return JOB_TYPE;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.xwiki.job.AbstractJob#runInternal()
   */
  @Override
  protected void runInternal() throws Exception {
    try {
      WikiReference wikiReference = request.getWikiReference();
      List<String> results = Collections.emptyList();
      try {
        Query query = queryManager
            .createQuery(String.format("from doc.object(%s) as entry", Entry.CLASS_REFERENCE_AS_STRING), Query.XWQL)
            .setWiki(StringUtils.defaultIfBlank(wikiReference.getName(), null));
        results = query.execute();
        if (results == null) {
          results = Collections.emptyList();
        }
      } catch (QueryException ex) {
        logger.warn("An error occurred while executing the query", ex);
      }

      progressManager.pushLevelProgress(results.size(), this);
      try {
        for (String result : results) {
          service.getDocumentWalker().getNode(documentReferenceResolver.resolve(result)).wrapAsEntry().update();
        }
      } finally {
        progressManager.popLevelProgress(this);
      }
    } catch (Exception ex) {
      logger.warn("An error occurred while updating ", ex);
    }
  }

}
