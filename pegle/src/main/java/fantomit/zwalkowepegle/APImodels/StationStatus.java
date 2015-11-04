package fantomit.zwalkowepegle.APImodels;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable
public class StationStatus implements Serializable {
    @DatabaseField
    private String name;    //ID stacji
    @DatabaseField
    private String description; //nazwa stacji
    @DatabaseField
    private String river;   //nazwa rzeki; odczytując odcinać nawiasy z końca z zawartościa
    @DatabaseField
    private String parameter;   //??
    @DatabaseField
    private String currentDate; //aktualna data
    @DatabaseField
    private int currentValue;   //aktualny stan wody
    @DatabaseField
    private int previousValue;  //poprzedni stan wody
    @DatabaseField
    private String previousDate;    //poprzednia data
    @DatabaseField
    private String state;   //??
    @DatabaseField
    private double riverCourseKm;
    @DatabaseField
    private double catchmentArea;
    @DatabaseField
    private int trend;  //nie ma potrzeby korzystać
    @DatabaseField
    private double waterGaugeZeroOrdinate;
    @DatabaseField
    private boolean isFromObs;
    @DatabaseField
    private String province;    //dolnośląskie
    @DatabaseField
    private double alarmValue;
    @DatabaseField
    private double warningValue;
    @DatabaseField
    private double lowValue;    //?granica dolna stanów średnich
    @DatabaseField
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
