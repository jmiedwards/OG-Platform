/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctionCollection;

import com.opengamma.maths.commonapi.exceptions.MathsExceptionNotImplemented;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGArraySuper;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGDoubleArray;
import com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.Mtimes;
import com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.PlusAndMinus;
import com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.Rdivide;
import com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.Times;
import com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMARearrangingMatrices.copy.Copy;
import com.opengamma.maths.highlevelapi.functions.DOGMAinterfaces.DOGMAArithmeticAPI;

/**
 * Basic Arithmetic 
 */
public class DOGMAArithmetic implements DOGMAArithmeticAPI {
  private final PlusAndMinus _plusMinus = new PlusAndMinus();
  private final Copy _copy = new Copy();
  private final Times _times = new Times();
  private Rdivide _rdivide = new Rdivide();
  private Mtimes _mtimes = new Mtimes();

  /* ADD */
  @Override
  public OGArraySuper<Number> plus(OGArraySuper<Number>... array) {
    OGArraySuper<Number> tmp = _copy.copy(array[0]);
    for (int i = 1; i < array.length; i++) {
      tmp = _plusMinus.plus(tmp, array[i]);
    }
    return tmp;
  }

  @Override
  public OGArraySuper<Number> plus(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return _plusMinus.plus(array1, array2);
  }

  @Override
  public OGArraySuper<Number> plus(OGArraySuper<Number> array1, double aNumber) {
    return plus(array1, new OGDoubleArray(aNumber));
  }

  @Override
  public OGArraySuper<Number> plus(OGArraySuper<Number> array1, int aNumber) {
    return plus(array1, new OGDoubleArray(aNumber));
  }

  /* SUBTRACT */
  @Override
  public OGArraySuper<Number> minus(OGArraySuper<Number>... array) {
    OGArraySuper<Number> tmp = _copy.copy(array[0]);
    for (int i = 1; i < array.length; i++) {
      tmp = _plusMinus.minus(tmp, array[i]);
    }
    return tmp;
  }

  @Override
  public OGArraySuper<Number> minus(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return _plusMinus.minus(array1, array2);
  }

  @Override
  public OGArraySuper<Number> minus(OGArraySuper<Number> array1, double aNumber) {
    return minus(array1, new OGDoubleArray(aNumber));
  }

  @Override
  public OGArraySuper<Number> minus(OGArraySuper<Number> array1, int aNumber) {
    return minus(array1, new OGDoubleArray(aNumber));
  }

  /* LDIVIDE */

  @Override
  public OGArraySuper<Number> ldivide(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    throw new MathsExceptionNotImplemented("ldivide not implemented yet");
  }

  /* MLDIVIDE */

  @Override
  public OGArraySuper<Number> mldivide(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    throw new MathsExceptionNotImplemented("mldivide not implemented yet");
  }

  /* RDIVIDE */

  @Override
  public OGArraySuper<Number> rdivide(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return _rdivide.rdivide(array1, array2);
  }

  @Override
  public OGArraySuper<Number> rdivide(OGArraySuper<Number> array1, double number) {
    return _rdivide.rdivide(array1, new OGDoubleArray(number));
  }

  @Override
  public OGArraySuper<Number> rdivide(OGArraySuper<Number> array1, int number) {
    return _rdivide.rdivide(array1, new OGDoubleArray(number));
  }
  
  @Override
  public OGArraySuper<Number> rdivide(double number, OGArraySuper<Number> array1) {
    return _rdivide.rdivide(new OGDoubleArray(number), array1);
  }

  @Override
  public OGArraySuper<Number> rdivide(int number, OGArraySuper<Number> array1) {
    return _rdivide.rdivide(new OGDoubleArray(number), array1);
  }  

  /* MRDIVIDE */

  @Override
  public OGArraySuper<Number> mrdivide(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    throw new MathsExceptionNotImplemented("mrdivide not implemented yet");
  }

  /* TIMES */
  @Override
  public OGArraySuper<Number> times(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return _times.times(array1, array2);
  }

  @Override
  public OGArraySuper<Number> times(OGArraySuper<Number>... array) {
    OGArraySuper<Number> tmp = _copy.copy(array[0]);
    for (int i = 1; i < array.length; i++) {
      tmp = _times.times(tmp, array[i]);
    }
    return tmp;
  }
  
  @Override
  public OGArraySuper<Number> times(OGArraySuper<Number> array1, double number) {
    return _times.times(array1, new OGDoubleArray(number));
  }

  @Override
  public OGArraySuper<Number> times(OGArraySuper<Number> array1, int number) {
    return _times.times(array1, new OGDoubleArray(number));
  }
  
  @Override
  public OGArraySuper<Number> times(double number, OGArraySuper<Number> array1) {
    return _times.times(array1, new OGDoubleArray(number));
  }

  @Override
  public OGArraySuper<Number> times(int number, OGArraySuper<Number> array1) {
    return _times.times(array1, new OGDoubleArray(number));
  }  

  /* MTIMES */
  @Override
  public OGArraySuper<Number> mtimes(OGArraySuper<Number>... array) {
    OGArraySuper<Number> tmp = _copy.copy(array[0]);
    for (int i = 1; i < array.length; i++) {
      tmp = _mtimes.mtimes(tmp, array[i]);
    }
    return tmp;
  }

  @Override
  public OGArraySuper<Number> mtimes(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return _mtimes.mtimes(array1, array2);
  }

  /* POWER */
  @Override
  public OGArraySuper<Number> power(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    throw new MathsExceptionNotImplemented("power not implemented yet");
  }

  /* MPOWER */
  @Override
  public OGArraySuper<Number> mpower(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    throw new MathsExceptionNotImplemented("mpower not implemented yet");
  }

  /* TRANSPOSE */
  @Override
  public OGArraySuper<Number> transpose(OGArraySuper<Number> array) {
    throw new MathsExceptionNotImplemented("transpose not implemented yet");
  }


}