package com.guicesample.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.SecurityContext;

import com.guicesample.Context;
import com.guicesample.service.Foo;

/**
 * Basic REST Endpoint
 */
@Path("hello")
public class APIResource {
  private final Foo foo;
  private final Context context;

  @Inject
  public APIResource(Foo foo, Context context) {
    this.foo = foo;
    this.context = context;
  }

  @GET
  public String test(@javax.ws.rs.core.Context SecurityContext securityContext) {
    System.out.println("Context principal: " + context.getValue());
    System.out.println("Security context principal: " + securityContext.getUserPrincipal().getName());
    return foo.get();
  }
}
