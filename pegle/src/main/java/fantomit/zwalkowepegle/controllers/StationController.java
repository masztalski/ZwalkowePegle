package fantomit.zwalkowepegle.controllers;

import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.greenrobot.event.EventBus;
import fantomit.zwalkowepegle.APImodels.MyRecord;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.BuildConfig;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.interfaces.StationDetailsInterface;
import fantomit.zwalkowepegle.utils.RetroFitErrorHelper;
import fantomit.zwalkowepegle.webservices.StationHistoryWebService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@Singleton
public class StationController {

    private Station mStacja;
    private List<MyRecord> mLevelHistoricStates;
    private List<MyRecord> mPrzeplywHistoricStates;

    private StationDetailsInterface mView;
    @Inject
    private StationRepository repoStacja;
    @Inject
    private SettingsRepository repoSettings;

    @Inject
    private StationHistoryWebService stationHistoryWS;

    @Inject
    private EventBus eventBus;

    public boolean isTriggerFired = false;

    public void setView(StationDetailsInterface mView) {
        this.mView = mView;
    }

    public void loadStacja(String id) {
        mLevelHistoricStates = null;
        mView.showProgressSpinner();
        mStacja = repoStacja.findById(id);
        Calendar todayDate = Calendar.getInstance();
        todayDate.add(Calendar.HOUR_OF_DAY,1);
        Calendar beginDate = Calendar.getInstance();
        beginDate.add(Calendar.MONTH, -1); //data miesi¹c wczeœniej
        beginDate.set(Calendar.HOUR_OF_DAY, 0);
        beginDate.set(Calendar.MINUTE, 0);
        beginDate.set(Calendar.SECOND, 0);
        String today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(todayDate.getTime());
        String begin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(beginDate.getTime());
        loadHistoricStates("level", begin, today, true);
        loadHistoricStates("przeplyw", begin, today, true);
        mView.loadView(false);
    }

    public void loadHistoricStates(String kind, String begin, String end, boolean initLoading) {
        Log.e("StationController", "LoadHistoricStates: " + kind);
        Observable<List<MyRecord>> result = stationHistoryWS.getRecords(mStacja.getId(), kind, begin, end);
        result.observeOn(AndroidSchedulers.mainThread()).subscribe(myRecords -> {
            if(kind.equals("level")) {
                if (mLevelHistoricStates != null) {
                    this.mLevelHistoricStates.addAll(0, myRecords);
                } else {
                    this.mLevelHistoricStates = myRecords;
                }
                mView.loadDataToLevelChart();
            } else  {
                if (getmPrzeplywHistoricStates() != null) {
                    this.getmPrzeplywHistoricStates().addAll(0, myRecords);
                } else {
                    this.setmPrzeplywHistoricStates(myRecords);
                }
                mView.loadDataToPrzeplywChart();
            }
        }, new RetroFitErrorHelper(mView));
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
        if(!BuildConfig.DEBUG) {
            Answers.getInstance().logCustom(new CustomEvent("Favourites added")
                    .putCustomAttribute("Station", mStacja.getName()));
        }
        Log.e("ADD ULUBIONE", status ? "Succes" : "Fail");
    }

    public void deleteFromFavourite() {
        mStacja.setIsFav(false);
        boolean status = repoStacja.createOrUpdate(mStacja);
        Log.e("DELETE ULUBIONE", status ? "Succes" : "Fail");
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
