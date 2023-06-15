package com.accela.bidibifi.proxy.relayer;

import com.accela.bidibifi.formatters.HexenSchuss;
import com.accela.bidibifi.proxy.config.Configuration;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

import static com.accela.bidibifi.proxy.relayer.SocketRelayer.Direction.Inbound;
import static com.accela.bidibifi.proxy.relayer.SocketRelayer.Direction.Outbound;

public class SocketRelayer extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(SocketRelayer.class);
  enum Direction {Outbound, Inbound}
  private final Direction direction;
  private static int instanceCount = 0;
  private final Socket source;
  private final Socket destination;

  private SocketRelayer(final Configuration configuration, final Direction direction,
                        final Socket source, final Socket destination) throws IOException {
    setName(direction + "Relayer-" + ++instanceCount);
    this.direction = direction;
    this.source = source;
    this.destination = destination;
    source.setSoTimeout(configuration.soTimeout());
    if (direction == Outbound) {
      logger.info("client relay started {} => {}",
          source.getRemoteSocketAddress(), destination.getRemoteSocketAddress());
    } else {
      logger.info("%s target relay started {} <= {}",
          destination.getRemoteSocketAddress(), source.getRemoteSocketAddress());
    }
  }

  public static SocketRelayer outbound(
      final Configuration configuration, final Socket source, final Socket destination) throws IOException {
    return new SocketRelayer(configuration, Outbound, source, destination);
  }

  public static SocketRelayer inbound(
      final Configuration configuration, final Socket source, final Socket destination) throws IOException {
    return new SocketRelayer(configuration, Inbound, source, destination);
  }

  @Override
  public void run() {
    while (relay()) {}
  }

  private boolean relay() {
    return readFromSource()
        .onFailure(t -> logger.info("read/write: {}", t.getMessage()))
        .filter(this::shouldForward)
        .flatMap(this::writeToDestination)
        .isSuccess();
  }

  private Try<Frame> readFromSource() {
    final byte[] requestBuffer = new byte[4096];
    logger.info("reading from {} with timeout={}ms", direction,
        Try.of(source::getSoTimeout).getOrElse(() -> 0));
    return Try.of(source::getInputStream)
        .flatMap(input -> Try.of(() -> input.read(requestBuffer))
            .peek(bytesRead -> logger.info("<<< read {} bytes from {}", bytesRead, direction))
            .filterTry(b -> b != -1)
            .map(bytesRead -> new Frame(requestBuffer, bytesRead))
            .peek(frame -> HexenSchuss.format.apply(frame.data(), frame.size()).apply(System.out)));
  }

  private Try<Frame> writeToDestination(final Frame frame) {
    logger.info(">>> writing {} bytes {}", frame.size(), direction == Outbound ? Inbound : Outbound);
    return Try.of(destination::getOutputStream)
        .flatMap(frame::writeToStream);
  }

  private boolean shouldForward(final Frame frame) {
    logger.info(String.format("shouldForward? TYPE=%02d", frame.getByte(4).getOrElse((byte) 0)));
    return true;
  }

}