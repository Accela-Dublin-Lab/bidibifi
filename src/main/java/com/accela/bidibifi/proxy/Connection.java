package com.accela.bidibifi.proxy;

import com.accela.bidibifi.proxy.config.Configuration;
import com.accela.bidibifi.proxy.relayer.SocketRelayer;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

class Connection extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(Connection.class);
  private static int instanceCount = 0;
  private final Socket clientSocket;
  private final Configuration configuration;

  Connection(final Configuration configuration, final Socket socket) throws IOException {
    this.configuration = configuration;
    this.clientSocket = socket;
    setName("Connection-" + ++instanceCount);
    logger.info("New Connection for {}}:{}",
        clientSocket.getInetAddress().getHostName(), clientSocket.getPort());
  }

  @Override
  public void run() {
    try (final Socket targetSocket = createSSLSocket()) {
      final Thread clientRelay = SocketRelayer.outbound(configuration, clientSocket, targetSocket);
      clientRelay.start();
      final Thread targetRelay = SocketRelayer.inbound(configuration, targetSocket, clientSocket);
      targetRelay.start();

      clientRelay.join();
      targetRelay.join();
    } catch (final Exception e) {
      logger.error("connection failed: {}", e.getMessage());
      e.printStackTrace();
    }

    logger.info("%s Connection closed.");
    Option.of(clientSocket)
        .filter(cs -> !cs.isClosed())
        .peek(cs ->
            logger.info("%s Closing socket from {}", clientSocket.getRemoteSocketAddress()))
        .forEach(cs -> Try.run(cs::close));
  }

  private Socket createSSLSocket() throws IOException {
    final SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    final SSLSocket sslSocket =
        (SSLSocket) sslSocketFactory.createSocket(configuration.targetHost(), configuration.targetPort());
    sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
    sslSocket.startHandshake();
    sslSocket.setKeepAlive(true);
    sslSocket.setSoTimeout(configuration.soTimeout());
    return sslSocket;
  }
}