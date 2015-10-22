package fantomit.zwalkowepegle.utils;

public class UsuwanieRzekiEvent {
    private boolean czyUsunac;
    private int riverPos;

    public UsuwanieRzekiEvent(boolean czyUsunac, int riverPos){
        this.czyUsunac = czyUsunac;
        this.riverPos = riverPos;
    }

    public boolean czyUsunac(){
        return czyUsunac;
    }

    public int getRiverPos(){
        return riverPos;
    }
}
