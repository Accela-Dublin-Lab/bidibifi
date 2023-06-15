package com.accela.bidibifi.proxy.relayer;

import io.vavr.control.Option;
import io.vavr.control.Try;

import java.io.OutputStream;

record Frame(byte[] data, int size) {
    public Try<Frame> writeToStream(final OutputStream stream) {
      return Try.of(() -> {
        stream.write(data(), 0, size());
        stream.flush();
        return this;
      });
    }
    public Option<Byte> getByte(final int position) {
      return Option.of(data)
          .filter(__ -> position < size)
          .map(d -> d[position]);
    }
  }