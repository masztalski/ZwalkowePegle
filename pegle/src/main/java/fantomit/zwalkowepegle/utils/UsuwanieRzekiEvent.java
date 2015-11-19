package fantomit.zwalkowepegle.utils;

import java.util.ArrayList;

public class UsuwanieRzekiEvent {
    private boolean czyUsunac;
    private ArrayList<Integer> riverPos;

    public UsuwanieRzekiEvent(boolean czyUsunac, ArrayList<Integer> riverPos) {
        this.czyUsunac = czyUsunac;
        this.riverPos = riverPos;
    }

    public boolean czyUsunac() {
        return czyUsunac;
    }

    public ArrayList<Integer> getRiverPos() {
        return riverPos;
    }
}
