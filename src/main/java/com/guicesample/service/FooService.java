package com.guicesample.service;

import java.util.Random;

import com.guicesample.Context;

public class FooService implements Foo {
  private final Context context;
  private final int num;

  public FooService(Context context) {
    this.context = context;
    num = new Random().nextInt();
  }

  @Override
  public String get() {
    return "foo";
  }

  @Override
  public int getInt() {
    return num;
  }

  @Override
  public Context getContext() {
    return context;
  }
}
