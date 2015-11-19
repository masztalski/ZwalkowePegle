package com.models;

import java.util.ArrayList;

public class River {
    private int id;
    private String riverName;
    private ArrayList<String> connectedStations; //id przypisanych stacji
    private String trend;
    /*===Custom Data===*/
    private String idStacjiCharakterycznej = "-1";

    public River() {
        connectedStations = new ArrayList<String>();
    }

    public int getId() {
        return id;
    }

    public String getRiverName() {
        return riverName;
    }

    public String getRiverShort() {
//        String pattern = "\\d+";
//        this.riverName = this.riverName.replaceAll(pattern, "");
        return this.riverName.substring(0, this.riverName.indexOf('(')-1);
    }

    public String getRiverId(){
        String temp = this.riverName.substring(riverName.indexOf('(')+1,riverName.indexOf(')'));
        return temp;
    }

    public void setRiverName(String riverName) {
        this.riverName = riverName;
    }

    public ArrayList<String> getConnectedStations(){
        return this.connectedStations;
    }

    public void addConnectedStation(String id){
        connectedStations.add(id);
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public String getIdStacjiCharakterycznej() {
        return idStacjiCharakterycznej;
    }

    public void setIdStacjiCharakterycznej(String idStacjiCharakterycznej) {
        this.idStacjiCharakterycznej = idStacjiCharakterycznej;
    }
}
