/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.id.Identifier;
import com.opengamma.id.Identifiable;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2009-09-15 -- This REALLY needs to be renamed.

// REVIEW kirk 2009-09-16 -- This needs to be worked out better for serialization.
// It's really not serialization clean, and everything at this level needs to be
// serialization friendly.

public class Currency implements Identifiable {
  
  public static final IdentificationScheme IDENTIFICATION_DOMAIN = new IdentificationScheme("CurrencyISO"); 
  
  private Identifier _identifier;

  private Currency(String isoCode) {
    _identifier = new Identifier(IDENTIFICATION_DOMAIN, isoCode);
  }
  
  public String getISOCode() {
    return _identifier.getValue();
  }
  
  @Override
  public Identifier getIdentityKey() {
    return _identifier;
  }
  
  public static Map<String, Currency> s_instanceMap = new HashMap<String, Currency>();
  // is this even necessary or a good idea?  Probably should 
  public static Currency getInstance(String isoCode) {
    // REVIEW kirk 2009-09-16 -- This is really not matching good practice:
    // - Will allow lower-case ISO codes
    // - Will allow ISO codes outside normal rules (e.g. 3-letter)
    // - Isn't even concurrency safe
    ArgumentChecker.notNull(isoCode, "ISO Code");
    if (s_instanceMap.containsKey(isoCode)) {
      return s_instanceMap.get(isoCode);
    } else {
      Currency curr = new Currency(isoCode);
      s_instanceMap.put(isoCode, curr);
      return curr;
    }
  }
  
  // NOTE: This relies on the above getInstance pattern being used.  If that is ditched, get rid.
  public boolean equals(Object o) {
    return (this == o);
  }
  
  public int hashCode() {
    return _identifier.hashCode();
  }
  
  public String toString() {
    return getISOCode();
  }
}
