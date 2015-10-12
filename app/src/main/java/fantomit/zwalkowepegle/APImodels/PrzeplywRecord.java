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
    @DatabaseField
    private int dreId;
    @DatabaseField
    private String operationId;
    @DatabaseField
    private String parameterId;
    @DatabaseField
    private int versionId;
    @DatabaseField
    private long id;

    public String getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }

    public int getDreId() {
        return dreId;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getParameterId() {
        return parameterId;
    }

    public int getVersionId() {
        return versionId;
    }

    public long getId() {
        return id;
    }
}
