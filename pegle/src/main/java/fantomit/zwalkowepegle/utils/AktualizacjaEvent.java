package fantomit.zwalkowepegle.utils;

/**
 * Created by mmar12 on 2015-10-12.
 */
public class AktualizacjaEvent {
    boolean czyPobrac;

    public AktualizacjaEvent(boolean czyPobrac) {
        this.czyPobrac = czyPobrac;
    }

    public boolean czyPobrac() {
        return czyPobrac;
    }

    public void setCzyPobrac(boolean czyPobrac) {
        this.czyPobrac = czyPobrac;
    }
}
