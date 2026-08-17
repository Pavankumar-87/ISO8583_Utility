package com.iso8583_utility.iso8583.parser;

public record FramedMessage(
                int declaredLength,
                byte[] payload,
                byte[] tpdu) {
}