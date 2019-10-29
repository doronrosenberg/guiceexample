package com.guicesample.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
  public String test() {
    System.out.println("Rest endpoint context: " + context.getValue());
    return foo.get();
  }
}
