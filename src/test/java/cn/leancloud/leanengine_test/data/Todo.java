package cn.leancloud.leanengine_test.data;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;

@AVClassName("Todo")
public class Todo extends AVObject {


  public String getContent() {
    return this.getString("content");
  }

  public void setContent(String content) {
    this.put("content", content);
  }

  public void setAuthor(AVUser user) {
    this.put("author", user);
  }

  public AVUser getAuthor() {
    return this.getAVUser("author");
  }
}
