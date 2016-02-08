package fantomit.zwalkowepegle.controllers;

import android.util.Log;

import com.annimon.stream.Stream;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.db.repositories.RiverRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.events.StationsLoadedEvent;
import fantomit.zwalkowepegle.interfaces.RiverStationsInterface;
import fantomit.zwalkowepegle.webservices.PogodynkaWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class RiverStationsController {

    private PogodynkaWebService pogodynkaWS;
    private RiverRepository repoRiver;
    private StationRepository repoStacja;
    private RiverStationsInterface mView;

    public boolean isSorted = false;

    private String riverName;
    private int riverId;

    private List<Station> stations;

    @Inject
    public RiverStationsController(PogodynkaWebService stacjaWebService, RiverRepository riverRepository, StationRepository stationRepository) {
        this.pogodynkaWS = stacjaWebService;
        this.repoRiver = riverRepository;
        this.repoStacja = stationRepository;
    }

    public void setView(RiverStationsInterface mView) {
        this.mView = mView;
    }

    public void loadStations() {
        if (mView != null) mView.showProgressSpinner();
        if (stations == null) {
            stations = new ArrayList<>();
        } else {
            stations.clear();
        }
        River r = repoRiver.findById(riverId);
        riverName = r.getRiverName();
        List<String> stationsId = r.getConnectedStations();
        Stream.of(stationsId)
                .forEach(id -> getStacja(id, stationsId.size()));
    }

    public void getStacja(String id, final int size) {
        Call<Station> result = pogodynkaWS.getStacja(id);

        result.enqueue(new Callback<Station>() {
            @Override
            public void onResponse(Call<Station> call, Response<Station> response) {
                Station station = response.body();
                Log.i("Retrofit", "Pobrano stacjê " + station.getName());
                stations.add(station);
                if (repoStacja.findById(station.getId()) != null) {
                    Station s = repoStacja.findById(station.getId());
                    station.setIsFav(s.isFav());
                    station.setNotifByPrzeplyw(s.isNotifByPrzeplyw());
                    station.setNotifHint(s.getNotifHint());
                    station.setNotifCheckedId(s.getNotifCheckedId());
                    station.setDolnaGranicaPoziomu(s.getDolnaGranicaPoziomu());
                    station.setDolnaGranicaPrzeplywu(s.getDolnaGranicaPrzeplywu());
                    station.setLan(s.getLan());
                    station.setLon(s.getLon());
                    if (s.isUserCustomized()) {
                        station.setIsUserCustomized(true);
                        station.setLw_poziom(s.getLw_poziom());
                        station.setLw_przeplyw(s.getLw_przeplyw());
                        station.setMw2_poziom(s.getMw2_poziom());
                        station.setMw2_przeplyw(s.getMw2_przeplyw());
                        station.setHw_poziom(s.getHw_poziom());
                        station.setHw_przeplyw(s.getHw_przeplyw());
                    }
                }
                repoStacja.createOrUpdate(station);
                if (stations.size() == size) EventBus.getDefault().post(new StationsLoadedEvent());
            }

            @Override
            public void onFailure(Call<Station> call, Throwable throwable) {
                if (throwable != null) {
                    if (throwable.getMessage() != null) Log.e("Retrofit", throwable.getMessage());
                    if (mView != null) {
                        if (throwable.getMessage() != null)
                            mView.displayToast(throwable.getMessage());
                        else mView.displayToast("B³¹d po³¹czenia");
                    }
                }
            }
        });
    }

    public List<Station> getStations() {
        return stations;
    }

    public String getRiverName() {
        return riverName;
    }

    public void setRiverId(int riverId) {
        this.riverId = riverId;
    }
}
