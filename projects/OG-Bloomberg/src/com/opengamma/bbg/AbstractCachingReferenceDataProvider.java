/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * A base for implementations of {@link CachingReferenceDataProvider} which can store fields by security.
 */
public abstract class AbstractCachingReferenceDataProvider implements CachingReferenceDataProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractCachingReferenceDataProvider.class);
  /**
   * Constant used when field not available.
   */
  private static final String FIELD_NOT_AVAILABLE_NAME = "NOT_AVAILABLE_FIELD";

  /**
   * The underlying reference data source.
   */
  private final ReferenceDataProvider _underlying;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying reference data provider, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public AbstractCachingReferenceDataProvider(final ReferenceDataProvider underlying, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * 
   * @return the context, not null
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the underlying provider.
   * 
   * @return the underlying, not null
   */
  @Override
  public ReferenceDataProvider getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public ReferenceDataResult getFields(Set<String> securityKeys, Set<String> fields) {
    ArgumentChecker.notEmpty(securityKeys, "securityKeys");
    ArgumentChecker.notEmpty(fields, "fields");
    
    Map<String, PerSecurityReferenceDataResult> cachedResults = loadCachedResults(securityKeys);
    // Key is the set of field names, value is the securities that require that exact
    // set of fields.
    Map<Set<String>, Set<String>> securityKeysByFields = determineSecuritiesForFieldSets(cachedResults, securityKeys, fields);
    ReferenceDataResult resolvedResults = loadAndPersistUnknownFields(cachedResults, securityKeysByFields);
    resolvedResults = stripUnwantedFields(resolvedResults, fields);
    return resolvedResults;
  }

  protected ReferenceDataResult stripUnwantedFields(final ReferenceDataResult resolvedResults, final Set<String> fields) {
    ReferenceDataResult result = new ReferenceDataResult();
    Set<String> securityKeys = resolvedResults.getSecurities();
    for (String securityKey : securityKeys) {
      PerSecurityReferenceDataResult unstippedDataResult = resolvedResults.getResult(securityKey);
      PerSecurityReferenceDataResult strippedDataResult = new PerSecurityReferenceDataResult(securityKey);
      strippedDataResult.getExceptions().addAll(unstippedDataResult.getExceptions());
      MutableFudgeMsg strippedFields = getFudgeContext().newMessage();
      FudgeMsg unstrippedFieldData = unstippedDataResult.getFieldData();
      // check requested fields
      for (String requestField : fields) {
        List<FudgeField> fudgeFields = unstrippedFieldData.getAllByName(requestField);
        for (FudgeField fudgeField : fudgeFields) {
          strippedFields.add(requestField, fudgeField.getValue());
        }
      }
      strippedDataResult.setFieldData(strippedFields);
      result.addResult(strippedDataResult);
    }
    return result;
  }

  public void refresh(Set<String> securityKeys) {
    // TODO bulk queries
    Map<String, PerSecurityReferenceDataResult> cachedResults = loadCachedResults(securityKeys);
    
    Map<Set<String>, Set<String>> securitiesByFields = Maps.newHashMap();
    
    for (String securityKey : securityKeys) {
      PerSecurityReferenceDataResult cachedResult = cachedResults.get(securityKey);
      if (cachedResult == null) {
        continue; // nothing to refresh
      }
      Set<String> fields = new HashSet<String>();
      fields.addAll(cachedResult.getFieldData().getAllFieldNames());
      fields.addAll(getNotAvailableFields(cachedResult));
      fields.remove(FIELD_NOT_AVAILABLE_NAME);
      Set<String> secsForTheseFields = securitiesByFields.get(fields);
      if (secsForTheseFields == null) {
        secsForTheseFields = new HashSet<String>();
        securitiesByFields.put(fields, secsForTheseFields);
      }
      secsForTheseFields.add(securityKey);
    }
    
    for (Entry<Set<String>, Set<String>> entry : securitiesByFields.entrySet()) {
      Set<String> securityKeysForTheseFields = entry.getValue();
      Set<String> fields = entry.getKey();
      
      ReferenceDataResult underlyingResult = _underlying.getFields(securityKeysForTheseFields, fields);
      for (String securityKey : securityKeysForTheseFields) {
        PerSecurityReferenceDataResult previousResult = cachedResults.get(securityKey);
        PerSecurityReferenceDataResult resolvedResult = getCombinedResult(fields, new PerSecurityReferenceDataResult(securityKey), underlyingResult.getResult(securityKey));
        if (differentCachedResult(previousResult, resolvedResult)) {
          persistSecurityFields(resolvedResult);
        }
      }
    }
  }

  private boolean differentCachedResult(PerSecurityReferenceDataResult previousResult, PerSecurityReferenceDataResult resolvedResult) {
    if (!previousResult.getSecurity().equals(resolvedResult.getSecurity())) {
      throw new OpenGammaRuntimeException("Attempting to compare two different securities " + previousResult + " " + resolvedResult);
    }
    // TODO better, non ordered comparison
    if (previousResult.getFieldData().toString().equals(resolvedResult.getFieldData().toString())) {
      return false;
    }
    return true;
  }

  protected ReferenceDataResult loadAndPersistUnknownFields(
      Map<String, PerSecurityReferenceDataResult> cachedResults,
      Map<Set<String>, Set<String>> securityKeysByFields) {
     
    // TODO kirk 2009-10-23 -- Also need to maintain securities we don't need to put back in the database.
    ReferenceDataResult result = new ReferenceDataResult();
    // REVIEW kirk 2009-10-23 -- Candidate for scatter/gather.
    for (Map.Entry<Set<String>, Set<String>> entry : securityKeysByFields.entrySet()) {
      assert !entry.getValue().isEmpty();
      if (entry.getKey().isEmpty()) {
        s_logger.debug("Satisfied entire request for securities {} from cache", entry.getValue());
        for (String securityKey : entry.getValue()) {
          result.addResult(cachedResults.get(securityKey));
        }
        continue;
      }
      s_logger.info("Loading {} fields for {} securities from underlying", entry.getKey().size(), entry.getValue().size());
      Set<String> requestedFields = entry.getKey();
      ReferenceDataResult loadedResult = getUnderlying().getFields(entry.getValue(), requestedFields);
      for (String securityKey : entry.getValue()) {
        PerSecurityReferenceDataResult cachedResult = cachedResults.get(securityKey);
        PerSecurityReferenceDataResult freshResult = loadedResult.getResult(securityKey);
        
        PerSecurityReferenceDataResult resolvedResult = getCombinedResult(requestedFields, cachedResult, freshResult);
        persistSecurityFields(resolvedResult);
        result.addResult(resolvedResult);
      }
    }
    return result;
  }

  private PerSecurityReferenceDataResult getCombinedResult(
      Set<String> requestedFields,
      PerSecurityReferenceDataResult cachedResult,
      PerSecurityReferenceDataResult freshResult) {
    MutableFudgeMsg unionFieldData = null;
    if (cachedResult == null) {
      unionFieldData = getFudgeContext().newMessage();
    } else {
      unionFieldData = getFudgeContext().newMessage(cachedResult.getFieldData());
    }
    Set<String> returnedFields = new HashSet<String>();
    for (FudgeField freshField : freshResult.getFieldData().getAllFields()) {
      unionFieldData.add(freshField);
      returnedFields.add(freshField.getName());
    }
    //cache not available fields as well
    Set<String> notAvaliableFields = new TreeSet<String>(requestedFields);
    notAvaliableFields.removeAll(returnedFields);

    //add list of not available fields
    for (String notAvailableField : notAvaliableFields) {
      unionFieldData.add(FIELD_NOT_AVAILABLE_NAME, notAvailableField);
    }
    
    PerSecurityReferenceDataResult resolvedResult = new PerSecurityReferenceDataResult(freshResult.getSecurity());
    resolvedResult.setFieldData(unionFieldData);
    
    for (String exception : freshResult.getExceptions()) {
      resolvedResult.getExceptions().add(exception);          
    }
    return resolvedResult;
  }

  protected Map<Set<String>, Set<String>> determineSecuritiesForFieldSets(
      Map<String, PerSecurityReferenceDataResult> cachedResults,
      Set<String> securityKeys,
      Set<String> fields) {
    Map<Set<String>, Set<String>> result = Maps.newHashMap();
    for (String securityKey : securityKeys) {
      PerSecurityReferenceDataResult cachedResult = cachedResults.get(securityKey);
      Set<String> missingFields = null;
      if (cachedResult == null) {
        missingFields = fields;
      } else {
        missingFields = getMissingFields(cachedResult.getFieldData().getAllFieldNames(), fields);
        //remove known not available fields from missingFields
        List<String> notAvailableFieldNames = getNotAvailableFields(cachedResult);
        for (String field : notAvailableFieldNames) {
          missingFields.remove(field);
        }
      }
      Set<String> securitiesMatchingFields = result.get(missingFields);
      if (securitiesMatchingFields == null) {
        securitiesMatchingFields = new TreeSet<String>();
        result.put(missingFields, securitiesMatchingFields);
      }
      securitiesMatchingFields.add(securityKey);
    }
    return result;
  }

  private List<String> getNotAvailableFields(PerSecurityReferenceDataResult cachedResult) {
    List<FudgeField> notAvailableFields = cachedResult.getFieldData().getAllByName(FIELD_NOT_AVAILABLE_NAME);
    List<String> notAvailableFieldNames = new ArrayList<String>(notAvailableFields.size());
    for (FudgeField field : notAvailableFields) {
      notAvailableFieldNames.add((String) field.getValue());
    }
    return notAvailableFieldNames;
  }

  protected static Set<String> getMissingFields(Set<String> existingFieldNames,
      Set<String> fields) {
    Set<String> result = new TreeSet<String>();
    for (String requiredField : fields) {
      if (!existingFieldNames.contains(requiredField)) {
        result.add(requiredField);
      }
    }
    return result;
  }

  protected abstract Map<String, PerSecurityReferenceDataResult> loadCachedResults(Set<String> securityKeys);

  protected abstract void persistSecurityFields(PerSecurityReferenceDataResult securityResult);

}
