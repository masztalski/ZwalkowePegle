package fantomit.zwalkowepegle.APImodels;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable
public class PrzeplywRecord implements Serializable {
    @DatabaseField
    private String date;
    @DatabaseField
    private double value;

    public String getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }
}
