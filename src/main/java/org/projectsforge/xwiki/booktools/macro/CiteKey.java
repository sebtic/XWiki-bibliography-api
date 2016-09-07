package org.projectsforge.xwiki.booktools.macro;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class CiteKey.
 */
public class CiteKey {

  /**
   * Decode the keys with their locator.
   *
   * @param keys
   *          the keys
   * @return the list
   */
  public static List<CiteKey> decode(String keys) {
    List<CiteKey> results = new ArrayList<>();
    if (keys != null) {
      for (String element : keys.split(",")) {
        CiteKey ck = new CiteKey(element);
        if (!ck.getKey().isEmpty()) {
          results.add(ck);
        }
      }
    }
    return results;
  }

  /**
   * Decode unique keys.
   *
   * @param keys
   *          the keys
   * @return the list
   */
  public static List<String> decodeUniqueKeys(String keys) {
    Set<String> known = new HashSet<>();
    List<String> results = new ArrayList<>();

    if (keys != null) {
      for (CiteKey ck : decode(keys)) {
        if (!known.contains(ck.getKey())) {
          known.add(ck.getKey());
          results.add(ck.getKey());
        }
      }
    }
    return results;
  }

  /** The key. */
  private String key;

  /** The locator. */
  private String locator;

  /**
   * Instantiates a new cite key.
   *
   * @param keyWithLocator
   *          the key with locator
   */
  public CiteKey(String keyWithLocator) {
    String value = keyWithLocator.trim();
    int pos = value.indexOf('[');
    if (pos == -1) {
      key = value;
      locator = "";
    } else {
      key = value.substring(0, pos).trim();
      locator = value.substring(pos + 1, value.length() - 1).trim();
    }
  }

  /**
   * Gets the key.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Gets the locator.
   *
   * @return the locator
   */
  public String getLocator() {
    return locator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CiteKey [key=" + key + ", locator=" + locator + "]";
  }

}
