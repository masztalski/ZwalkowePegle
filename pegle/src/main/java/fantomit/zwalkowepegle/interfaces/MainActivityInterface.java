package fantomit.zwalkowepegle.interfaces;

import java.util.Date;

public interface MainActivityInterface extends MainInterface {
    void displayRivers();
    Date getToday();
    void displayAktualizacjaDialog();
    void runAktualizacjaService();
    void displayProgress(int message, String messageAlt);

}
