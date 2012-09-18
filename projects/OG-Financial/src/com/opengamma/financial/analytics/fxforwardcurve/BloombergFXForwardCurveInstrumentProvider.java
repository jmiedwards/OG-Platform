/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class BloombergFXForwardCurveInstrumentProvider implements FXForwardCurveInstrumentProvider {
  private static final ExternalScheme SCHEME = ExternalSchemes.BLOOMBERG_TICKER;
  private final String _prefix;
  private final String _postfix;
  private final String _spotPrefix;
  private final String _dataFieldName;
  private final String _spotName;
  private final ExternalId _spotId;

  public BloombergFXForwardCurveInstrumentProvider(final String prefix, final String postfix, final String spotPrefix, final String dataFieldName) {
    ArgumentChecker.notNull(prefix, "prefix");
    ArgumentChecker.notNull(postfix, "postfix");
    ArgumentChecker.notNull(spotPrefix, "spot prefix");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    _prefix = prefix;
    _postfix = postfix;
    _spotPrefix = spotPrefix;
    _dataFieldName = dataFieldName;
    _spotName = spotPrefix + " " + _postfix;
    _spotId = ExternalId.of(SCHEME, _spotName);
  }

  public String getPrefix() {
    return _prefix;
  }

  public String getPostfix() {
    return _postfix;
  }

  public String getSpotPrefix() {
    return _spotPrefix;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  public String getSpotName() {
    return _spotName;
  }

  @Override
  public ExternalId getSpotInstrument() {
    return _spotId;
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor) {
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_prefix);
    final Period period = tenor.getPeriod();
    if (period.getYears() != 0) {
      ticker.append(period.getYears() + "Y");
    } else if (period.getMonths() != 0) {
      ticker.append(period.getMonths() + "M");
    } else {
      final int days = period.getDays();
      if (days != 0) {
        if (days % 7 == 0) {
          ticker.append(days / 7 + "W");
        } else if (days == 1) {
          ticker.append("ON");
        } else if (days == 2) {
          ticker.append("TN");
        } else if (days == 3) {
          ticker.append("SN");
        } else {
          throw new OpenGammaRuntimeException("Cannot handle period of " + days + " days");
        }
      } else {
        throw new OpenGammaRuntimeException("Can only handle periods of year, month, week and day");
      }
    }
    ticker.append(" ");
    ticker.append(_postfix);
    return ExternalId.of(SCHEME, ticker.toString());
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType,
      final IndexType receiveIndexType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor resetTenor, final IndexType indexType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    return getPrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BloombergFXForwardCurveInstrumentProvider)) {
      return false;
    }
    final BloombergFXForwardCurveInstrumentProvider other = (BloombergFXForwardCurveInstrumentProvider) obj;
    return getPrefix().equals(other.getPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        getSpotPrefix().equals(other.getSpotPrefix()) &&
        getDataFieldName().equals(other.getDataFieldName());
  }
}
