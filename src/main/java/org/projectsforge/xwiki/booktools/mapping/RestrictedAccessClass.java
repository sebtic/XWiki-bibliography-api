package org.projectsforge.xwiki.booktools.mapping;

import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.projectsforge.xwiki.booktools.Utils;
import org.projectsforge.xwiki.booktools.mapping.DocumentWalker.Node;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;

/**
 * The Class RestrictedAccessClass.
 */
public abstract class RestrictedAccessClass {

  /** The node. */
  private Node node;

  /** The xobject. */
  protected BaseObject xobject;

  /**
   * Instantiates a new restricted access class.
   *
   * @param node
   *          the node
   * @param classReference
   *          the class reference
   */
  public RestrictedAccessClass(Node node, EntityReference classReference) {
    this.node = node;
    this.xobject = node.getXObject(classReference, true);
  }

  /**
   * Update some fields (triggered when the document is saved through events).
   */
  public void update() {
    // remove existing rights
    node.removeXObjects(XWikiRightServiceImpl.RIGHTCLASS_REFERENCE);

    boolean allowGuest = xobject.getIntValue("allowGuest", 0) == 1;

    if (!allowGuest) {
      // deny guest access
      BaseObject denyObject = node.newXObject(XWikiRightServiceImpl.RIGHTCLASS_REFERENCE);
      denyObject.setLargeStringValue("users", XWikiRightService.GUEST_USER_FULLNAME);
      denyObject.setStringValue("levels", "view");
      denyObject.setIntValue("allow", 0);
    }

    {
      // allow BookToolsAdminGroup to view and edit
      // we explicitly add this inherited rights to disallow access to any
      // other group if not explicitly specified
      BaseObject allowObject = node.newXObject(XWikiRightServiceImpl.RIGHTCLASS_REFERENCE);
      allowObject.setLargeStringValue("groups", "XWiki.BookToolsAdminGroup");
      allowObject.setStringValue("levels", "view,edit");
      allowObject.setIntValue("allow", 1);
    }

    {
      // allow view to specified group list
      String grantedGroups = xobject.getLargeStringValue("groups");
      if (StringUtils.isNotBlank(grantedGroups)) {
        BaseObject allowObject = node.newXObject(XWikiRightServiceImpl.RIGHTCLASS_REFERENCE);
        allowObject.setLargeStringValue("groups", grantedGroups);
        allowObject.setStringValue("levels", "view");
        allowObject.setIntValue("allow", 1);
      }
    }

    {
      // allow view to specified users list and author
      StringJoiner users = new StringJoiner(",");
      String author = Utils.LOCAL_REFERENCE_SERIALIZER.serialize(node.getXWikiDocument().getAuthorReference());
      if (!XWikiRightService.SUPERADMIN_USER_FULLNAME.equals(author)) {
        users.add(author);
      }
      String contentAuthor = Utils.LOCAL_REFERENCE_SERIALIZER
          .serialize(node.getXWikiDocument().getContentAuthorReference());
      if (!XWikiRightService.SUPERADMIN_USER_FULLNAME.equals(contentAuthor)) {
        users.add(contentAuthor);
      }
      users.add(xobject.getLargeStringValue("users"));
      String grantedUsers = users.toString();
      if (StringUtils.isNotBlank(grantedUsers)) {
        BaseObject allowObject = node.newXObject(XWikiRightServiceImpl.RIGHTCLASS_REFERENCE);
        allowObject.setLargeStringValue("groups", grantedUsers);
        allowObject.setStringValue("levels", "view");
        allowObject.setIntValue("allow", 1);
      }
    }
  }
}
