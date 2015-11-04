package fantomit.zwalkowepegle.DBmodels;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Settings {
    @DatabaseField(generatedId = false, id = true)
    private int id = 1;
    @DatabaseField
    private boolean notificationEnabled = true; //brak pobierania ulubionych - nie ma to sensu wtedy
    @DatabaseField
    private String wojewodztwo = "dolnoœl¹skie";
    @DatabaseField
    private boolean hasWojewodztwoChanged = false;
    @DatabaseField
    private int time = 60;
    @DatabaseField
    private int wojPos = 0;
    @DatabaseField
    private boolean stanyPogodynkaEnabled = false;

    public Settings() {
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public String getWojewodztwo() {
        return wojewodztwo;
    }

    public void setWojewodztwo(String wojewodztwo) {
        this.wojewodztwo = wojewodztwo;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWojPos() {
        return wojPos;
    }

    public void setWojPos(int wojPos) {
        this.wojPos = wojPos;
    }

    public boolean isHasWojewodztwoChanged() {
        return hasWojewodztwoChanged;
    }

    public void setHasWojewodztwoChanged(boolean hasWojewodztwoChanged) {
        this.hasWojewodztwoChanged = hasWojewodztwoChanged;
    }

    public boolean isStanyPogodynkaEnabled() {
        return stanyPogodynkaEnabled;
    }

    public void setStanyPogodynkaEnabled(boolean stanyPogodynkaEnabled) {
        this.stanyPogodynkaEnabled = stanyPogodynkaEnabled;
    }
}
