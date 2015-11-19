package com.models;

import java.io.Serializable;

public class StateRecord implements Serializable {
    private String state;
    private String date;
    private double value;

    public String getState() {
        return state;
    }

    public String getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }
}
