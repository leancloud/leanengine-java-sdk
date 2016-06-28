package com.avos.avoscloud.internal.impl;

import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.internal.impl.SimplePersistence;

public class EnginePersistenceImplementation extends SimplePersistence {

  protected EnginePersistenceImplementation() {

  }

  public static EnginePersistenceImplementation instance() {
    synchronized (EnginePersistenceImplementation.class) {
      if (instance == null) {
        instance = new EnginePersistenceImplementation();
      }
    }
    return instance;
  }

  private static EnginePersistenceImplementation instance;
  private ThreadLocal<AVUser> currentUser = new ThreadLocal<AVUser>();

  @Override
  public void setCurrentUser(AVUser user, boolean clean) {
    currentUser.set(user);
  }

  @Override
  public <T extends AVUser> T getCurrentUser(Class<T> userClass) {
    return (T) currentUser.get();
  }
}
