package fantomit.zwalkowepegle.interfaces;

public interface StationDetailsInterface extends MainInterface {
    void loadView(boolean loadExistingData);
    void loadDataToLevelChart();
    void loadDataToPrzeplywChart();
}
