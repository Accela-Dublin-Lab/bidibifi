package com.accela.bidibifi.formatters;

import io.vavr.collection.CharSeq;
import java.io.PrintStream;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HexenSchuss {
  private static final int BYTES_PER_LINE = 16;
  private static final String HEX_FIELD_FMT = "{%-48.48s}";
  private static final String ASCII_FIELD_FMT = "\"%-16.16s\"";
  private static final char NON_PRINTABLE_CHAR = '.';

  public static BiFunction<byte[], Integer, Function<PrintStream, Void>> format =
      (buffer, size) -> output -> {
        final Function<CharSeq, String> renderAsHex = group ->
            group.map(index -> index < buffer.length ? index : 0)
                .map(index -> String.format("%02X%s", buffer[index],
                    (index % BYTES_PER_LINE) == (BYTES_PER_LINE / 2) - 1 ? "  " : " "))
                .collect(Collectors.joining());

        final Predicate<Byte> printable = value -> value >= ' ' && value <= '~';

        final Function<CharSeq, String> renderAsAscii = group ->
            group.map(index -> index < buffer.length ? buffer[index] : 0)
                .map(value -> (char) (printable.test(value) ? value : NON_PRINTABLE_CHAR))
                .map(String::valueOf)
                .collect(Collectors.joining());

        CharSeq.range((char)0, size > 0 ? (char)size.intValue() : 0)
            .grouped(BYTES_PER_LINE)
            .forEach(group ->
                output.printf("%08X "+HEX_FIELD_FMT+" "+ASCII_FIELD_FMT+"\n",
                    (int)group.get(0), renderAsHex.apply(group), renderAsAscii.apply(group)));

        return null;
      };

}
