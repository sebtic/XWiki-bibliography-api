package org.projectsforge.xwiki.bibliography.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectsforge.xwiki.bibliography.Error;
import org.projectsforge.xwiki.bibliography.Utils;
import org.projectsforge.xwiki.bibliography.service.BibliographyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.google.common.base.Objects;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * The Class DocumentWalker.
 */
public class DocumentWalker {

  /**
   * The Class Node.
   */
  public class Node implements Comparable<Node> {

    /** The children. */
    private List<Node> children;

    /** The document. */
    private XWikiDocument document;

    /** The document reference. */
    private DocumentReference documentReference;

    /** The order. */
    private Integer order;

    /**
     * Instantiates a new node.
     *
     * @param documentReference
     *          the document reference
     */
    Node(DocumentReference documentReference) {
      this.documentReference = documentReference;
    }

    /**
     * Instantiates a new node.
     *
     * @param document
     *          the document
     */
    Node(XWikiDocument document) {
      this.documentReference = document.getDocumentReference();
      this.document = document;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Node other) {
      int result = getOrder().compareTo(other.getOrder());
      if (result == 0) {
        result = getDocumentReference().compareTo(other.getDocumentReference());
      }
      return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      Node other = (Node) obj;
      if (documentReference == null) {
        if (other.documentReference != null) {
          return false;
        }
      } else if (!documentReference.equals(other.documentReference)) {
        return false;
      }
      return true;
    }

    /**
     * Gets the children.
     *
     * @return the children
     */
    public List<Node> getChildren() {
      return getChildren(false);
    }

    /**
     * Gets the children.
     *
     * @param includeHidden
     *          include hidden document and space ?
     * @return the children
     */
    public List<Node> getChildren(boolean includeHidden) {
      if (children == null) {
        if ("WebHome".equals(getDocumentReference().getName())) {
          XWikiContext context = service.getContext();

          List<String> results = new ArrayList<>();

          String spaceName = Utils.LOCAL_REFERENCE_SERIALIZER.serialize(getDocumentReference().getLastSpaceReference());

          try {
            // the children which are not nested spaces
            results.addAll(queryManager
                .createQuery(
                    "select distinct doc.fullName from Document doc where doc.name <> 'WebHome' and doc.space = :space and doc.hidden = :hidden",
                    Query.XWQL)
                .bindValue("space", spaceName).bindValue("hidden", includeHidden ? 1 : 0).setWiki(context.getWikiId())
                .execute());
            // the children which are nested spaces
            for (String space : queryManager
                .createQuery(
                    "select distinct space.reference from Space space where space.parent = :space and doc.hidden = :hidden",
                    Query.XWQL)
                .bindValue("space", spaceName).bindValue("hidden", includeHidden ? 1 : 0).setWiki(context.getWikiId())
                .<String> execute()) {
              results.add(space + ".WebHome");
            }
          } catch (QueryException ex) {
            logger.warn("An error occurred while querying children of " + documentReference, ex);
          }

          children = new ArrayList<>();
          for (String result : results) {
            Node node = getNode(documentReferenceResolver.resolve(result, context.getWikiReference()));

            children.add(node);
          }
          Collections.sort(children);
        } else {
          // we are not the space WebHome => we dont have children
          children = Collections.emptyList();
        }
      }
      return children;
    }

    /**
     * Gets the document.
     *
     * @return the document
     */
    public Document getDocument() {
      return new Document(getXWikiDocument(), service.getContext());
    }

    /**
     * Gets the document reference.
     *
     * @return the document reference
     */
    public DocumentReference getDocumentReference() {
      return documentReference;
    }

    /**
     * Gets the next.
     *
     * @return the next
     */
    public Node getNext() {
      if (getChildren().isEmpty()) {
        // no children => get next sibling
        Node current = this;
        Node result = null;
        while (current.getParent() != null && (result = current.getNextSibling()) == null) {
          current = current.getParent();
        }
        return result;
      } else {
        return getChildren().get(0);
      }
    }

    /**
     * Gets the next sibling.
     *
     * @return the next sibling
     */
    public Node getNextSibling() {
      if (getParent() != null) {
        List<Node> siblings = getParent().getChildren();
        for (int i = 0; i < siblings.size(); ++i) {
          if (Objects.equal(siblings.get(i).getDocumentReference(), documentReference) && (i + 1 < siblings.size())) {
            // I found my position in the siblings
            return siblings.get(i + 1);
          }
        }
      }
      return null;
    }

    /**
     * Gets the nodes to root.
     *
     * @return the nodes to root
     */
    public List<Node> getNodesToRoot() {
      List<Node> results = new ArrayList<>();

      Node current = this;
      results.add(this);
      while (!current.isRootNode()) {
        current = current.getParent();
        if (current == null || results.contains(current)) {
          // loop detected
          break;
        }
        results.add(current);
      }

      return results;
    }

    /**
     * Gets the order.
     *
     * @return the order
     */
    public Integer getOrder() {
      if (order == null) {
        order = new Order(this).getOrder();
      }
      return order;
    }

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    public Node getParent() {
      EntityReference parentSpace = getDocumentReference().getLastSpaceReference().getParent();
      if (parentSpace.getType() == EntityType.SPACE) {
        DocumentReference parentRef = new DocumentReference(
            new EntityReference("WebHome", EntityType.DOCUMENT, parentSpace));
        return getNode(parentRef);
      } else {
        // there is no parent space
        return null;
      }
    }

    /**
     * Gets the previous.
     *
     * @return the previous
     */
    public Node getPrevious() {
      if (getParent() != null) {
        Node result = getPreviousSibling();
        if (result != null) {
          return result;
        }
      }
      return getParent();
    }

    /**
     * Gets the previous sibling.
     *
     * @return the previous sibling
     */
    public Node getPreviousSibling() {
      if (getParent() != null) {
        List<Node> siblings = getParent().getChildren();
        for (int i = 0; i < siblings.size(); ++i) {
          if (Objects.equal(siblings.get(i).getDocumentReference(), documentReference) && (i - 1 >= 0)) {
            // I found my position in the siblings
            return siblings.get(i - 1);
          }
        }
      }
      return null;
    }

    /**
     * Gets the root node.
     *
     * @return the root node
     */
    public Node getRootNode() {
      List<Node> results = getNodesToRoot();
      return results.get(results.size() - 1);
    }

    /**
     * Gets the service.
     *
     * @return the service
     */
    public BibliographyService getService() {
      return service;
    }

    /**
     * Gets the tree.
     *
     * @return the tree
     */
    public List<Node> getTree() {
      List<Node> results = new ArrayList<>();
      results.add(this);
      for (Node child : getChildren()) {
        results.addAll(child.getTree());
      }
      return results;
    }

    /**
     * Gets the x object.
     *
     * @param classReference
     *          the class reference
     * @return the x object
     */
    public BaseObject getXObject(EntityReference classReference) {
      return getXObject(classReference, false);
    }

    /**
     * Gets the x object.
     *
     * @param classReference
     *          the class reference
     * @param create
     *          the create
     * @return the x object
     */
    public BaseObject getXObject(EntityReference classReference, boolean create) {
      return getXWikiDocument().getXObject(classReference, create, service.getContext());
    }

    /**
     * Gets the x wiki document.
     *
     * @return the x wiki document
     */
    XWikiDocument getXWikiDocument() {
      if (document == null) {
        XWikiContext context = service.getContext();
        try {
          this.document = context.getWiki().getDocument(documentReference, context);
        } catch (XWikiException ex) {
          logger.warn("An error occurred while loading " + documentReference, ex);
        }
      }
      return document;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return (documentReference == null) ? 0 : documentReference.hashCode();
    }

    /**
     * Checks if is index.
     *
     * @return true, if is index
     */
    public boolean isIndex() {
      return getXWikiDocument().getXObject(Index.CLASS_REFERENCE) != null;
    }

    /**
     * Checks if is root node.
     *
     * @return true, if is root node
     */
    public boolean isRootNode() {
      return isIndex() || getParent() == null;
    }

    /**
     * Move as child.
     *
     * @param child
     *          the child
     * @return true, if successful
     */
    public boolean moveAsChild(Node child) {
      if (getChildren().contains(child)) {
        return true;
      }

      if (!"WebHome".equals(getDocumentReference().getName())) {
        // parent is not a space, we can not move page
        return false;
      }

      String newName = child.getDocumentReference().getName();
      if ("WebHome".equals(newName)) {
        // it is the main page of a space => we prefix with the last space name
        // as
        // the new name
        newName = child.getDocumentReference().getLastSpaceReference().getName() + ".WebHome";
      }
      DocumentReference newDocumentReference = documentReferenceResolver.resolve(newName, getDocumentReference());

      try {
        child.getXWikiDocument().rename(newDocumentReference, service.getContext());
        child.getXWikiDocument().setParentReference(getDocumentReference());
        child.documentReference = newDocumentReference;
        child.children = null;
        child.save();
        nodes.remove(child.getDocumentReference());
        nodes.put(newDocumentReference, child);
      } catch (XWikiException ex) {
        logger.warn(
            "An error occurred while adding " + child.getDocumentReference() + " as child of " + getDocumentReference(),
            ex);
        return false;
      }

      children = null;
      return true;
    }

    /**
     * New X object.
     *
     * @param classReference
     *          the class reference
     * @return the base object
     */
    public BaseObject newXObject(EntityReference classReference) {
      try {
        return getXWikiDocument().newXObject(classReference, service.getContext());
      } catch (XWikiException ex) {
        service.getLogger().warn("An error occurred", ex);
        return null;
      }
    }

    /**
     * Removes the X objects.
     *
     * @param classReference
     *          the class reference
     * @return true, if successful
     */
    public boolean removeXObjects(EntityReference classReference) {
      return getXWikiDocument().removeXObjects(classReference);
    }

    /**
     * Save.
     */
    public void save() {
      XWikiContext context = service.getContext();
      try {
        context.getWiki().saveDocument(getXWikiDocument(), context);
      } catch (XWikiException ex) {
        service.addError(Error.SAVE_DOCUMENT, document.getDocumentReference());
        logger.warn("An error occurred while saving document " + document.getDocumentReference(), ex);
      }
    }

    /**
     * Sets the children.
     *
     * @param newChildren
     *          the new children
     */
    public void setChildren(List<Node> newChildren) {
      for (Node child : newChildren) {
        moveAsChild(child);
      }
      List<Node> allChildren = getChildren();
      for (Node node : allChildren) {
        node.setOrder(Integer.MAX_VALUE);
      }
      int counter = 0;
      for (Node node : newChildren) {
        node.setOrder(counter++);
      }
      // save changes
      for (Node node : allChildren) {
        node.save();
      }
      children = null;
    }

    /**
     * Sets the order.
     *
     * @param order
     *          the new order
     */
    public void setOrder(int order) {
      new Order(this).setOrder(order);
      this.order = order;
    }

    @Override
    public String toString() {
      return documentReference == null ? "null" : documentReference.toString();
    }

    /**
     * Wrap as configuration.
     *
     * @return the configuration
     */
    public Configuration wrapAsConfiguration() {
      return new Configuration(this);
    }

    /**
     * Wrap as entry.
     *
     * @return the entry
     */
    public Entry wrapAsEntry() {
      return new Entry(this);
    }

    /**
     * Wrap as index.
     *
     * @return the index
     */
    public Index wrapAsIndex() {
      return new Index(this);
    }

    /**
     * Wrap as local index.
     *
     * @param index
     *          the index
     * @return the local index
     */
    public LocalIndex wrapAsLocalIndex(Index index) {
      return new LocalIndex(this, index);
    }

    /**
     * Wrap as person.
     *
     * @return the person
     */
    public Person wrapAsPerson() {
      return new Person(this);
    }

    /**
     * Wrap if entry.
     *
     * @return the entry
     */
    public Entry wrapIfEntry() {
      if (getXObject(Entry.CLASS_REFERENCE) != null) {
        return new Entry(this);
      } else {
        return null;
      }
    }

    /**
     * Wrap if index.
     *
     * @return the index
     */
    public Index wrapIfIndex() {
      if (isIndex()) {
        return wrapAsIndex();
      } else {
        return null;
      }
    }

    /**
     * Wrap if local index.
     *
     * @param index
     *          the index
     * @return the local index
     */
    public LocalIndex wrapIfLocalIndex(Index index) {
      if (getXObject(LocalIndex.CLASS_REFERENCE) != null) {
        return new LocalIndex(this, index);
      } else {
        return null;
      }
    }

    /**
     * Wrap if person.
     *
     * @return the person
     */
    public Person wrapIfPerson() {
      if (getXObject(Person.CLASS_REFERENCE) != null) {
        return new Person(this);
      } else {
        return null;
      }
    }

  }

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(DocumentWalker.class);

  /** The document reference resolver. */
  private DocumentReferenceResolver<String> documentReferenceResolver;

  /** The nodes. */
  private Map<DocumentReference, Node> nodes = new HashMap<>();

  /** The query manager. */
  private QueryManager queryManager;

  /** The service. */
  private BibliographyService service;

  /**
   * Instantiates a new document walker.
   *
   * @param service
   *          the service
   * @param documentReferenceResolver
   *          the document reference resolver
   * @param queryManager
   *          the query manager
   */
  public DocumentWalker(BibliographyService service, DocumentReferenceResolver<String> documentReferenceResolver,
      QueryManager queryManager) {
    this.service = service;
    this.documentReferenceResolver = documentReferenceResolver;
    this.queryManager = queryManager;
  }

  /**
   * Gets the node.
   *
   * @param documentReference
   *          the document reference
   * @return the node
   */
  public Node getNode(DocumentReference documentReference) {
    if (documentReference == null) {
      return null;
    }

    Node node = nodes.get(documentReference);
    if (node == null) {
      node = new Node(documentReference);
      nodes.put(documentReference, node);
    }
    return node;
  }

  /**
   * Wrap node.
   *
   * @param document
   *          the document
   * @return the node
   */
  public Node wrapNode(XWikiDocument document) {
    if (document == null) {
      return null;
    }
    Node node = new Node(document);
    nodes.put(document.getDocumentReference(), node);
    return node;
  }

}
