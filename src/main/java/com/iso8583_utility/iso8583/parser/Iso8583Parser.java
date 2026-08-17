package com.iso8583_utility.iso8583.parser;

import com.iso8583_utility.iso8583.config.Iso8583FieldDefinitions;
import com.iso8583_utility.iso8583.model.EmvTagDto;
import com.iso8583_utility.iso8583.model.IsoFieldDefinition;
import com.iso8583_utility.iso8583.model.IsoFieldDto;
import com.iso8583_utility.iso8583.model.IsoMessageDto;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public class Iso8583Parser {

        private final Map<Integer, IsoFieldDefinition> definitions;

        private static final HexFormat HEX = HexFormat.of().withUpperCase();

        public Iso8583Parser() {
                this(Iso8583FieldDefinitions.create());
        }

        public Iso8583Parser(
                        Map<Integer, IsoFieldDefinition> definitions) {

                this.definitions = definitions;
        }

        public IsoMessageDto parse(
                        byte[] isoPayload,
                        byte[] tpdu) {

                if (isoPayload == null) {
                        throw new IsoParseException(
                                        "ISO payload cannot be null");
                }

                if (isoPayload.length < 12) {
                        throw new IsoParseException(
                                        "ISO message is too short");
                }

                Cursor cursor = new Cursor(isoPayload);

                String mti = cursor.readAscii(4);

                validateMti(mti);

                byte[] primaryBitmap = cursor.readBytes(8);

                boolean secondaryPresent = (primaryBitmap[0] & 0x80) != 0;

                byte[] secondaryBitmap = secondaryPresent
                                ? cursor.readBytes(8)
                                : new byte[0];

                List<Integer> fields = extractSetFields(
                                primaryBitmap,
                                secondaryBitmap);

                IsoMessageDto message = new IsoMessageDto();

                message.setMti(mti);

                if (tpdu != null && tpdu.length > 0) {
                        message.setTpdu(HEX.formatHex(tpdu));
                }

                message.setPrimaryBitmap(
                                HEX.formatHex(primaryBitmap));

                if (secondaryPresent) {
                        message.setSecondaryBitmap(
                                        HEX.formatHex(secondaryBitmap));
                }

                for (int fieldNumber : fields) {

                        IsoFieldDefinition definition = definitions.get(fieldNumber);

                        if (definition == null) {
                                // throw new IsoParseException("No definition configured for field "+
                                // fieldNumber);
                                System.err.println("No definition configured for field " + fieldNumber
                                                + ". Skipping this field.");
                                continue; // Skip this field and continue parsing the next one
                        }
                        byte[] raw = null;
                        try {
                                raw = readField(cursor, definition);
                        } catch (IsoParseException e) {
                                System.err.println("Error parsing field " + fieldNumber + ": " + e.getMessage());
                                continue; // Skip this field and continue parsing the next one
                        }
                        String value;
                        String hexValue;

                        if (definition.binary()) {

                                value = HEX.formatHex(raw);
                                hexValue = value;

                        } else {

                                value = new String(
                                                raw,
                                                StandardCharsets.US_ASCII);

                                hexValue = HEX.formatHex(raw);
                        }

                        IsoFieldDto field = new IsoFieldDto(
                                        fieldNumber,
                                        definition.dataType(),
                                        value,
                                        hexValue,
                                        definition.binary());

                        message.getFields()
                                        .put(fieldNumber, field);

                        /*
                         * Field 55 is the normal EMV container.
                         *
                         * Field 127 can also be configured as binary
                         * and parsed as EMV when required by the network.
                         */
                        if (fieldNumber == 55
                                        || fieldNumber == 127) {

                                try {

                                        List<EmvTagDto> tags = EmvTlvParser.parse(raw);

                                        if (!tags.isEmpty()) {
                                                message.getEmvFields()
                                                                .put(fieldNumber, tags);
                                        }

                                } catch (IsoParseException ignored) {

                                        /*
                                         * Field 127 is often not pure EMV TLV.
                                         *
                                         * Do not fail the complete ISO message
                                         * merely because a network-specific field
                                         * is not TLV.
                                         */
                                }
                        }
                }

                if (cursor.remaining() != 0) {
                        // throw new IsoParseException("Unexpected trailing bytes after ISO fields: "+
                        // cursor.remaining());
                        System.err.println(
                                        "Warning: Unexpected trailing bytes after ISO fields: " + cursor.remaining());
                }

                return message;
        }

        private byte[] readField(
                        Cursor cursor,
                        IsoFieldDefinition definition) {

                int length;

                switch (definition.lengthType()) {

                        case FIXED ->
                                length = definition.maxLength();

                        case LLVAR ->
                                length = cursor.readAsciiInteger(2);

                        case LLLVAR ->
                                length = cursor.readAsciiInteger(3);

                        default ->
                                throw new IsoParseException(
                                                "Unsupported field length type");
                }

                if (length < 0
                                || length > definition.maxLength()) {

                        throw new IsoParseException(
                                        "Invalid field "
                                                        + definition.fieldNumber()
                                                        + " length="
                                                        + length
                                                        + ", max="
                                                        + definition.maxLength());
                }

                return cursor.readBytes(length);
        }

        private List<Integer> extractSetFields(
                        byte[] primary,
                        byte[] secondary) {

                List<Integer> result = new ArrayList<>();

                for (int i = 1; i <= 64; i++) {

                        if (isBitSet(primary, i)) {

                                /*
                                 * Bit 1 indicates the secondary bitmap.
                                 * It is not a data field.
                                 */
                                if (i != 1) {
                                        result.add(i);
                                }
                        }
                }

                if (secondary.length > 0) {

                        for (int i = 1; i <= 64; i++) {

                                if (isBitSet(secondary, i)) {
                                        result.add(i + 64);
                                }
                        }
                }

                return result;
        }

        private boolean isBitSet(
                        byte[] bitmap,
                        int fieldNumber) {

                int index = fieldNumber - 1;

                int byteIndex = index / 8;

                int bitIndex = 7 - (index % 8);

                return (bitmap[byteIndex]
                                & (1 << bitIndex)) != 0;
        }

        private void validateMti(String mti) {

                if (mti == null
                                || mti.length() != 4
                                || !mti.matches("\\d{4}")) {

                        throw new IsoParseException(
                                        "Invalid MTI: " + mti);
                }
        }

        private static class Cursor {

                private final byte[] data;
                private int offset;

                Cursor(byte[] data) {
                        this.data = data;
                }

                byte[] readBytes(int length) {

                        if (length < 0) {
                                throw new IsoParseException(
                                                "Negative read length");
                        }

                        if (offset + length > data.length) {

                                throw new IsoParseException(
                                                "Truncated ISO message at offset "
                                                                + offset
                                                                + ", requested="
                                                                + length
                                                                + ", remaining="
                                                                + remaining());
                        }

                        byte[] result = java.util.Arrays.copyOfRange(
                                        data,
                                        offset,
                                        offset + length);

                        offset += length;

                        return result;
                }

                String readAscii(int length) {

                        return new String(
                                        readBytes(length),
                                        StandardCharsets.US_ASCII);
                }

                int readAsciiInteger(int digits) {

                        String value = readAscii(digits);

                        if (!value.matches("\\d{" + digits + "}")) {

                                throw new IsoParseException(
                                                "Invalid variable field length: "
                                                                + value);
                        }

                        return Integer.parseInt(value);
                }

                int remaining() {
                        return data.length - offset;
                }
        }
}