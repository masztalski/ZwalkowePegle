package fantomit.zwalkowepegle.controllers;

import android.util.Log;

import com.buganalytics.trace.BugAnalytics;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import fantomit.zwalkowepegle.APImodels.PrzeplywRecord;
import fantomit.zwalkowepegle.APImodels.StateRecord;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.R;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.interfaces.StationDetailsInterface;
import fantomit.zwalkowepegle.utils.RetroFitErrorHelper;
import fantomit.zwalkowepegle.utils.StationDownloadedEvent;
import fantomit.zwalkowepegle.webservices.StacjaWebService;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

@Singleton
public class StationController {

    private Station mStacja;
    private StationDetailsInterface mView;
    @Inject
    private StationRepository repoStacja;

    @Inject
    private EventBus eventBus;

    public void setView(StationDetailsInterface mView){
        this.mView = mView;
    }

    public void loadStacja(String id){
        mView.showProgressSpinner();
        mStacja = repoStacja.findById(id);
        mView.loadView();
        mView.hideProgressSpinner();
    }

    public Station getStacja(){
        return mStacja;
    }

    public boolean isStationFav() {
        return mStacja.isFav();
    }

    public void addToFavourite(){
        mStacja.setIsFav(true);
        boolean status = repoStacja.createOrUpdate(mStacja);
        BugAnalytics.sendEvent("Dodano do ulubionych");
        Log.e("ADD ULUBIONE", status ? "Succes" : "Fail");
    }

    public void deleteFromFavourite(){
        mStacja.setIsFav(false);
        boolean status = repoStacja.createOrUpdate(mStacja);
        Log.e("DELETE ULUBIONE", status ? "Succes" : "Fail");
    }
}
