package cn.leancloud;

import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.internal.impl.SimplePersistence;

public class EnginePersistence extends SimplePersistence {

  private ThreadLocal<AVUser> currentUser;

  EnginePersistence() {
    currentUser = new InheritableThreadLocal<>();
  }

  @Override
  public void setCurrentUser(AVUser user, boolean clean) {
    currentUser.set(user);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends AVUser> T getCurrentUser(Class<T> userClass) {
    return (T) currentUser.get();
  }
}
