/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Computes the present value change when the rate changes by 1 (it is not rescaled to 1 basis point). 
 * The meaning of "rate" will change for each instrument. 
 * For Coupon the result is the discounted accrual factor multiplied by the notional.
 * For PaymentFixed, it is 0 (there is no rate).
 * For annuities, it is the sum of all payments.
 */
// TODO: Review overlap with PresentValueCouponSensitivityCalculator.
public final class PresentValueBasisPointCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBasisPointCalculator INSTANCE = new PresentValueBasisPointCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBasisPointCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBasisPointCalculator() {
  }

  @Override
  public Double visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Double visitFixedPayment(final PaymentFixed payment, final YieldCurveBundle data) {
    return 0.0;
  }

  public Double visitCoupon(final Coupon coupon, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(coupon);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(coupon.getFundingCurveName());
    return fundingCurve.getDiscountFactor(coupon.getPaymentTime()) * coupon.getPaymentYearFraction() * coupon.getNotional();
  }

  @Override
  public Double visitCouponFixed(final CouponFixed coupon, final YieldCurveBundle curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public Double visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    return visitCoupon(coupon, curves);
  }

  @Override
  public Double visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    double pvbp = 0;
    for (final Payment p : annuity.getPayments()) {
      pvbp += visit(p, curves);
    }
    return pvbp;
  }

  @Override
  public Double visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

}