package com.guicesample;

import javax.inject.Inject;

public class OtherBar extends Bar {
  @Inject
  public OtherBar(Context context) {
    super(context);
  }
}
