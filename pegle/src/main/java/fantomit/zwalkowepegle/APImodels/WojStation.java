package fantomit.zwalkowepegle.APImodels;

public class WojStation {
    private String station_id;
    private String wojewodztwo;
    private double lowValue;
    private double lowDischargeValue;
    private String river;

    public String getStation_id() {
        return station_id;
    }

    public void setStation_id(String station_id) {
        this.station_id = station_id;
    }

    public String getWojewodztwo() {
        return wojewodztwo;
    }

    public void setWojewodztwo(String wojewodztwo) {
        this.wojewodztwo = wojewodztwo;
    }

    public double getLowValue() {
        return lowValue;
    }

    public void setLowValue(double lowValue) {
        this.lowValue = lowValue;
    }

    public double getLowDischargeValue() {
        return lowDischargeValue;
    }

    public void setLowDischargeValue(double lowDischargeValue) {
        this.lowDischargeValue = lowDischargeValue;
    }

    public String getRiver() {
        return river;
    }

    public void setRiver(String river) {
        this.river = river;
    }
}
