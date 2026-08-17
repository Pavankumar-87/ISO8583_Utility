package com.iso8583_utility.iso8583.model;

import java.util.ArrayList;
import java.util.List;

public class EmvTagDto {

    private String tag;
    private int length;
    private String value;
    private String description;
    private boolean constructed;
    private List<EmvTagDto> children = new ArrayList<>();

    public EmvTagDto() {
    }

    public EmvTagDto(
            String tag,
            int length,
            String value,
            String description,
            boolean constructed) {

        this.tag = tag;
        this.length = length;
        this.value = value;
        this.description = description;
        this.constructed = constructed;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isConstructed() {
        return constructed;
    }

    public void setConstructed(boolean constructed) {
        this.constructed = constructed;
    }

    public List<EmvTagDto> getChildren() {
        return children;
    }

    public void setChildren(List<EmvTagDto> children) {
        this.children = children;
    }
}