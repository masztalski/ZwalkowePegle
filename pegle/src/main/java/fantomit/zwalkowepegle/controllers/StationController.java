package fantomit.zwalkowepegle.controllers;

import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fantomit.zwalkowepegle.APImodels.MyRecord;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.BuildConfig;
import fantomit.zwalkowepegle.Statics;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.interfaces.StationDetailsInterface;
import fantomit.zwalkowepegle.webservices.WrotkaWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class StationController {

    private Station mStacja;
    private List<MyRecord> mLevelHistoricStates;
    private List<MyRecord> mPrzeplywHistoricStates;

    private StationDetailsInterface mView;
    private StationRepository repoStacja;
    private SettingsRepository repoSettings;

    private WrotkaWebService stationHistoryWS;

    public String firstRecordDate;

    public boolean isTriggerFired = false;

    @Inject
    public StationController(WrotkaWebService stationHistoryWS, StationRepository repoStacja, SettingsRepository repoSettings) {
        this.stationHistoryWS = stationHistoryWS;
        this.repoStacja = repoStacja;
        this.repoSettings = repoSettings;
    }

    public void setView(StationDetailsInterface mView) {
        this.mView = mView;
    }

    public void loadStacja(String id) {
        mLevelHistoricStates = null;
        if (mView != null) mView.showProgressSpinner();
        mStacja = repoStacja.findById(id);
        Calendar todayDate = Calendar.getInstance();
        Calendar beginDate = (Calendar) todayDate.clone();
        todayDate.add(Calendar.HOUR_OF_DAY, 1);
        beginDate.add(Calendar.MONTH, -1); //data miesi¹c wczeœniej
        beginDate.set(Calendar.HOUR_OF_DAY, 0);
        beginDate.set(Calendar.MINUTE, 0);
        beginDate.set(Calendar.SECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String today = sdf.format(todayDate.getTime());
        String begin = sdf.format(beginDate.getTime());
        loadHistoricStates(Statics._LEVEL, begin, today, true);
        loadHistoricStates(Statics._PRZEPLYW, begin, today, true);
        if (mView != null) mView.loadView(false);
    }


    public void loadHistoricStates(String kind, String begin, String end, boolean initLoading) {
        //Log.i("StationController", "LoadHistoricStates: " + kind);
        Call<List<MyRecord>> result = stationHistoryWS.getRecords(mStacja.getId(), kind, begin, end);

        result.enqueue(new Callback<List<MyRecord>>() {
            @Override
            public void onResponse(Call<List<MyRecord>> call, Response<List<MyRecord>> response) {
                List<MyRecord> myRecords = response.body();
                Log.i(StationController.class.getSimpleName(), kind + " Response parsed");
                if (kind.equals(Statics._LEVEL)) {
                    Log.i(StationController.class.getSimpleName(), "Pobrane stany");
                    if (myRecords != null && !myRecords.isEmpty()) {
                        if (mView != null) mView.displayToast("Uda³o siê pobraæ historiê poziomów rzeki");
                        if (mLevelHistoricStates != null) {
                            mLevelHistoricStates.addAll(0, myRecords);
                        } else {
                            mLevelHistoricStates = myRecords;
                        }
                        if (mView != null) {
                            mView.loadDataToLevelChart();
                            mView.hideProgressSpinner();
                        }
                    }  else {
                        if (mView != null){
                            mView.displayToast("Wybrana stacja nie posiada historii poziomów rzeki");
                            mView.hideProgressSpinner();
                        }
                    }

                } else {
                    Log.i(StationController.class.getSimpleName(), "Pobrane przeplywy");
                    if (myRecords != null && !myRecords.isEmpty()) {
                        if (mView != null) mView.displayToast("Uda³o siê pobraæ historiê przep³ywów rzeki");
                        if (mPrzeplywHistoricStates != null) {
                            mPrzeplywHistoricStates.addAll(0, myRecords);
                        } else {
                            mPrzeplywHistoricStates = myRecords;
                        }
                        if (mView != null) {
                            mView.loadDataToPrzeplywChart();
                            mView.hideProgressSpinner();
                        }
                    } else {
                        if (mView != null){
                            mView.displayToast("Wybrana stacja nie posiada historii przep³ywów rzeki");
                            mView.hideProgressSpinner();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<MyRecord>> call, Throwable throwable) {
                if (mView != null) mView.hideProgressSpinner();
                if (throwable != null) {
                    throwable.printStackTrace();
                    if (throwable.getMessage() != null)
                        Log.e("Retrofit", throwable.getMessage());
                    if (mView != null) {
                        if (throwable.getMessage() != null)
                            mView.displayToast(throwable.getMessage());
                        else
                            mView.displayToast("B³¹d po³¹czenia - spróbuj wczytaæ stacjê ponownie");
                    }
                }
            }
        });
    }

    public Station getStacja() {
        return mStacja;
    }

    public boolean isStationFav() {
        return mStacja.isFav();
    }

    public void addToFavourite() {
        Station stacja = repoStacja.findById(mStacja.getId());
        if (stacja != null) {
            mStacja.setNotifByPrzeplyw(stacja.isNotifByPrzeplyw());
            mStacja.setNotifCheckedId(stacja.getNotifCheckedId());
            mStacja.setDolnaGranicaPoziomu(stacja.getDolnaGranicaPoziomu());
            mStacja.setDolnaGranicaPrzeplywu(stacja.getDolnaGranicaPrzeplywu());
        }
        mStacja.setIsFav(true);
        boolean status = repoStacja.createOrUpdate(mStacja);
        if (mView != null) mView.displayToast(status ? "Dodano do ulubionych" : "Wyst¹pi³ b³¹d");
        if (!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent("Favourites added")
                    .putCustomAttribute("Station", mStacja.getName()));
        }
        Log.i(getClass().getSimpleName(), "ADD ULUBIONE " + (status ? "Succes" : "Fail"));
    }

    public void deleteFromFavourite() {
        mStacja.setIsFav(false);
        boolean status = repoStacja.createOrUpdate(mStacja);
        if (mView != null) mView.displayToast(status ? "Usuniêto z ulubionych" : "Wyst¹pi³ b³¹d");
        Log.i(getClass().getSimpleName(), "DELETE ULUBIONE " + (status ? "Succes" : "Fail"));
    }

    public boolean pogodynkaStatesEnabled() {
        return repoSettings.getSettings().isStanyPogodynkaEnabled();
    }

    public boolean isUserCustomized() {
        return repoStacja.findById(mStacja.getId()).isUserCustomized();
    }

    public List<MyRecord> getmLevelHistoricStates() {
        return mLevelHistoricStates;
    }

    public void setmLevelHistoricStates(List<MyRecord> mLevelHistoricStates) {
        this.mLevelHistoricStates = mLevelHistoricStates;
    }

    public List<MyRecord> getmPrzeplywHistoricStates() {
        return mPrzeplywHistoricStates;
    }

    public void setmPrzeplywHistoricStates(List<MyRecord> mPrzeplywHistoricStates) {
        this.mPrzeplywHistoricStates = mPrzeplywHistoricStates;
    }
}
