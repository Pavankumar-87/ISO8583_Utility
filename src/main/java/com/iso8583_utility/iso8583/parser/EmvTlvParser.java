package com.iso8583_utility.iso8583.parser;

import com.iso8583_utility.iso8583.model.EmvTagDto;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public final class EmvTlvParser {

        private EmvTlvParser() {
        }

        private static final HexFormat HEX = HexFormat.of().withUpperCase();

        private static final Map<String, String> TAG_NAMES = Map.ofEntries(

                        Map.entry("4F", "Application Identifier"),
                        Map.entry("50", "Application Label"),
                        Map.entry("57", "Track 2 Equivalent Data"),
                        Map.entry("5A", "Application PAN"),
                        Map.entry("5F20", "Cardholder Name"),
                        Map.entry("5F24", "Application Expiration Date"),
                        Map.entry("5F25", "Application Effective Date"),
                        Map.entry("5F28", "Issuer Country Code"),
                        Map.entry("5F2A", "Transaction Currency Code"),
                        Map.entry("5F2D", "Language Preference"),
                        Map.entry("5F34", "PAN Sequence Number"),
                        Map.entry("5F36", "Transaction Currency Exponent"),

                        Map.entry("82", "Application Interchange Profile"),
                        Map.entry("84", "Dedicated File Name"),
                        Map.entry("87", "Application Priority Indicator"),

                        Map.entry("8A", "Authorization Response Code"),
                        Map.entry("91", "Issuer Authentication Data"),
                        Map.entry("95", "Terminal Verification Results"),
                        Map.entry("9A", "Transaction Date"),
                        Map.entry("9B", "Transaction Status Information"),
                        Map.entry("9C", "Transaction Type"),
                        Map.entry("9F02", "Amount Authorised"),
                        Map.entry("9F03", "Amount Other"),
                        Map.entry("9F09", "Application Version Number"),
                        Map.entry("9F10", "Issuer Application Data"),
                        Map.entry("9F1A", "Terminal Country Code"),
                        Map.entry("9F1C", "Terminal Identification"),
                        Map.entry("9F1E", "Interface Device Serial Number"),
                        Map.entry("9F26", "Application Cryptogram"),
                        Map.entry("9F27", "Cryptogram Information Data"),
                        Map.entry("9F33", "Terminal Capabilities"),
                        Map.entry("9F34", "CVM Results"),
                        Map.entry("9F35", "Terminal Type"),
                        Map.entry("9F36", "Application Transaction Counter"),
                        Map.entry("9F37", "Unpredictable Number"),
                        Map.entry("9F41", "Transaction Sequence Counter"),
                        Map.entry("9F53", "Transaction Category Code"),
                        Map.entry("9F66", "Terminal Transaction Qualifiers"),
                        Map.entry("9F6C", "Card Transaction Qualifiers"));

        public static List<EmvTagDto> parse(byte[] data) {

                if (data == null) {
                        throw new IsoParseException(
                                        "EMV TLV data cannot be null");
                }

                return parseRange(data, 0, data.length);
        }

        private static List<EmvTagDto> parseRange(
                        byte[] data,
                        int start,
                        int end) {

                List<EmvTagDto> tags = new ArrayList<>();

                int offset = start;

                while (offset < end) {

                        TagResult tagResult = readTag(data, offset, end);

                        offset = tagResult.nextOffset;

                        LengthResult lengthResult = readLength(data, offset, end);

                        int length = lengthResult.length;

                        offset = lengthResult.nextOffset;

                        if (offset + length > end) {
                                throw new IsoParseException(
                                                "TLV value exceeds available data. "
                                                                + "Tag=" + tagResult.tag
                                                                + ", length=" + length);
                        }

                        byte[] value = java.util.Arrays.copyOfRange(
                                        data,
                                        offset,
                                        offset + length);

                        boolean constructed = (tagResult.firstTagByte & 0x20) != 0;

                        String hexValue = HEX.formatHex(value);

                        EmvTagDto dto = new EmvTagDto(
                                        tagResult.tag,
                                        length,
                                        hexValue,
                                        TAG_NAMES.getOrDefault(
                                                        tagResult.tag,
                                                        "Unknown EMV Tag"),
                                        constructed);

                        if (constructed && value.length > 0) {
                                dto.setChildren(
                                                parseRange(
                                                                value,
                                                                0,
                                                                value.length));
                        }

                        tags.add(dto);

                        offset += length;
                }

                return tags;
        }

        private static TagResult readTag(
                        byte[] data,
                        int offset,
                        int end) {

                if (offset >= end) {
                        throw new IsoParseException(
                                        "Missing EMV tag");
                }

                int first = data[offset] & 0xFF;

                StringBuilder tag = new StringBuilder();

                tag.append(String.format("%02X", first));

                int nextOffset = offset + 1;

                if ((first & 0x1F) == 0x1F) {

                        boolean last = false;

                        while (!last) {

                                if (nextOffset >= end) {
                                        throw new IsoParseException(
                                                        "Truncated multi-byte EMV tag");
                                }

                                int next = data[nextOffset++] & 0xFF;

                                tag.append(
                                                String.format("%02X", next));

                                last = (next & 0x80) == 0;
                        }
                }

                return new TagResult(
                                tag.toString(),
                                first,
                                nextOffset);
        }

        private static LengthResult readLength(
                        byte[] data,
                        int offset,
                        int end) {

                if (offset >= end) {
                        throw new IsoParseException(
                                        "Missing TLV length");
                }

                int first = data[offset++] & 0xFF;

                if ((first & 0x80) == 0) {
                        return new LengthResult(
                                        first,
                                        offset);
                }

                int numberOfLengthBytes = first & 0x7F;

                if (numberOfLengthBytes == 0) {
                        throw new IsoParseException(
                                        "Indefinite length encoding is not supported");
                }

                if (numberOfLengthBytes > 4) {
                        throw new IsoParseException(
                                        "TLV length uses more than 4 bytes");
                }

                if (offset + numberOfLengthBytes > end) {
                        throw new IsoParseException(
                                        "Truncated multi-byte TLV length");
                }

                int length = 0;

                for (int i = 0; i < numberOfLengthBytes; i++) {

                        length = (length << 8)
                                        | (data[offset++] & 0xFF);
                }

                return new LengthResult(
                                length,
                                offset);
        }

        private record TagResult(
                        String tag,
                        int firstTagByte,
                        int nextOffset) {
        }

        private record LengthResult(
                        int length,
                        int nextOffset) {
        }
}