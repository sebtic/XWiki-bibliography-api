package org.projectsforge.xwiki.bibliography.mapping;

import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.bibliography.Utils;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;

/**
 * The Class RestrictedAccessClass.
 */
public abstract class RestrictedAccessClass {

  /** The service. */
  protected BibliographyService service;

  /** The document. */
  protected XWikiDocument document;

  /** The xobject. */
  protected BaseObject xobject;

  /**
   * Instantiates a new restricted access class.
   *
   * @param service
   *          the service
   * @param document
   *          the document
   * @param xobject
   *          the xobject
   */
  public RestrictedAccessClass(BibliographyService service, XWikiDocument document, BaseObject xobject) {
    this.service = service;
    this.document = document;
    this.xobject = xobject;
  }

  /**
   * Update some fields (triggered when the document is saved through events).
   */
  public void update() {
    try {
      // remove existing rights
      document.removeXObjects(XWikiRightServiceImpl.RIGHTCLASS_REFERENCE);

      boolean allowGuest = xobject.getIntValue("allowGuest", 0) == 1;

      if (!allowGuest) {
        // deny guest access
        BaseObject denyObject = document.newXObject(XWikiRightServiceImpl.RIGHTCLASS_REFERENCE, service.getContext());
        denyObject.setLargeStringValue("users", XWikiRightService.GUEST_USER_FULLNAME);
        denyObject.setStringValue("levels", "view");
        denyObject.setIntValue("allow", 0);
      }

      {
        // allow BibliographyAdminGroup to view and edit
        // we explicitly add this inherited rights to disallow access to any
        // other group if not explicitly specified
        BaseObject allowObject = document.newXObject(XWikiRightServiceImpl.RIGHTCLASS_REFERENCE, service.getContext());
        allowObject.setLargeStringValue("groups", "XWiki.BibliographyAdminGroup");
        allowObject.setStringValue("levels", "view,edit");
        allowObject.setIntValue("allow", 1);
      }

      {
        // allow view to specified group list
        String grantedGroups = xobject.getLargeStringValue("groups");
        if (StringUtils.isNotBlank(grantedGroups)) {
          BaseObject allowObject = document.newXObject(XWikiRightServiceImpl.RIGHTCLASS_REFERENCE,
              service.getContext());
          allowObject.setLargeStringValue("groups", grantedGroups);
          allowObject.setStringValue("levels", "view");
          allowObject.setIntValue("allow", 1);
        }
      }

      {
        // allow view to specified users list and author
        StringJoiner users = new StringJoiner(",");
        String author = Utils.LOCAL_REFERENCE_SERIALIZER.serialize(document.getAuthorReference());
        if (!XWikiRightService.SUPERADMIN_USER_FULLNAME.equals(author)) {
          users.add(author);
        }
        String contentAuthor = Utils.LOCAL_REFERENCE_SERIALIZER.serialize(document.getContentAuthorReference());
        if (!XWikiRightService.SUPERADMIN_USER_FULLNAME.equals(contentAuthor)) {
          users.add(contentAuthor);
        }
        users.add(xobject.getLargeStringValue("users"));
        String grantedUsers = users.toString();
        if (StringUtils.isNotBlank(grantedUsers)) {
          BaseObject allowObject = document.newXObject(XWikiRightServiceImpl.RIGHTCLASS_REFERENCE,
              service.getContext());
          allowObject.setLargeStringValue("groups", grantedUsers);
          allowObject.setStringValue("levels", "view");
          allowObject.setIntValue("allow", 1);
        }
      }
    } catch (XWikiException ex) {
      service.getLogger().warn("An error occurred", ex);
    }
  }
}
