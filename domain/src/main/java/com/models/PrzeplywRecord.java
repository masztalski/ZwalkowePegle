package com.models;

import java.io.Serializable;

public class PrzeplywRecord implements Serializable {
    private String date;
    private double value;
    private int dreId;
    private String operationId;
    private String parameterId;
    private int versionId;
    private long id;

    public String getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }

    public int getDreId() {
        return dreId;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getParameterId() {
        return parameterId;
    }

    public int getVersionId() {
        return versionId;
    }

    public long getId() {
        return id;
    }
}
