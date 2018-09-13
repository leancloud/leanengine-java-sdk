package cn.leancloud.leanengine_test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.Response;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class MetadataTest extends EngineBasicTest {

  @Test
  public void testGetMetadata() throws IOException {
    Request.Builder builder = new Request.Builder()
        .addHeader("X-LC-Id", getAppId())
        .addHeader("X-LC-key", getMasterKey() + ",master")
        .addHeader("Content-Type", getContentType())
        .url("http://localhost:3000/1.1/functions/_ops/metadatas").get();
    Response response = client.newCall(builder.build()).execute();
    assertEquals(200, response.code());
    JSONArray functions = (JSONArray) JSON.parseObject(new String(response.body().bytes()), Map.class).get("result");
    assertTrue(functions.contains("hello"));
    assertTrue(functions.contains("__before_save_for__User"));
    assertTrue(functions.contains("_messageReceived"));
  }

  @Test
  public void testGetMetadata_unauth() throws IOException {
    Request.Builder builder = new Request.Builder()
        .addHeader("X-LC-Id", getAppId())
        .addHeader("X-LC-key", getAppKey())
        .addHeader("Content-Type", getContentType())
        .url("http://localhost:3000/1.1/functions/_ops/metadatas").get();
    Response response = client.newCall(builder.build()).execute();
    assertEquals(401, response.code());
  }
}
