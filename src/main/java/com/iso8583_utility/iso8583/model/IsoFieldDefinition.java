package com.iso8583_utility.iso8583.model;

public record IsoFieldDefinition(
                int fieldNumber,
                String dataType,
                int maxLength,
                LengthType lengthType,
                boolean binary) {

        public enum LengthType {
                FIXED,
                LLVAR,
                LLLVAR
        }

        public static IsoFieldDefinition fixed(
                        int fieldNumber,
                        String dataType,
                        int length) {

                return new IsoFieldDefinition(
                                fieldNumber,
                                dataType,
                                length,
                                LengthType.FIXED,
                                "b".equalsIgnoreCase(dataType));
        }

        public static IsoFieldDefinition llvar(
                        int fieldNumber,
                        String dataType,
                        int maxLength) {

                return new IsoFieldDefinition(
                                fieldNumber,
                                dataType,
                                maxLength,
                                LengthType.LLVAR,
                                "b".equalsIgnoreCase(dataType));
        }

        public static IsoFieldDefinition lllvar(
                        int fieldNumber,
                        String dataType,
                        int maxLength) {

                return new IsoFieldDefinition(
                                fieldNumber,
                                dataType,
                                maxLength,
                                LengthType.LLLVAR,
                                "b".equalsIgnoreCase(dataType));
        }
}