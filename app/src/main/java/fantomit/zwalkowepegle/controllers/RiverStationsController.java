package fantomit.zwalkowepegle.controllers;

import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.db.repositories.RiverRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.interfaces.RiverStationsInterface;
import fantomit.zwalkowepegle.utils.RetroFitErrorHelper;
import fantomit.zwalkowepegle.utils.StationsLoadedEvent;
import fantomit.zwalkowepegle.webservices.StacjaWebService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

@Singleton
public class RiverStationsController {

    @Inject
    private StacjaWebService stacjaWS;
    @Inject
    private RiverRepository repoRiver;
    @Inject
    private StationRepository repoStacja;
    @Inject
    private EventBus eventBus;
    private RiverStationsInterface mView;

    public boolean isSorted = false;

    private String riverName;

    private List<Station> stations;

    public void setView(RiverStationsInterface mView) {
        this.mView = mView;
    }

    public void getStacja(String id, final int size) {
        Observable<Station> result = stacjaWS.getStacja(id);

        result.observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Station>() {
            @Override
            public void call(Station station) {
                Log.e("Retrofit", "Pobrano stacjê " + station.getName());
                stations.add(station);
                if(repoStacja.findById(station.getId()) != null){
                    Station s = repoStacja.findById(station.getId());
                    station.setIsFav(s.isFav());
                    station.setNotifByPrzeplyw(s.isNotifByPrzeplyw());
                    station.setNotifHint(s.getNotifHint());
                    station.setNotifCheckedId(s.getNotifCheckedId());
                    station.setDolnaGranicaPoziomu(s.getDolnaGranicaPoziomu());
                    station.setDolnaGranicaPrzeplywu(s.getDolnaGranicaPrzeplywu());
                    if (s.isUserCustomized()) {
                        station.setIsUserCustomized(true);
                        station.setLlw_poziom(s.getLlw_poziom());
                        station.setLlw_przeplyw(s.getLlw_przeplyw());
                        station.setLw_poziom(s.getLw_poziom());
                        station.setLw_przeplyw(s.getLw_przeplyw());
                        station.setMw1_poziom(s.getMw1_poziom());
                        station.setMw1_przeplyw(s.getMw1_przeplyw());
                        station.setMw2_poziom(s.getMw2_poziom());
                        station.setMw2_przeplyw(s.getMw2_przeplyw());
                        station.setHw_poziom(s.getHw_poziom());
                        station.setHw_przeplyw(s.getHw_przeplyw());
                    }
                }
                repoStacja.createOrUpdate(station);
                if (stations.size() == size) eventBus.post(new StationsLoadedEvent());
            }
        }, new RetroFitErrorHelper(mView));
    }


    public void loadStations(int riverId) {
        mView.showProgressSpinner();
        if(stations == null) {
            stations = new ArrayList<>();
        } else {
            stations.clear();
        }
        River r = repoRiver.findById(riverId);
        riverName = r.getRiverName();
        List<String> stationsId = r.getConnectedStations();
        for (String id : stationsId) {
            getStacja(id, stationsId.size());
        }
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public String getRiverName() {
        return riverName;
    }
}
