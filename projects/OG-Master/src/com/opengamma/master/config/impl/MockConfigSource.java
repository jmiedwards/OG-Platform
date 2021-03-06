/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.time.Instant;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;

/**
 * A simple mutable implementation of a source of configuration documents.
 * <p>
 * This class is intended for testing scenarios.
 * It is not thread-safe and must not be used in production.
 */
public class MockConfigSource extends MasterConfigSource {
  // this is public to allow testing

  /**
   * The configuration documents keyed by identifier.
   */
  private final Map<ObjectId, ConfigDocument<?>> _configs = Maps.newHashMap();
  /**
   * The next index for the identifier.
   */
  private final UniqueIdSupplier _uniqueIdSupplier;

  /**
   * Creates the instance.
   */
  public MockConfigSource() {
    super(new InMemoryConfigMaster());
    _uniqueIdSupplier = new UniqueIdSupplier("Mock");
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> search(ConfigSearchRequest<T> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getType(), "request.type");
    Pattern matchName = RegexUtils.wildcardsToPattern(request.getName());
    List<T> result = new ArrayList<T>();
    for (ConfigDocument<?> doc : _configs.values()) {
      if (matchName.matcher(doc.getName()).matches() && request.getType().isInstance(doc.getValue())) {
        result.add((T) doc.getValue());
      }
    }
    return result;
  }

  @Override
  public <T> T getConfig(Class<T> clazz, UniqueId uniqueId) {
    return getDocument(clazz, uniqueId).getValue();
  }

  @Override
  public <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    return getDocument(clazz, objectId, versionCorrection).getValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Collection<? extends T> getConfigs(Class<T> clazz, String configName, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(configName, "configName");
    Pattern matchName = RegexUtils.wildcardsToPattern(configName);
    Collection<T> result = new ArrayList<T>();
    for (ConfigDocument<?> doc : _configs.values()) {
      if (matchName.matcher(doc.getName()).matches() && clazz.isInstance(doc.getValue())) {
        result.add((T) doc.getValue());
      }
    }
    return result;
  }

  @Override
  public <T> T getLatestByName(final Class<T> clazz, final String name) {
    return getByName(clazz, name, null);
  }

  @Override
  public <T> T getByName(final Class<T> clazz, final String name, final Instant versionAsOf) {
    ConfigDocument<T> doc = getDocumentByName(clazz, name, versionAsOf);
    return doc == null ? null : doc.getValue();
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> getDocument(Class<T> clazz, UniqueId uniqueId) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ConfigDocument<T> config = (ConfigDocument<T>) _configs.get(uniqueId.getObjectId());
    if (config != null && clazz.isInstance(config.getValue())) {
      return config;
    }
    throw new DataNotFoundException("Configuration not found: " + uniqueId);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> getDocument(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ConfigDocument<T> config = (ConfigDocument<T>) _configs.get(objectId);
    if (config != null && clazz.isInstance(config.getValue())) {
      return config;
    }
    throw new DataNotFoundException("Configuration not found: " + objectId);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> getDocumentByName(final Class<T> clazz, final String name, final Instant versionAsOf) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(name, "name");
    Pattern matchName = RegexUtils.wildcardsToPattern(name);
    for (ConfigDocument<?> doc : _configs.values()) {
      if (matchName.matcher(doc.getName()).matches() && clazz.isInstance(doc.getValue())) {
        return (ConfigDocument<T>) doc;
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a config document to the master.
   * 
   * @param configDoc  the config document to add, not null
   */
  public void add(ConfigDocument<?> configDoc) {
    ArgumentChecker.notNull(configDoc, "doc");
    UniqueId uniqueId = _uniqueIdSupplier.get();
    configDoc.setUniqueId(uniqueId);
    _configs.put(uniqueId.getObjectId(), configDoc);
  }

}
