package com.iso8583_utility.iso8583.model;

public class IsoFieldDto {

    private int fieldNumber;
    private String dataType;
    private String value;
    private String hexValue;
    private boolean binary;

    public IsoFieldDto() {
    }

    public IsoFieldDto(
            int fieldNumber,
            String dataType,
            String value,
            String hexValue,
            boolean binary) {

        this.fieldNumber = fieldNumber;
        this.dataType = dataType;
        this.value = value;
        this.hexValue = hexValue;
        this.binary = binary;
    }

    public int getFieldNumber() {
        return fieldNumber;
    }

    public void setFieldNumber(int fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getHexValue() {
        return hexValue;
    }

    public void setHexValue(String hexValue) {
        this.hexValue = hexValue;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }
}