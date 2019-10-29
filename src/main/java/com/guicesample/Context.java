package com.guicesample;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

/**
 * Generic context object that is used in the rest and service code.  Due to how weld works, we inject it and then
 * init the value.
 */
public class Context{
  private String value;
  private boolean inited = false;

  public Context() {
  }

  public void init(String value) {
    if (inited) {
      throw new IllegalStateException("cant call twice");
    }

    this.value = value;
    this.inited = true;
  }


  public String getValue() {
    return value;
  }
}