package com.example.venetatodorova.chucknorrisjokes.models;

import java.util.HashMap;
import java.util.Map;

public class Example {

    private String type;
    private Value value;
    private Map<String, Object> additionalProperties = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
