package fantomit.zwalkowepegle.controllers;

import android.util.Log;

import com.annimon.stream.Stream;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.APImodels.StationListObject;
import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.DBmodels.Settings;
import fantomit.zwalkowepegle.R;
import fantomit.zwalkowepegle.db.repositories.RiverRepository;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.interfaces.MainActivityInterface;
import fantomit.zwalkowepegle.utils.AktualizacjaEvent;
import fantomit.zwalkowepegle.utils.LastDownloadEvent;
import fantomit.zwalkowepegle.utils.RetroFitErrorHelper;
import fantomit.zwalkowepegle.utils.UsuwanieRzekiEvent;
import fantomit.zwalkowepegle.webservices.ListaStacjiWebService;
import fantomit.zwalkowepegle.webservices.MyWebService;
import fantomit.zwalkowepegle.webservices.StacjaWebService;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

@Singleton
public class MainController {
    private MainActivityInterface mView;

    @Inject
    private ListaStacjiWebService listaWS;
    @Inject
    private StacjaWebService stacjaWS;
    @Inject
    private RiverRepository repoRiver;
    @Inject
    private StationRepository repoStacja;
    @Inject
    private SettingsRepository repoSettings;

    @Inject
    private EventBus eventBus;

    private List<StationListObject> mListaStacji;
    private List<Station> mWojewodzkieStacje;
    private List<River> mRivers;
    private Station mStacja;
    private int howManyStationsTested = 0;
    private boolean isDivided = false;
    public boolean isSorted = false;

    public void setView(MainActivityInterface mView) {
        this.mView = mView;
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
        if (repoSettings.getSettings() == null) {
            repoSettings.createOrUpdate(new Settings());
        }
    }

    public void getListaStacji() {
        mView.showProgressSpinner();
        mRivers = repoRiver.getAll();
        if (mRivers == null || mRivers.isEmpty() || repoSettings.getSettings().isHasWojewodztwoChanged()) {
            mRivers.clear();
            repoRiver.deleteAll();
            isDivided = false;
            isSorted = false;
            final Observable<List<StationListObject>> result = listaWS.getListaStacji();
            Settings set = repoSettings.getSettings();
            set.setHasWojewodztwoChanged(false);
            repoSettings.createOrUpdate(set);

            result.observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<StationListObject>>() {
                @Override
                public void call(List<StationListObject> stationListObjects) {
                    Log.i("Retrofit", "success");
                    eventBus.post(new LastDownloadEvent(mView.getToday().toString()));
                    mListaStacji = stationListObjects;
                    if (mListaStacji != null && !mListaStacji.isEmpty()) {
                        setWojewodztwo();
                    } else {
                        Log.e("FANTOM", "brak pobranych stacji");
                    }
                }
            }, new RetroFitErrorHelper(mView));
        } else {
            isDivided = true;
            mView.displayRivers();
        }
    }

    public void getStacja(String id) {
        Observable<Station> result = stacjaWS.getStacja(id);
        if (!isDivided) mWojewodzkieStacje = new ArrayList<>();

        result.observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Station>() {
            @Override
            public void call(Station station) {
                mStacja = station;
                if (mStacja != null && !isDivided) {
                    //Log.e("FANTOM", "test stacji " + mStacja.getName());
                    howManyStationsTested++;
                    if (repoSettings.getSettings().getWojewodztwo().equals(mStacja.getStatus().getProvince())) {
                        Log.e("SORTED", "Stacja " + mStacja.getName() + " dopasowana");
                        if (mView != null)
                            mView.displayProgress(0, "Stacja " + mStacja.getName() + " dopasowana");
                        mWojewodzkieStacje.add(mStacja);
                        if (repoStacja.findById(mStacja.getId()) != null) {
                            Station s = repoStacja.findById(station.getId());
                            station.setIsFav(s.isFav());
                            station.setIsUserCustomized(s.isUserCustomized());
                            station.setNotifByPrzeplyw(s.isNotifByPrzeplyw());
                            station.setNotifCheckedId(s.getNotifCheckedId());
                            if (s.getDolnaGranicaPrzeplywu() != -1.0) {
                                station.setDolnaGranicaPrzeplywu(s.getDolnaGranicaPrzeplywu());
                            } else {
                                station.setDolnaGranicaPrzeplywu(s.getLw_przeplyw() != -1.0 ? s.getLw_przeplyw() : s.getLowDischargeValue());
                            }

                            if (s.getDolnaGranicaPoziomu() != -1) {
                                station.setDolnaGranicaPoziomu(s.getDolnaGranicaPoziomu());
                            } else {
                                Double temp = new Double(s.getStatus().getLowValue());
                                station.setDolnaGranicaPoziomu(s.getLw_poziom() != -1 ? s.getLw_poziom() : temp.intValue());
                            }
                        } else {
                            Double temp = new Double(station.getStatus().getLowValue());
                            station.setDolnaGranicaPoziomu(temp.intValue());
                            station.setDolnaGranicaPrzeplywu(station.getLowDischargeValue());
                        }
                        repoStacja.createOrUpdate(mStacja);
                    }
                    int progress = (howManyStationsTested / mListaStacji.size()) * 100;
                    if (progress == 25) {
                        Log.e("TESTED:", "25%");
                    }
                    if (progress == 50) {
                        Log.e("TESTED:", "50%");
                    }
                    if (progress == 75) {
                        Log.e("TESTED:", "75%");
                    }
                    if (progress == 100) {
                        Log.e("TESTED:", "100%");
                    }

                    if (howManyStationsTested == mListaStacji.size()) {
                        Log.e("TESTED", "Znaleziono: " + Integer.toString(mWojewodzkieStacje.size())
                                + " stacji dla woj. " + repoSettings.getSettings().getWojewodztwo());
                        sortRivers(mWojewodzkieStacje);
                    }
                }
            }
        }, new RetroFitErrorHelper(mView));
    }

    private void setWojewodztwo() {
        Log.e("FANTOM", "Set " + repoSettings.getSettings().getWojewodztwo());
        howManyStationsTested = 0;
        if (mView != null)
            mView.displayProgress(R.string.msg_setWojewodztwo, null);
        Stream.of(mListaStacji)
                .forEach((StationListObject s) -> getStacja(s.getId()));
        Log.e("DOWNLOAD", "Downloaded " + Integer.toString(mListaStacji.size()) + " stations");
        if (mView != null)
            mView.displayProgress(0, "Pobrano " + Integer.toString(mListaStacji.size()) + " stacji do posortowania");
    }

    private void sortRivers(List<Station> stacje) {
        Log.e("FANTOM", "sortRivers");
        if (mView != null)
            mView.displayProgress(R.string.msg_SortRivers, null);
        if (stacje.isEmpty()) Log.e("FANTOM", "brak stacji");
        List<Station> customStations = new ArrayList<>();
        List<River> rzeki = new ArrayList<>();
        List<String> nazwyRzek = new ArrayList<>();
        Stream.of(stacje)
                .forEach(stacja -> {
                    String riverName = stacja.getStatus().getRiver();
                    if (!nazwyRzek.contains(riverName)) {
                        River r = new River();
                        r.setRiverName(riverName);
                        r.addConnectedStation(stacja.getId());
                        r.setTrend(stacja.getTrend());
                        nazwyRzek.add(riverName);
                        rzeki.add(r);
                    } else {
                        int i = 0;
                        for (River r : rzeki) {
                            if (r.getRiverName().equals(riverName)) {
                                r.addConnectedStation(stacja.getId());
                                r.setTrend(stacja.getTrend());
                                rzeki.set(i, r);
                            }
                            i++;
                        }
                    }
                });
        nazwyRzek.clear();
        if (!rzeki.isEmpty()) Log.e("FANTOM", "sortRivers-Succes");
        Log.e("ILOSC RZEK w " + repoSettings.getSettings().getWojewodztwo(), Integer.toString(rzeki.size()));
        mRivers = rzeki;

        mView.displayRivers();
        for (River r : rzeki) {
            repoRiver.createOrUpdate(r);
        }
    }


    public List<River> getRivers() {
        return mRivers;
    }

    public void onEvent(UsuwanieRzekiEvent event) {
        boolean czyUsunac = event.czyUsunac();
        if (czyUsunac) {
            repoRiver.delete(mRivers.get(event.getRiverPos()));
            mRivers = repoRiver.getAll();
            mView.displayRivers();
        }
    }

    public String getWojewodztwoFromSettings() {
        return repoSettings.getSettings().getWojewodztwo();
    }

    public boolean hasWojewodztwoChanged() {
        return repoSettings.getSettings().isHasWojewodztwoChanged();
    }

    public void onEvent(AktualizacjaEvent event) {
        if (!event.czyPobrac()) {
            mView.displayAktualizacjaDialog();
        }
    }

    public HashMap checkPlywalnosc() {
        Log.e("PLYWALNOSC", "STARTED");
        HashMap<String, Integer> plywalnoscRzek = new HashMap<>();
        List<StationListObject> listaStacji = listaWS.getListaStacji().toBlocking().first();

        Stream.of(mRivers)
                .forEach(river -> {
                    List<String> connectedStations = river.getConnectedStations();
                    int i = 0;
                    for (StationListObject station : listaStacji) {
                        if (connectedStations.contains(station.getId())) {
                            if(repoStacja.findById(station.getId()) != null) {
                                if (station.getPoziom() >= repoStacja.findById(station.getId()).getDolnaGranicaPoziomu()) {
                                    i++;
                                    plywalnoscRzek.put(river.getRiverId(), i);
                                } else {
                                    plywalnoscRzek.put(river.getRiverId(), i);
                                }
                            }
                        }
                    }
                });
        Log.e("PLYWALNOSC", "COMPLETED");
        return plywalnoscRzek;
    }

}
