package com.models;


import java.io.Serializable;

public class StationStatus implements Serializable{
    private String name;    //ID stacji
    private String description; //nazwa stacji
    private String river;   //nazwa rzeki; odczytując odcinać nawiasy z końca z zawartościa
    private String parameter;   //??
    private String currentDate; //aktualna data
    private int currentValue;   //aktualny stan wody
    private int previousValue;  //poprzedni stan wody
    private String previousDate;    //poprzednia data
    private String state;   //??
    private double riverCourseKm;
    private double catchmentArea;
    private int trend;  //nie ma potrzeby korzystać
    private double waterGaugeZeroOrdinate;
    private boolean isFromObs;
    private String province;    //dolnośląskie
    private double alarmValue;
    private double warningValue;
    private double lowValue;    //?granica dolna stanów średnich
    private double highValue;   //?granica dolna stanów wysokich

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRiver() {
        return this.river;
    }

    public String getParameter() {
        return parameter;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public int getPreviousValue() {
        return previousValue;
    }

    public String getPreviousDate() {
        return previousDate;
    }

    public String getState() {
        return state;
    }

    public double getRiverCourseKm() {
        return riverCourseKm;
    }

    public double getCatchmentArea() {
        return catchmentArea;
    }

    public double getWaterGaugeZeroOrdinate() {
        return waterGaugeZeroOrdinate;
    }

    public boolean isFromObs() {
        return isFromObs;
    }

    public String getProvince() {
        return province;
    }

    public double getAlarmValue() {
        return alarmValue;
    }

    public double getWarningValue() {
        return warningValue;
    }

    public double getLowValue() {
        return lowValue;
    }

    public double getHighValue() {
        return highValue;
    }
}
