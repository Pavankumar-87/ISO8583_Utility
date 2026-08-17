package com.iso8583_utility.iso8583.parser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class FramingHandler {

        private FramingHandler() {
        }

        public enum LengthEncoding {
                BINARY_2,
                BINARY_4,
                ASCII_2,
                ASCII_4
        }

        public static FramedMessage unframe(
                        byte[] frame,
                        LengthEncoding encoding,
                        int tpduLength) {

                if (frame == null) {
                        throw new IsoParseException("Frame cannot be null");
                }

                int headerLength = switch (encoding) {
                        case BINARY_2, ASCII_2 -> 2;
                        case BINARY_4, ASCII_4 -> 4;
                };

                if (frame.length < headerLength) {
                        throw new IsoParseException(
                                        "Frame shorter than length header");
                }
                if (headerLength > 9999) {
                        throw new IllegalArgumentException("Payload exceeds 4-digit ASCII length");
                }

                int declaredLength;

                switch (encoding) {

                        case BINARY_2 -> declaredLength = ByteBuffer.wrap(frame, 0, 2)
                                        .order(ByteOrder.BIG_ENDIAN)
                                        .getShort() & 0xFFFF;

                        case BINARY_4 -> declaredLength = ByteBuffer.wrap(frame, 0, 4)
                                        .order(ByteOrder.BIG_ENDIAN)
                                        .getInt();

                        case ASCII_2, ASCII_4 -> {
                                String lengthText = new String(frame, 0, headerLength);

                                if (!lengthText.matches("\\d{" + headerLength + "}")) {
                                        throw new IsoParseException(
                                                        "Invalid ASCII length header: " + lengthText);
                                }

                                declaredLength = Integer.parseInt(lengthText);
                        }

                        default -> throw new IllegalStateException(
                                        "Unsupported framing encoding");
                }

                if (declaredLength < 0) {
                        throw new IsoParseException("Negative frame length");
                }

                int available = frame.length - headerLength;

                if (available < declaredLength) {
                        // throw new IsoParseException(
                        // "Truncated ISO frame. Declared payload length="
                        // + declaredLength
                        // + ", available="
                        // + available);
                        System.err.println("Truncated ISO frame. Declared payload length="
                                        + declaredLength
                                        + ", available="
                                        + available);
                }

                byte[] payload = Arrays.copyOfRange(
                                frame,
                                headerLength,
                                headerLength + declaredLength);

                byte[] tpdu = new byte[0];

                if (tpduLength > 0) {

                        if (payload.length < tpduLength) {
                                throw new IsoParseException(
                                                "Payload shorter than TPDU length");
                        }

                        tpdu = Arrays.copyOfRange(
                                        payload,
                                        0,
                                        tpduLength);

                        payload = Arrays.copyOfRange(
                                        payload,
                                        tpduLength,
                                        payload.length);
                }

                return new FramedMessage(
                                declaredLength,
                                payload,
                                tpdu);
        }

        public static byte[] frame(
                        byte[] payload,
                        LengthEncoding encoding) {

                if (payload == null) {
                        throw new IllegalArgumentException(
                                        "Payload cannot be null");
                }

                int length = payload.length;

                return switch (encoding) {

                        case BINARY_2 -> {
                                if (length > 65535) {
                                        throw new IllegalArgumentException(
                                                        "Payload exceeds 2-byte length");
                                }

                                ByteBuffer buffer = ByteBuffer.allocate(2 + length);

                                buffer.putShort((short) length);
                                buffer.put(payload);

                                yield buffer.array();
                        }

                        case BINARY_4 -> {
                                ByteBuffer buffer = ByteBuffer.allocate(4 + length);

                                buffer.putInt(length);
                                buffer.put(payload);

                                yield buffer.array();
                        }

                        case ASCII_2 -> {
                                if (length > 9999) {
                                        throw new IllegalArgumentException(
                                                        "Payload exceeds 4-digit ASCII length");
                                }

                                byte[] header = String.format("%02d", length)
                                                .getBytes();

                                byte[] result = new byte[header.length + payload.length];

                                System.arraycopy(header, 0, result, 0, header.length);
                                System.arraycopy(
                                                payload,
                                                0,
                                                result,
                                                header.length,
                                                payload.length);

                                yield result;
                        }

                        case ASCII_4 -> {
                                if (length > 9999) {
                                        throw new IllegalArgumentException(
                                                        "Payload exceeds 4-digit ASCII length");
                                }

                                byte[] header = String.format("%04d", length)
                                                .getBytes();

                                byte[] result = new byte[header.length + payload.length];

                                System.arraycopy(header, 0, result, 0, header.length);
                                System.arraycopy(
                                                payload,
                                                0,
                                                result,
                                                header.length,
                                                payload.length);

                                yield result;
                        }
                };
        }
}