package org.projectsforge.xwiki.booktools.mapping;

import org.projectsforge.xwiki.booktools.Constants;
import org.projectsforge.xwiki.booktools.mapping.DocumentWalker.Node;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.objects.BaseObject;

/**
 * The Class Annotation.
 */
public class Order {

  /** The Constant CLASS_REFERENCE. */
  public static final EntityReference CLASS_REFERENCE = new EntityReference("OrderClass", EntityType.DOCUMENT,
      Constants.CODE_SPACE_REFERENCE);

  /** The Constant CLASS_REFERENCE_AS_STRING. */
  public static final String CLASS_REFERENCE_AS_STRING = Constants.CODE_SPACE_NAME_AS_STRING + ".OrderClass";

  /** The Constant FIELD_ORDER. */
  private static final String FIELD_ORDER = "order";

  /** The node. */
  private Node node;

  /** The xobject. */
  private BaseObject xobject;

  /**
   * Instantiates a new order.
   *
   * @param node
   *          the node
   */
  public Order(Node node) {
    this.node = node;
    this.xobject = node.getXObject(CLASS_REFERENCE);
  }

  /**
   * Gets the order.
   *
   * @return the order
   */
  public Integer getOrder() {
    if (xobject == null) {
      return Integer.MAX_VALUE;
    } else {
      return xobject.getIntValue(FIELD_ORDER, Integer.MAX_VALUE);
    }
  }

  /**
   * Sets the order.
   *
   * @param order
   *          the new order
   */
  public void setOrder(int order) {
    if (xobject == null) {

      xobject = node.newXObject(CLASS_REFERENCE);
    }
    xobject.setIntValue(FIELD_ORDER, order);
  }

}
