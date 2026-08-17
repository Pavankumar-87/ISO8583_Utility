import com.iso8583_utility.iso8583.builder.Iso8583Builder;
import com.iso8583_utility.iso8583.model.EmvTagDto;
import com.iso8583_utility.iso8583.model.IsoMessageDto;
import com.iso8583_utility.iso8583.parser.FramedMessage;
import com.iso8583_utility.iso8583.parser.FramingHandler;
import com.iso8583_utility.iso8583.parser.Iso8583Parser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Iso8583ParserTest {

        @Test
        void shouldFrameAndUnframeBinaryTwoByteLength() {

                byte[] payload = "HELLO".getBytes();

                byte[] framed = FramingHandler.frame(
                                payload,
                                FramingHandler.LengthEncoding.BINARY_2);

                FramedMessage result = FramingHandler.unframe(
                                framed,
                                FramingHandler.LengthEncoding.BINARY_2,
                                0);

                assertEquals(
                                payload.length,
                                result.declaredLength());

                assertArrayEquals(
                                payload,
                                result.payload());
        }

        @Test
        void shouldParseBasicIsoFields() {

                Iso8583Builder builder = new Iso8583Builder()
                                .mti("0200")
                                .field(
                                                3,
                                                "000000")
                                .field(
                                                4,
                                                "000000001000")
                                .field(
                                                11,
                                                "123456")
                                .field(
                                                41,
                                                "TERM1234");

                byte[] payload = builder.buildIsoPayload();

                Iso8583Parser parser = new Iso8583Parser();

                IsoMessageDto message = parser.parse(
                                payload,
                                new byte[0]);

                assertEquals(
                                "0200",
                                message.getMti());

                assertEquals(
                                "000000",
                                message.getFields()
                                                .get(3)
                                                .getValue());

                assertEquals(
                                "000000001000",
                                message.getFields()
                                                .get(4)
                                                .getValue());

                assertEquals(
                                "123456",
                                message.getFields()
                                                .get(11)
                                                .getValue());

                assertEquals(
                                "TERM1234",
                                message.getFields()
                                                .get(41)
                                                .getValue());
        }

        @Test
        void shouldParseEmvField55() {

                List<EmvTagDto> tags = List.of(
                                tag(
                                                "9F26",
                                                "A1A2A3A4A5A6A7A8"),
                                tag(
                                                "9F10",
                                                "06010A03A0B800"),
                                tag(
                                                "82",
                                                "1800"),
                                tag(
                                                "9F36",
                                                "0001"));

                Iso8583Builder builder = new Iso8583Builder()
                                .mti("0200")
                                .field(
                                                3,
                                                "000000")
                                .field55(tags);

                byte[] payload = builder.buildIsoPayload();

                IsoMessageDto message = new Iso8583Parser()
                                .parse(
                                                payload,
                                                new byte[0]);

                assertTrue(
                                message.getEmvFields()
                                                .containsKey(55));

                List<EmvTagDto> parsed = message.getEmvFields()
                                .get(55);

                assertEquals(
                                4,
                                parsed.size());

                assertEquals(
                                "9F26",
                                parsed.get(0).getTag());

                assertEquals(
                                "A1A2A3A4A5A6A7A8",
                                parsed.get(0).getValue());

                assertEquals(
                                "9F10",
                                parsed.get(1).getTag());

                assertEquals(
                                "06010A03A0B800",
                                parsed.get(1).getValue());
        }

        @Test
        void shouldParseMultiByteEmvTag() {

                byte[] data = hex("5F2A020356");

                List<EmvTagDto> result = com.iso8583_utility.iso8583.parser.EmvTlvParser
                                .parse(data);

                assertEquals(
                                1,
                                result.size());

                assertEquals(
                                "5F2A",
                                result.get(0).getTag());

                assertEquals(
                                2,
                                result.get(0).getLength());

                assertEquals(
                                "0356",
                                result.get(0).getValue());
        }

        // @Test
        // void shouldParse81LengthEncoding() {

        // byte[] value = new byte[128];

        // byte[] data = new byte[132];

        // data[0] = (byte) 0x9F;
        // data[1] = 0x10;
        // data[2] = (byte) 0x81;
        // data[3] = (byte) 0x80;

        // System.arraycopy(
        // value,
        // 0,
        // data,
        // 4,
        // value.length);

        // List<EmvTagDto> result = com.iso8583_utility.iso8583.parser.EmvTlvParser
        // .parse(data);

        // assertEquals(
        // 1,
        // result.size());

        // assertEquals(
        // "9F10",
        // result.get(0).getTag());

        // assertEquals(
        // 128,
        // result.get(0).getLength());
        // }
        void shouldParse81LengthEncoding() {

                byte[] value = new byte[128];

                byte[] data = new byte[132];

                data[0] = (byte) 0x9F;
                data[1] = 0x10;
                data[2] = (byte) 0x81;
                data[3] = (byte) 0x80;

                System.arraycopy(
                                value,
                                0,
                                data,
                                4,
                                value.length);

                List<EmvTagDto> result = com.example.iso8583.parser.EmvTlvParser
                                .parse(data);

                assertEquals(1, result.size());
                assertEquals("9F10", result.get(0).getTag());
                assertEquals(128, result.get(0).getLength());
        }

        @Test
        void shouldRejectTruncatedTlv() {

                byte[] invalid = hex("9F2608A1A2A3");

                assertThrows(
                                RuntimeException.class,
                                () -> com.iso8583_utility.iso8583.parser.EmvTlvParser
                                                .parse(invalid));
        }

        @Test
        void shouldSupportSecondaryBitmap() {

                Iso8583Builder builder = new Iso8583Builder()
                                .mti("0200")
                                .field(
                                                3,
                                                "000000")
                                .field(
                                                70,
                                                "001");

                byte[] payload = builder.buildIsoPayload();

                IsoMessageDto message = new Iso8583Parser()
                                .parse(
                                                payload,
                                                new byte[0]);

                assertEquals(
                                "0200",
                                message.getMti());

                assertEquals(
                                "001",
                                message.getFields()
                                                .get(70)
                                                .getValue());

                assertNotNull(
                                message.getSecondaryBitmap());
        }

        private static EmvTagDto tag(
                        String tag,
                        String value) {

                return new EmvTagDto(
                                tag,
                                value.length() / 2,
                                value,
                                "Test",
                                false);
        }

        private static byte[] hex(
                        String value) {

                return java.util.HexFormat.of()
                                .parseHex(value);
        }
}