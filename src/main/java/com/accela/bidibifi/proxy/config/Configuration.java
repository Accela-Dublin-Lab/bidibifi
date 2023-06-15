package com.accela.bidibifi.proxy.config;

public record Configuration(int proxyPort, String targetHost, int targetPort, int soTimeout) {
  @Override
  public String toString() {
    return String.format("Proxy running on port %d, ssl=true target=%s:%d socket-timeout=%d",
        proxyPort, targetHost, targetPort, soTimeout);
  }
}