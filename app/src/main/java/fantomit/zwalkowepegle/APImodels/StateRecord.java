package fantomit.zwalkowepegle.APImodels;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable
public class StateRecord implements Serializable {
    @DatabaseField
    private String state;
    @DatabaseField
    private String date;
    @DatabaseField
    private double value;

    public String getState() {
        return state;
    }

    public String getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }
}
