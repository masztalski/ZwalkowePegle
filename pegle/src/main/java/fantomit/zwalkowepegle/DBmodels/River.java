package fantomit.zwalkowepegle.DBmodels;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;

@DatabaseTable
public class River {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String riverName;
    @DatabaseField(id = false, foreign = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<String> connectedStations; //id przypisanych stacji
    @DatabaseField
    private String trend;
    /*===Custom Data===*/
    @DatabaseField
    private String idStacjiCharakterycznej = "-1";

    public River() {
        connectedStations = new ArrayList<>();
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
        return this.riverName.substring(0, this.riverName.lastIndexOf('(') - 1);
    }

    public String getRiverId() {
        String temp = this.riverName.substring(riverName.lastIndexOf('(') + 1, riverName.lastIndexOf(')'));
        return temp;
    }

    public void setRiverName(String riverName) {
        this.riverName = riverName;
    }

    public ArrayList<String> getConnectedStations() {
        return this.connectedStations;
    }

    public void addConnectedStation(String id) {
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
