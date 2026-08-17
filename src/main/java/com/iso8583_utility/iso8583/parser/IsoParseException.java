package com.iso8583_utility.iso8583.parser;

public class IsoParseException extends RuntimeException {

    public IsoParseException(String message) {
        super(message);
    }

    public IsoParseException(String message, Throwable cause) {
        super(message, cause);
    }
}