package fantomit.zwalkowepegle.APImodels;

public class StationListObject {
    /*========Model API=========*/
    private String cd;  //Aktualna data
    private int cv;  //Aktualny poziom wody
    private String i;   //ID stacji
    private String n;   //nazwa stacji
    private int a;  //??
    private String s;   //opisowy stan wody na stacji -> Constants->hydroStatusLabels
    private double lo;  //Longitude
    private double la;  //Langitude

    public String getData() {
        return cd;
    }

    public int getPoziom() {
        return cv;
    }

    public String getId() {
        return i;
    }

    public String getName() {
        return n;
    }

    public int getA() {
        return a;
    }

    public String getState() {
        return s;
    }

    public double getLongitude() {
        return lo;
    }

    public double getLangitude() {
        return la;
    }
}
