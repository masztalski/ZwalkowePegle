package fantomit.zwalkowepegle.utils;

public class StationDownloadedEvent {
    private String stationName;

    public StationDownloadedEvent(String name){
        this.stationName = name;
    }

    public String getStationName(){
        return this.stationName;
    }
}
