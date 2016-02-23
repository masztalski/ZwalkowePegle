package fantomit.zwalkowepegle.controllers;

import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;
import javax.inject.Singleton;

import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.APImodels.StationListObject;
import fantomit.zwalkowepegle.APImodels.WojStation;
import fantomit.zwalkowepegle.BuildConfig;
import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.DBmodels.Settings;
import fantomit.zwalkowepegle.db.repositories.RiverRepository;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.events.DataLoadedEvent;
import fantomit.zwalkowepegle.events.UsuwanieRzekiEvent;
import fantomit.zwalkowepegle.events.WojewodztwoChoosedEvent;
import fantomit.zwalkowepegle.interfaces.MainActivityInterface;
import fantomit.zwalkowepegle.webservices.PogodynkaWebService;
import fantomit.zwalkowepegle.webservices.WrotkaWebService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

@Singleton
public class MainController {
    private MainActivityInterface mView;

    private PogodynkaWebService pogodynkaWS;
    private WrotkaWebService wrotkaWS;

    private RiverRepository repoRiver;
    private StationRepository repoStacja;
    private SettingsRepository repoSettings;

    private EventBus eventBus;

    public HashMap<String, Integer> plywalnoscRzek;

    private List<WojStation> mWojewodzkieStacje;
    private List<River> mRivers;
    private boolean isDivided = false;
    public boolean isSorted = false;


    @Inject
    public MainController(PogodynkaWebService listaStacjiWebService, WrotkaWebService wrotkaWebService, RiverRepository repoRiver, StationRepository repoStacja,
                          SettingsRepository repoSettings, EventBus eventBus) {
        this.pogodynkaWS = listaStacjiWebService;
        this.wrotkaWS = wrotkaWebService;
        this.repoRiver = repoRiver;
        this.repoStacja = repoStacja;
        this.repoSettings = repoSettings;
        this.eventBus = eventBus;
    }

    public void setView(MainActivityInterface mView) {
        this.mView = mView;
        plywalnoscRzek = null;
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
        if (repoSettings.getSettings() == null) {
            repoSettings.createOrUpdate(new Settings());
        }
        if (mView == null) {
            isSorted = false;
        }
    }

    @Subscribe
    public void handle(WojewodztwoChoosedEvent event) {
        getListaStacji();
    }

    public void getListaStacji() {
        if (mView != null) mView.showProgressSpinner();
        mRivers = repoRiver.getAll();
        if (repoSettings.getSettings() == null) {
            if (mView != null)
                mView.displayToast("B³¹d bazy danych. Odinstaluj i zainstaluj aplikacjê ponownie");
            return;
        }
        if (mRivers == null || mRivers.isEmpty() || repoSettings.getSettings().isHasWojewodztwoChanged()) {
            mRivers = new ArrayList<>();
            repoRiver.deleteAll();
            isDivided = false;
            isSorted = false;

            Settings set = repoSettings.getSettings();
            set.setHasWojewodztwoChanged(false);
            repoSettings.createOrUpdate(set);

            Call<List<WojStation>> result = wrotkaWS.getStations(set.getWojewodztwo());

            result.enqueue(new Callback<List<WojStation>>() {
                @Override
                public void onResponse(Call<List<WojStation>> call, Response<List<WojStation>> response) {
                    if(response.isSuccess()) {
                        List<WojStation> stations = response.body();
                        if (!isDivided) mWojewodzkieStacje = new ArrayList<>();
                        for (WojStation stacja : stations) {
                            saveStationToRepo(stacja);
                        }
                        sortRivers(mWojewodzkieStacje);
                    } else {
                        Log.e(MainController.class.getSimpleName(),String.valueOf(response.code()));
                        Log.e(MainController.class.getSimpleName(), response.errorBody().toString());
                        if (mView != null) mView.displayToast("Przekroczono czas po³¹czenia");
                    }
                }

                @Override
                public void onFailure(Call<List<WojStation>> call, Throwable throwable) {
                    if(mView != null) mView.hideProgressSpinner();
                    if (throwable != null) {
                        throwable.printStackTrace();
                        if (throwable.getMessage() != null)
                            Log.e("Retrofit", throwable.getMessage());
                        if (mView != null) {
                            if (throwable.getMessage() != null)
                                mView.displayToast(throwable.getMessage());
                            else mView.displayToast("B³¹d po³¹czenia");
                        }
                    }
                }
            });
        } else {
            isDivided = true;
            checkPlywalnosc();
        }
    }

    private void saveStationToRepo(WojStation wojStation) {
        mWojewodzkieStacje.add(wojStation);
        Station s = repoStacja.findById(wojStation.getStation_id());
        if (s == null) {
            Station stacjaDB = new Station();
            stacjaDB.setId(wojStation.getStation_id());
            Double temp = Double.valueOf(wojStation.getLowValue());
            stacjaDB.setDolnaGranicaPoziomu(temp.intValue());
            stacjaDB.setDolnaGranicaPrzeplywu(wojStation.getLowDischargeValue());
            repoStacja.createOrUpdate(stacjaDB);
        }
    }

    private void sortRivers(List<WojStation> stacje) {
        Log.i(getClass().getSimpleName(), "sortRivers");
        if (stacje.isEmpty()) Log.e(getClass().getSimpleName(), "Brak stacji");
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
        if (!rzeki.isEmpty()) Log.i(getClass().getSimpleName(), "sortRivers-Succes");
        if (BuildConfig.DEBUG)
            Log.i(getClass().getSimpleName(), "ILOSC RZEK w " + repoSettings.getSettings().getWojewodztwo() + ": " + Integer.toString(rzeki.size()));
    }

    public List<River> getRivers() {
        return mRivers;
    }

    @Subscribe
    public void usunRzeke(UsuwanieRzekiEvent event) {
        boolean czyUsunac = event.czyUsunac();
        isSorted = false;
        if (czyUsunac) {
            for (int pos : event.getRiverPos()) {
                repoRiver.delete(mRivers.get(pos));
            }
            mRivers = repoRiver.getAll();
            if (mView != null) mView.displayRivers();
        }
    }

    public String getWojewodztwoFromSettings() {
        return repoSettings.getSettings().getWojewodztwo();
    }

    public boolean hasWojewodztwoChanged() {
        return repoSettings.getSettings().isHasWojewodztwoChanged();
    }

    @Subscribe
    public void poPobraniuDanych(DataLoadedEvent event) {
        checkPlywalnosc();
    }

    public void checkPlywalnosc() {
        Log.i(getClass().getSimpleName(), "Rozpoczêto testowanie p³ywalnoœci");
        long startTime = Calendar.getInstance().getTimeInMillis();

        plywalnoscRzek = new HashMap<>();
        Call<List<StationListObject>> result = pogodynkaWS.getListaStacji();
        result.enqueue(new Callback<List<StationListObject>>() {
            @Override
            public void onResponse(Call<List<StationListObject>> call, Response<List<StationListObject>> response) {
                if (response.isSuccess()) {
                    List<StationListObject> listaStacji = response.body();
                    Log.i(getClass().getSimpleName(), "data received");
                    Stream.of(mRivers)
                            .distinct()
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
                                            if (plywalnoscRzek != null)
                                                plywalnoscRzek.put(river.getRiverId(), i);
                                            // Log.i("PLYWALNOSC", river.getRiverName());
                                        } else {
                                            if (plywalnoscRzek != null)
                                                plywalnoscRzek.put(river.getRiverId(), i);
                                            //Log.i("PLYWALNOSC", river.getRiverName());
                                        }
                                    }
                                }
                            });
                    if (!BuildConfig.DEBUG)
                        Answers.getInstance().logCustom(new CustomEvent("P³ywalnoœæ")
                                .putCustomAttribute("TIME", (Calendar.getInstance().getTimeInMillis() - startTime) / 1000));
                    Log.i(getClass().getSimpleName(), "P³ywalnoœæ testing completed");
                    if (mView != null) mView.displayRivers();
                } else {
                    Log.e(MainController.class.getSimpleName(),String.valueOf(response.code()));
                    Log.e(MainController.class.getSimpleName(), response.errorBody().toString());
                    if (mView != null) mView.displayToast("Przekroczono czas po³¹czenia");
                }
            }

            @Override
            public void onFailure(Call<List<StationListObject>> call, Throwable throwable) {
                if(mView != null) mView.hideProgressSpinner();
                if (throwable != null) {
                    throwable.printStackTrace();
                    if (throwable.getCause() != null && throwable.getCause().getMessage() != null) Log.e("Retrofit",throwable.getCause().getMessage());
                    if (throwable.getMessage() != null) Log.e("Retrofit", throwable.getMessage());
                    else Log.e("Retrofit", "B³¹d przy po³¹czeniu do pogodynki");
                    if (mView != null) {
                        if (throwable.getMessage() != null)
                            mView.displayToast(throwable.getMessage());
                        else mView.displayToast("B³¹d po³¹czenia");
                    }
                }
            }
        });
    }
}


