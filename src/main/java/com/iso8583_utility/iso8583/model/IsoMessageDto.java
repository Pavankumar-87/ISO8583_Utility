package com.iso8583_utility.iso8583.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IsoMessageDto {

    private String mti;
    private String tpdu;
    private String primaryBitmap;
    private String secondaryBitmap;

    private Map<Integer, IsoFieldDto> fields = new LinkedHashMap<>();

    private Map<Integer, List<EmvTagDto>> emvFields = new LinkedHashMap<>();

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getTpdu() {
        return tpdu;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public String getPrimaryBitmap() {
        return primaryBitmap;
    }

    public void setPrimaryBitmap(String primaryBitmap) {
        this.primaryBitmap = primaryBitmap;
    }

    public String getSecondaryBitmap() {
        return secondaryBitmap;
    }

    public void setSecondaryBitmap(String secondaryBitmap) {
        this.secondaryBitmap = secondaryBitmap;
    }

    public Map<Integer, IsoFieldDto> getFields() {
        return fields;
    }

    public void setFields(Map<Integer, IsoFieldDto> fields) {
        this.fields = fields;
    }

    public Map<Integer, List<EmvTagDto>> getEmvFields() {
        return emvFields;
    }

    public void setEmvFields(Map<Integer, List<EmvTagDto>> emvFields) {
        this.emvFields = emvFields;
    }
}