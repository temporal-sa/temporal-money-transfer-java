package io.temporal.samples.moneytransfer.web;

import java.util.HashMap;
import java.util.Map;

public class ServerInfo {

  public static String getCertPath() {
    return System.getenv("CERT_PATH") != null ? System.getenv("CERT_PATH") : "";
  }

  public static String getKeyPath() {
    return System.getenv("KEY_PATH") != null ? System.getenv("KEY_PATH") : "";
  }

  public static String getNamespace() {
    return System.getenv("NAMESPACE") != null ? System.getenv("NAMESPACE") : "default";
  }

  public static String getAddress() {
    return System.getenv("ADDRESS") != null ? System.getenv("ADDRESS") : "localhost:7233";
  }

  public static String getWebServerURL() {
    return System.getenv("TEMPORAL_JAVA_WEB_SERVER_URL") != null
        ? System.getenv("TEMPORAL_JAVA_WEB_SERVER_URL")
        : "http://localhost:7070/";
  }

  public static Map<String, String> getServerInfo() {
    Map<String, String> info = new HashMap<>();
    info.put("certPath", getCertPath());
    info.put("keyPath", getKeyPath());
    info.put("namespace", getNamespace());
    info.put("address", getAddress());
    return info;
  }
}
