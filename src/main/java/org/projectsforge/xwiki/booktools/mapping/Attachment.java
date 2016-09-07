package org.projectsforge.xwiki.booktools.mapping;

import org.projectsforge.xwiki.booktools.Constants;
import org.projectsforge.xwiki.booktools.mapping.DocumentWalker.Node;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

/**
 * The Class Annotation.
 */
public class Attachment extends RestrictedAccessClass {

  /** The Constant CLASS_REFERENCE. */
  public static final EntityReference CLASS_REFERENCE = new EntityReference("AttachmentClass", EntityType.DOCUMENT,
      Constants.CODE_SPACE_REFERENCE);

  /** The Constant CLASS_REFERENCE_AS_STRING. */
  public static final String CLASS_REFERENCE_AS_STRING = Constants.CODE_SPACE_NAME_AS_STRING + ".AttachmentClass";

  /**
   * Instantiates a new attachment.
   *
   * @param node
   *          the node
   */
  public Attachment(Node node) {
    super(node, CLASS_REFERENCE);
  }
}
