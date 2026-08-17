package com.iso8583_utility.iso8583.builder;

import com.iso8583_utility.iso8583.config.Iso8583FieldDefinitions;
import com.iso8583_utility.iso8583.model.EmvTagDto;
import com.iso8583_utility.iso8583.model.IsoFieldDefinition;
import com.iso8583_utility.iso8583.parser.FramingHandler;
import com.iso8583_utility.iso8583.parser.IsoParseException;
 
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Iso8583Builder {

    private final Map<Integer, Object> fields =
            new TreeMap<>();

    private final Map<Integer, IsoFieldDefinition> definitions;

    private String mti;

    private byte[] tpdu;

    public Iso8583Builder() {
        this(Iso8583FieldDefinitions.create());
    }

    public Iso8583Builder(
            Map<Integer, IsoFieldDefinition> definitions) {

        this.definitions = definitions;
    }

    public Iso8583Builder mti(String mti) {

        if (mti == null
                || !mti.matches("\\d{4}")) {

            throw new IllegalArgumentException(
                    "MTI must contain exactly four digits");
        }

        this.mti = mti;

        return this;
    }

    public Iso8583Builder field(
            int fieldNumber,
            String value) {

        validateFieldNumber(fieldNumber);

        fields.put(fieldNumber, value);

        return this;
    }

    public Iso8583Builder binaryField(
            int fieldNumber,
            String hex) {

        validateFieldNumber(fieldNumber);

        if (hex == null
                || !hex.matches("[0-9A-Fa-f]*")
                || (hex.length() % 2 != 0)) {

            throw new IllegalArgumentException(
                    "Binary field must be even-length hexadecimal");
        }

        fields.put(
                fieldNumber,
                HexFormat.of().parseHex(hex));

        return this;
    }

    public Iso8583Builder field55(
            List<EmvTagDto> tags) {

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        for (EmvTagDto tag : tags) {

            byte[] tagBytes =
                    HexFormat.of().parseHex(
                            tag.getTag());

            byte[] value =
                    HexFormat.of().parseHex(
                            tag.getValue());

            output.writeBytes(tagBytes);
            writeTlvLength(output, value.length);
            output.writeBytes(value);
        }

        return binaryField(
                55,
                HexFormat.of().formatHex(
                        output.toByteArray()));
    }

    public Iso8583Builder tpdu(
            String tpduHex) {

        this.tpdu =
                HexFormat.of().parseHex(tpduHex);

        return this;
    }

    public byte[] buildIsoPayload() {

        if (mti == null) {
            throw new IllegalStateException(
                    "MTI has not been configured");
        }

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        output.writeBytes(
                mti.getBytes(
                        StandardCharsets.US_ASCII));

        boolean secondary =
                fields.keySet()
                        .stream()
                        .anyMatch(f -> f > 64);

        byte[] primary =
                new byte[8];

        byte[] secondaryBitmap =
                new byte[8];

        for (int field : fields.keySet()) {

            if (field == 1) {
                throw new IllegalArgumentException(
                        "Field 1 is reserved for secondary bitmap");
            }

            if (field > 128) {
                throw new IllegalArgumentException(
                        "Field must be between 2 and 128");
            }

            if (field <= 64) {
                setBit(primary, field);
            } else {
                setBit(
                        secondaryBitmap,
                        field - 64);
            }
        }

        if (secondary) {
            primary[0] |= (byte) 0x80;
        }

        output.writeBytes(primary);

        if (secondary) {
            output.writeBytes(secondaryBitmap);
        }

        for (Map.Entry<Integer, Object> entry :
                fields.entrySet()) {

            int fieldNumber =
                    entry.getKey();

            IsoFieldDefinition definition =
                    definitions.get(fieldNumber);

            if (definition == null) {
                throw new IsoParseException(
                        "No field definition for "
                                + fieldNumber);
            }

            byte[] value =
                    encodeValue(
                            entry.getValue(),
                            definition);

            validateLength(
                    value.length,
                    definition);

            writeLength(
                    output,
                    value.length,
                    definition);

            output.writeBytes(value);
        }

        return output.toByteArray();
    }

    public byte[] buildFramed(
            FramingHandler.LengthEncoding encoding) {

        byte[] isoPayload =
                buildIsoPayload();

        byte[] completePayload;

        if (tpdu != null
                && tpdu.length > 0) {

            completePayload =
                    new byte[
                            tpdu.length
                                    + isoPayload.length];

            System.arraycopy(
                    tpdu,
                    0,
                    completePayload,
                    0,
                    tpdu.length);

            System.arraycopy(
                    isoPayload,
                    0,
                    completePayload,
                    tpdu.length,
                    isoPayload.length);

        } else {
            completePayload = isoPayload;
        }

        return FramingHandler.frame(
                completePayload,
                encoding);
    }

    private byte[] encodeValue(
            Object value,
            IsoFieldDefinition definition) {

        if (definition.binary()) {

            if (value instanceof byte[] bytes) {
                return bytes;
            }

            if (value instanceof String hex) {
                return HexFormat.of()
                        .parseHex(hex);
            }

            throw new IllegalArgumentException(
                    "Binary field "
                            + definition.fieldNumber()
                            + " requires byte[] or hex String");
        }

        return String.valueOf(value)
                .getBytes(
                        StandardCharsets.US_ASCII);
    }

    private void writeLength(
            ByteArrayOutputStream output,
            int length,
            IsoFieldDefinition definition) {

        switch (definition.lengthType()) {

            case FIXED -> {
            }

            case LLVAR ->
                    output.writeBytes(
                            String.format(
                                            "%02d",
                                            length)
                                    .getBytes(
                                            StandardCharsets.US_ASCII));

            case LLLVAR ->
                    output.writeBytes(
                            String.format(
                                            "%03d",
                                            length)
                                    .getBytes(
                                            StandardCharsets.US_ASCII));
        }
    }

    private void validateLength(
            int length,
            IsoFieldDefinition definition) {

        if (definition.lengthType()
                == IsoFieldDefinition.LengthType.FIXED
                && length != definition.maxLength()) {

            throw new IllegalArgumentException(
                    "Field "
                            + definition.fieldNumber()
                            + " requires exactly "
                            + definition.maxLength()
                            + " bytes");
        }

        if (length > definition.maxLength()) {

            throw new IllegalArgumentException(
                    "Field "
                            + definition.fieldNumber()
                            + " length="
                            + length
                            + " exceeds maximum "
                            + definition.maxLength());
        }
    }

    private void setBit(
            byte[] bitmap,
            int field) {

        int index = field - 1;

        int byteIndex =
                index / 8;

        int bitIndex =
                7 - (index % 8);

        bitmap[byteIndex] |=
                (byte) (1 << bitIndex);
    }

    private void validateFieldNumber(
            int fieldNumber) {

        if (fieldNumber < 2
                || fieldNumber > 128) {

            throw new IllegalArgumentException(
                    "Field number must be between 2 and 128");
        }
    }

    private void writeTlvLength(
            ByteArrayOutputStream output,
            int length) {

        if (length < 128) {

            output.write(length);

        } else if (length <= 255) {

            output.write(0x81);
            output.write(length);

        } else if (length <= 65535) {

            output.write(0x82);
            output.write((length >> 8) & 0xFF);
            output.write(length & 0xFF);

        } else {

            throw new IllegalArgumentException(
                    "TLV value too large");
        }
    }
}