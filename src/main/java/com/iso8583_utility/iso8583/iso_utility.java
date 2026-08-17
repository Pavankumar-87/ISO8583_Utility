package com.iso8583_utility.iso8583;

import com.iso8583_utility.iso8583.model.IsoMessageDto;
import com.iso8583_utility.iso8583.parser.FramedMessage;
import com.iso8583_utility.iso8583.parser.FramingHandler;
import com.iso8583_utility.iso8583.parser.Iso8583Parser;

public class iso_utility {
    public static IsoMessageDto  parseISO8583Msg(byte[] isoMessage, FramingHandler.LengthEncoding encoding, int tpduLength) throws Exception {
        try {
            FramedMessage frame = FramingHandler.unframe(isoMessage, encoding, tpduLength);
            Iso8583Parser parser1 = new Iso8583Parser();
            return parser1.parse(frame.payload(), frame.tpdu());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new Exception("Unable to parse ISO message");
        }
    }
}
