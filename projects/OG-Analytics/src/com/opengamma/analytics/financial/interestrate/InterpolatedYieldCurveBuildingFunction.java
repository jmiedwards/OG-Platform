/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedCurveBuildingFunction;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class InterpolatedYieldCurveBuildingFunction extends YieldCurveBundleBuildingFunction {

  private final InterpolatedCurveBuildingFunction _curveBuilder;

  public InterpolatedYieldCurveBuildingFunction(final LinkedHashMap<String, double[]> knotPoints, LinkedHashMap<String, Interpolator1D> interpolators) {
    _curveBuilder = new InterpolatedCurveBuildingFunction(knotPoints, interpolators);
  }

  @Override
  public YieldCurveBundle evaluate(DoubleMatrix1D x) {
    YieldCurveBundle res = new YieldCurveBundle();
    LinkedHashMap<String, InterpolatedDoublesCurve> curves = _curveBuilder.evaluate(x);
    for (Map.Entry<String, InterpolatedDoublesCurve> entry : curves.entrySet()) {
      res.setCurve(entry.getKey(), YieldCurve.from(entry.getValue()));
    }

    return res;
  }
}
