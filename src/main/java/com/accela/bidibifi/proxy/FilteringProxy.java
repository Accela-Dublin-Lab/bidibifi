package com.accela.bidibifi.proxy;

import com.accela.bidibifi.proxy.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;

public class FilteringProxy {
    private static final Logger logger = LoggerFactory.getLogger(FilteringProxy.class);
    final static ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
    private static Configuration configuration;

    public static void main(String[] args) {
        Thread.currentThread().setName("Filtering-proxy");
        configuration = parseArgs(args);

        acceptConnections();
    }

    private static void acceptConnections() {
        try (final SSLServerSocket listener =
                 (SSLServerSocket)factory.createServerSocket(configuration.proxyPort())) {
            logger.info(configuration.toString());
            while (true) {
                final Socket clientSocket = listener.accept();
                new Connection(configuration, clientSocket).start();
            }
        } catch (final IOException e) {
            logger.error("connect failed: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static Configuration parseArgs(final String[] args) {
        int proxyPort = 8443;
        String targetHost = "";
        int targetPort = 0;
        int soTimeout = 10000;
        for (final String arg : args) {
            if (arg.startsWith("--proxy-port=")) {
                proxyPort = Integer.parseInt(arg.substring("--proxy-port=".length()));
            } else if (arg.startsWith("--target-host=")) {
                targetHost = arg.substring("--target-host=".length());
            } else if (arg.startsWith("--target-port=")) {
                targetPort = Integer.parseInt(arg.substring("--target-port=".length()));
            } else if (arg.startsWith("--so-timeout=")) {
                soTimeout = Integer.parseInt(arg.substring("--so-timeout=".length()));
            }
        }
        return new Configuration(proxyPort, targetHost, targetPort, soTimeout);
    }
}