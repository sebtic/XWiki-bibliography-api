package org.projectsforge.xwiki.bibliography.job;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.bibliography.mapping.Entry;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;
import org.xwiki.component.annotation.Component;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.JobGroupPath;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * The Class ConfigurationUpdatedJob.
 */
@Component
@Named(ConfigurationUpdatedJob.JOB_TYPE)
public class ConfigurationUpdatedJob
    extends AbstractJob<ConfigurationUpdatedJobRequest, DefaultJobStatus<ConfigurationUpdatedJobRequest>>
    implements GroupedJob {

  /** The Constant JOB_TYPE. */
  public static final String JOB_TYPE = "bibliography-configuration-updated";

  /** The document reference resolver. */
  @Inject
  private DocumentReferenceResolver<String> documentReferenceResolver;

  /** The query manager. */
  @Inject
  private QueryManager queryManager;

  /** The service. */
  @Inject
  private BibliographyService service;

  /** The context provider. */
  @Inject
  private Provider<XWikiContext> contextProvider;

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
            .createQuery(String.format("from doc.object(%s) as entry", Entry.getClassReferenceAsString()), Query.XWQL)
            .setWiki(StringUtils.defaultIfBlank(wikiReference.getName(), null));
        results = query.execute();
        if (results == null) {
          results = Collections.emptyList();
        }
      } catch (QueryException ex) {
        logger.warn("An error occurred while executing the query", ex);
      }

      XWikiContext context = contextProvider.get();
      XWiki wiki = context.getWiki();

      progressManager.pushLevelProgress(results.size(), this);
      try {
        for (String result : results) {
          DocumentReference entryRef = documentReferenceResolver.resolve(result);
          try {
            XWikiDocument entryDoc = wiki.getDocument(entryRef, context);
            new Entry(service, entryDoc).update();
            wiki.saveDocument(entryDoc, context);
          } catch (Exception ex) {
            logger.warn("An error occurred while updating " + entryRef, ex);
          }
        }
      } finally {
        progressManager.popLevelProgress(this);
      }
    } catch (Exception ex) {
      logger.warn("An error occurred while updating ", ex);
    }
  }

}
