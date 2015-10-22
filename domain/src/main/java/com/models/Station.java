package com.models;

import java.util.ArrayList;
import java.util.List;

public class Station {
    /*======API model========*/
    private String id;
    private String name;
    private String state;   //opisowy stan wody na stacji -> Constants->hydroStatusLabels (StationListObject.s)
    private StationStatus status;
    private String trend;   //Trend, opada,const, wzrast itp.
    private ArrayList<StateRecord> waterStateRecords;
    private ArrayList<PrzeplywRecord> dischargeRecords;
    private double highestHighDischargeValue;
    private double highDischargeValue;
    private double lowDischargeValue;   //granica dolna przepływów średnich
    private double mediumLowDischargeValue; //średni niski przepływ SNQ
    private double lowestLowDischargeValue;

    /*====Dane dodatkowe======*/
    private int dolnaGranicaPoziomu = -1;
    private double dolnaGranicaPrzeplywu = -1.0;
    private boolean isFav = false;
    private boolean isUserCustomized = false;
    private boolean isByDefaultCustomized = false;
    private boolean notifByPrzeplyw = true;
    /*=======Przepływy charakteryczytczne=====*/
    private double llw_przeplyw = -1.0;
    private double lw_przeplyw = -1.0;
    private double mw1_przeplyw = -1.0;
    private double mw2_przeplyw = -1.0;
    private double hw_przeplyw = -1.0;
    /*======Poziomy charakterystyczne====*/
    private int llw_poziom = -1;
    private int lw_poziom = -1;
    private int mw1_poziom = -1;
    private int mw2_poziom = -1;
    private int hw_poziom = -1;
    private String notifHint = "LLW";
    private int notifCheckedId = -1;

    /*====Gettery API===*/

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public StationStatus getStatus() {
        return status;
    }

    public String getTrend() {
        return trend;
    }

    public List<StateRecord> getWaterStateRecords() {
        return waterStateRecords;
    }

    public List<PrzeplywRecord> getDischargeRecords() {
        return dischargeRecords;
    }

    public double getHighestHighDischargeValue() {
        return highestHighDischargeValue;
    }

    public double getHighDischargeValue() {
        return highDischargeValue;
    }

    public double getLowDischargeValue() {
        return lowDischargeValue;
    }

    public double getMediumLowDischargeValue() {
        return mediumLowDischargeValue;
    }

    public double getLowestLowDischargeValue() {
        return lowestLowDischargeValue;
    }

    public int getDolnaGranicaPoziomu() {
        return dolnaGranicaPoziomu;
    }

    public void setDolnaGranicaPoziomu(int dolnaGranicaPoziomu) {
        this.dolnaGranicaPoziomu = dolnaGranicaPoziomu;
    }

    public double getDolnaGranicaPrzeplywu() {
        return dolnaGranicaPrzeplywu;
    }

    public void setDolnaGranicaPrzeplywu(double dolnaGranicaPrzeplywu) {
        this.dolnaGranicaPrzeplywu = dolnaGranicaPrzeplywu;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setIsFav(boolean isFav) {
        this.isFav = isFav;
    }

    public boolean isUserCustomized() {
        return isUserCustomized;
    }

    public void setIsUserCustomized(boolean isUserCustomized) {
        this.isUserCustomized = isUserCustomized;
    }

    public boolean isByDefaultCustomized() {
        return isByDefaultCustomized;
    }

    public void setIsByDefaultCustomized(boolean isByDefaultCustomized) {
        this.isByDefaultCustomized = isByDefaultCustomized;
    }

    public boolean isNotifByPrzeplyw() {
        return notifByPrzeplyw;
    }

    public void setNotifByPrzeplyw(boolean notifByPrzeplyw) {
        this.notifByPrzeplyw = notifByPrzeplyw;
    }

    public double getLlw_przeplyw() {
        return llw_przeplyw;
    }

    public void setLlw_przeplyw(double llw_przeplyw) {
        this.llw_przeplyw = llw_przeplyw;
    }

    public double getLw_przeplyw() {
        return lw_przeplyw;
    }

    public void setLw_przeplyw(double lw_przeplyw) {
        this.lw_przeplyw = lw_przeplyw;
    }

    public double getMw1_przeplyw() {
        return mw1_przeplyw;
    }

    public void setMw1_przeplyw(double mw1_przeplyw) {
        this.mw1_przeplyw = mw1_przeplyw;
    }

    public double getMw2_przeplyw() {
        return mw2_przeplyw;
    }

    public void setMw2_przeplyw(double mw2_przeplyw) {
        this.mw2_przeplyw = mw2_przeplyw;
    }

    public double getHw_przeplyw() {
        return hw_przeplyw;
    }

    public void setHw_przeplyw(double hw_przeplyw) {
        this.hw_przeplyw = hw_przeplyw;
    }

    public int getLlw_poziom() {
        return llw_poziom;
    }

    public void setLlw_poziom(int llw_poziom) {
        this.llw_poziom = llw_poziom;
    }

    public int getLw_poziom() {
        return lw_poziom;
    }

    public void setLw_poziom(int lw_poziom) {
        this.lw_poziom = lw_poziom;
    }

    public int getMw1_poziom() {
        return mw1_poziom;
    }

    public void setMw1_poziom(int mw1_poziom) {
        this.mw1_poziom = mw1_poziom;
    }

    public int getMw2_poziom() {
        return mw2_poziom;
    }

    public void setMw2_poziom(int mw2_poziom) {
        this.mw2_poziom = mw2_poziom;
    }

    public int getHw_poziom() {
        return hw_poziom;
    }

    public void setHw_poziom(int hw_poziom) {
        this.hw_poziom = hw_poziom;
    }

    public String getNotifHint() {
        return notifHint;
    }

    public void setNotifHint(String notifHint) {
        this.notifHint = notifHint;
    }

    public int getNotifCheckedId() {
        return notifCheckedId;
    }

    public void setNotifCheckedId(int notifCheckedId) {
        this.notifCheckedId = notifCheckedId;
    }

    /*====Gettery i Settery danych dodatkowych===*/

}
