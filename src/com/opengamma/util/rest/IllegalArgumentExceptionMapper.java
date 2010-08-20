/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * A JAX-RS provider to convert {@code IllegalArgumentException} to a RESTful 400.
 */
@Provider
public class IllegalArgumentExceptionMapper
    extends AbstractExceptionMapper
    implements ExceptionMapper<IllegalArgumentException> {

  /**
   * Creates the mapper.
   */
  public IllegalArgumentExceptionMapper() {
    super(Status.BAD_REQUEST);
  }

  //-------------------------------------------------------------------------
  @Override
  public Response toResponse(final IllegalArgumentException exception) {
    return createResponse(exception);
  }

}
