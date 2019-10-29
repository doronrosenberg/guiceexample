package com.guicesample;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

/**
 * Bar
 */
@RequestScoped
public class Bar {
  private final Context context;

  @Inject
  public Bar(Context context) {
    this.context = context;
  }

  public Context getContext() {
    return context;
  }
}
