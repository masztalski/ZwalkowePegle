package fantomit.zwalkowepegle.utils;

public class LastDownloadEvent {
    private String date;

    public LastDownloadEvent(String date){
        this.date = date;
    }

    public String getDate(){
        return date;
    }
}
