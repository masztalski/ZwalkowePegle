package fantomit.zwalkowepegle.controllers;

import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.FutureTask;

import de.greenrobot.event.EventBus;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.APImodels.StationListObject;
import fantomit.zwalkowepegle.APImodels.WojStation;
import fantomit.zwalkowepegle.BuildConfig;
import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.DBmodels.Settings;
import fantomit.zwalkowepegle.R;
import fantomit.zwalkowepegle.db.repositories.RiverRepository;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.interfaces.MainActivityInterface;
import fantomit.zwalkowepegle.utils.AktualizacjaEvent;
import fantomit.zwalkowepegle.utils.RetroFitErrorHelper;
import fantomit.zwalkowepegle.utils.UsuwanieRzekiEvent;
import fantomit.zwalkowepegle.utils.WojewodztwoChoosedEvent;
import fantomit.zwalkowepegle.webservices.ListaStacjiWebService;
import fantomit.zwalkowepegle.webservices.StacjaWebService;
import fantomit.zwalkowepegle.webservices.WrotkaWebService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Singleton
public class MainController {
    private MainActivityInterface mView;

    @Inject
    private ListaStacjiWebService listaWS;
    @Inject
    private StacjaWebService stacjaWS;
    @Inject
    private WrotkaWebService wrotkaWS;
    @Inject
    private RiverRepository repoRiver;
    @Inject
    private StationRepository repoStacja;
    @Inject
    private SettingsRepository repoSettings;

    @Inject
    private EventBus eventBus;

    public HashMap<String, Integer> plywalnoscRzek;

    private List<StationListObject> mListaStacji;
    private List<WojStation> mWojewodzkieStacje;
    private List<River> mRivers;
    private int howManyStationsTested = 0;
    private boolean isDivided = false;
    public boolean isSorted = false;
    private String mWojewodztwo = "";

    public void setView(MainActivityInterface mView) {
        this.mView = mView;
        plywalnoscRzek = null;
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
        if (repoSettings.getSettings() == null) {
            repoSettings.createOrUpdate(new Settings());
        }
        if(mView == null){
            isSorted = false;
        }
    }

    public void onEvent(WojewodztwoChoosedEvent event){
        mWojewodztwo = event.wojewodztwo;
        getListaStacji();
    }

    public void getListaStacji() {
        mView.showProgressSpinner();
        mRivers = repoRiver.getAll();
        if (repoSettings.getSettings() == null) {
            mView.displayToast("B³¹d bazy danych. Odinstaluj i zainstaluj aplikacjê ponownie");
            return;
        }
        if (mRivers == null || mRivers.isEmpty() || repoSettings.getSettings().isHasWojewodztwoChanged()) {
            mWojewodztwo = repoSettings.getSettings().getWojewodztwo();
            mRivers.clear();
            repoRiver.deleteAll();
            isDivided = false;
            isSorted = false;

            Settings set = repoSettings.getSettings();
            set.setHasWojewodztwoChanged(false);
            repoSettings.createOrUpdate(set);

            Observable<List<WojStation>> result = wrotkaWS.getStations(set.getWojewodztwo());

            result.observeOn(AndroidSchedulers.mainThread()).subscribe(stations -> {
                Log.i("Retrofit", "success");
                howManyStationsTested = 0;
                if (!isDivided) mWojewodzkieStacje = new ArrayList<>();
                for(WojStation stacja : stations){
                    saveStationToRepo(stacja);
                    //getStacja(stacja.getStation_id(), stations.size());
                }
                sortRivers(mWojewodzkieStacje);
            }, new RetroFitErrorHelper(null));
        } else {
            isDivided = true;
            checkPlywalnosc();
        }
    }

//    private void setWojewodztwo() {
//        if (BuildConfig.DEBUG)
//            Log.e("FANTOM", "Set " + repoSettings.getSettings().getWojewodztwo());
//        howManyStationsTested = 0;
//        if (mView != null)
//            mView.displayProgress(R.string.msg_setWojewodztwo, null);
//        Stream.of(mListaStacji)
//                .forEach((StationListObject s) -> getStacja(s.getId()));
//
//        if (BuildConfig.DEBUG)
//            Log.e("DOWNLOAD", "Downloaded " + Integer.toString(mListaStacji.size()) + " stations");
//    }

    public void getStacja(String id, int size ) {
        Observable<Station> result = stacjaWS.getStacja(id);
        if (!isDivided) mWojewodzkieStacje = new ArrayList<>();

        result.observeOn(AndroidSchedulers.mainThread())
                .subscribe(station -> {
                    if (station != null && !isDivided) {
                        howManyStationsTested++;
                        if (mView != null)
                            mView.displayProgress(0, "Stacja " + station.getName() + " dopasowana");


                        if (howManyStationsTested == size) {
                            sortRivers(mWojewodzkieStacje);
                            if (BuildConfig.DEBUG)
                                Log.e("TESTED", "Znaleziono: " + Integer.toString(mWojewodzkieStacje.size())
                                        + " stacji dla woj. " + repoSettings.getSettings().getWojewodztwo());
                        }
                    }
                }, new RetroFitErrorHelper(mView));
    }

    private void saveStationToRepo(WojStation wojStation){
        mWojewodzkieStacje.add(wojStation);
        Station s = repoStacja.findById(wojStation.getStation_id());
//        if (s != null) {
//            station.setIsFav(s.isFav());
//            station.setIsUserCustomized(s.isUserCustomized());
//            station.setNotifByPrzeplyw(s.isNotifByPrzeplyw());
//            station.setNotifCheckedId(s.getNotifCheckedId());
//            if (s.getDolnaGranicaPrzeplywu() != -1.0) {
//                station.setDolnaGranicaPrzeplywu(s.getDolnaGranicaPrzeplywu());
//            } else {
//                station.setDolnaGranicaPrzeplywu(s.getLw_przeplyw() != -1.0 ? s.getLw_przeplyw() : s.getLowDischargeValue());
//            }
//            if (s.getDolnaGranicaPoziomu() != -1) {
//                station.setDolnaGranicaPoziomu(s.getDolnaGranicaPoziomu());
//            } else {
//                Double temp = Double.valueOf(s.getStatus().getLowValue());
//                station.setDolnaGranicaPoziomu(s.getLw_poziom() != -1 ? s.getLw_poziom() : temp.intValue());
//            }
//        } else
        if(s == null){
            Station stacjaDB = new Station();
            stacjaDB.setId(wojStation.getStation_id());
            Double temp = Double.valueOf(wojStation.getLowValue());
            stacjaDB.setDolnaGranicaPoziomu(temp.intValue());
            stacjaDB.setDolnaGranicaPrzeplywu(wojStation.getLowDischargeValue());
            repoStacja.createOrUpdate(stacjaDB);
        }
    }

    private void sortRivers(List<WojStation> stacje) {
        Log.e("FANTOM", "sortRivers");
        if (mView != null)
            mView.displayProgress(R.string.msg_SortRivers, null);
        if (stacje.isEmpty()) Log.e("FANTOM", "brak stacji");
        List<River> rzeki = new ArrayList<>();
        List<String> nazwyRzek = new ArrayList<>();
        FutureTask<List<River>> sorting = new FutureTask<>(() -> {
            Stream.of(stacje)
                    .forEach(stacja -> {
                        String riverName = stacja.getRiver();
                        if (!nazwyRzek.contains(riverName)) {
                            River r = new River();
                            r.setRiverName(riverName);
                            r.addConnectedStation(stacja.getStation_id());
                            nazwyRzek.add(riverName);
                            rzeki.add(r);
                        } else {
                            int i = 0;
                            for (River r : rzeki) {
                                if (r.getRiverName().equals(riverName)) {
                                    r.addConnectedStation(stacja.getStation_id());
                                    rzeki.set(i, r);
                                }
                                i++;
                            }
                        }
                    });
            return rzeki;
        });
        Observable<List<River>> observable = Observable.from(sorting);

        Schedulers.computation().createWorker().schedule(() -> sorting.run());

        observable.subscribe((rivers) -> {
            mRivers = rivers;
            Stream.of(rivers).forEach(r -> repoRiver.createOrUpdate(r));
            checkPlywalnosc();
        });



        nazwyRzek.clear();
        if (BuildConfig.DEBUG) if (!rzeki.isEmpty()) Log.e("FANTOM", "sortRivers-Succes");
        if (BuildConfig.DEBUG)
            Log.e("ILOSC RZEK w " + repoSettings.getSettings().getWojewodztwo(), Integer.toString(rzeki.size()));
    }


    public List<River> getRivers() {
        return mRivers;
    }

    public void onEvent(UsuwanieRzekiEvent event) {
        boolean czyUsunac = event.czyUsunac();
        isSorted = false;
        if (czyUsunac) {
            for(int pos : event.getRiverPos()){
                repoRiver.delete(mRivers.get(pos));
            }
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

    public void checkPlywalnosc() {
        Log.e("Plywalnosc", "Rozpoczêto testowanie p³ywalnoœci");
        long startTime = Calendar.getInstance().getTimeInMillis();

        plywalnoscRzek = new HashMap<>();
        Observable<List<StationListObject>> result = listaWS.getListaStacji();
        result.observeOn(AndroidSchedulers.mainThread()).subscribe(listaStacji -> {
                    Log.e("PLYWALNOSC", "data received");
                    Stream.of(mRivers)
                            .forEach(river -> {
                                int i = 0;
                                List<StationListObject> filteredList = Stream.of(listaStacji)
                                        .filter(s -> river.getConnectedStations().contains(s.getId()))
                                        .collect(Collectors.toList());
                                for (StationListObject station : filteredList) {
                                    Station s = repoStacja.findById(station.getId());
                                    if (s != null) {
                                        s.setLan(station.getLangitude());
                                        s.setLon(station.getLongitude());
                                        repoStacja.createOrUpdate(s);
                                        if (station.getPoziom() >= s.getDolnaGranicaPoziomu()) {
                                            i++;
                                            plywalnoscRzek.put(river.getRiverId(), i);
                                            // Log.e("PLYWALNOSC", river.getRiverName());
                                        } else {
                                            plywalnoscRzek.put(river.getRiverId(), i);
                                            //Log.e("PLYWALNOSC", river.getRiverName());
                                        }
                                    }
                                }
                            });
                    Answers.getInstance().logCustom(new CustomEvent("P³ywalnoœæ")
                    .putCustomAttribute("TIME", (Calendar.getInstance().getTimeInMillis() - startTime)/1000));
                    Log.e("PLYWALNOSC", "completed");
                    mView.displayRivers();
                }, new RetroFitErrorHelper(mView)
        );
    }
}


