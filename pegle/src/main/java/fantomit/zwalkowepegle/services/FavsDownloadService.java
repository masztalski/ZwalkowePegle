package fantomit.zwalkowepegle.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.annimon.stream.Stream;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import fantomit.zwalkowepegle.APImodels.PrzeplywRecord;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.receivers.FavsDownloadReceiver;
import fantomit.zwalkowepegle.receivers.NotificationReceiver;
import fantomit.zwalkowepegle.utils.RetroFitErrorHelper;
import fantomit.zwalkowepegle.webservices.StacjaWebService;
import roboguice.service.RoboService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class FavsDownloadService extends RoboService {

    @Inject
    StationRepository repoStacja;
    @Inject
    AlarmManager aMgr;
    @Inject
    StacjaWebService stacjaWS;

    @Inject
    private SettingsRepository repoSettings;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Intent i = new Intent(FavsDownloadReceiver._ACTION);

        Calendar c = Calendar.getInstance();
        int time = repoSettings.getSettings().getTime();
        //int time = 2; //wartoœæ do testów

        c.add(Calendar.MINUTE, time);
        Log.e("SERWIS-CZAS ", Integer.toString(time));
        Log.e("NOTIFICATIONS", repoSettings.getSettings().isNotificationEnabled() ? "ENABLED" : "DISABLED");

        Log.e("Fantom", "serwis uruchomiony");

        aMgr.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), PendingIntent.getBroadcast(this, 999999, i, PendingIntent.FLAG_UPDATE_CURRENT));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                FavsDownloadService.this.stopSelf();
            }
        }, 1000 * 60);

        boolean isNotificationEnabled = repoSettings.getSettings().isNotificationEnabled();
        if (isNotificationEnabled) {
            List<Station> stations = repoStacja.getAll();
            List<String> favIds = new ArrayList<>();
            Stream.of(stations)
                    .filter(station -> station.isFav())
                    .forEach(station -> favIds.add(station.getId()));

            for (String id : favIds) {
                Observable<Station> result = stacjaWS.getStacja(id);

                result.observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Station>() {
                    @Override
                    public void call(Station station) {
                        //jak spe³nione warunki to ustaw powiadomienie
                        Log.e("Fantom", "Pobrano stacje ulubion¹");
                        if (repoStacja.findById(station.getId()) != null) {
                            Station s = repoStacja.findById(station.getId());
                            station.setIsFav(s.isFav());
                            station.setNotifByPrzeplyw(s.isNotifByPrzeplyw());
                            station.setIsByDefaultCustomized(s.isByDefaultCustomized());
                            station.setDolnaGranicaPoziomu(s.getDolnaGranicaPoziomu());
                            station.setDolnaGranicaPrzeplywu(s.getDolnaGranicaPrzeplywu());
                            station.setIsUserCustomized(s.isUserCustomized());
                        }
                        List<PrzeplywRecord> przeplywRecords = station.getDischargeRecords();

                            if (station.isNotifByPrzeplyw() && przeplywRecords.get(przeplywRecords.size() - 1).getValue() > station.getDolnaGranicaPrzeplywu()) {
                                sendNotification(station.getId(), station.getName(), "przep³yw > " + Double.toString(station.getDolnaGranicaPrzeplywu()), przeplywRecords.get(przeplywRecords.size()-1).getValue(), -1);
                            } else if (!station.isNotifByPrzeplyw() && station.getStatus().getCurrentValue() > station.getDolnaGranicaPoziomu()) {
                                sendNotification(station.getId(), station.getName(), "poziom > " + Integer.toString(station.getDolnaGranicaPoziomu()), -1.0, station.getStatus().getCurrentValue());
                            }
//                        } else if (!station.isUserCustomized() && !station.isByDefaultCustomized()) {
//                            if (station.isNotifByPrzeplyw() && przeplywRecords.get(przeplywRecords.size() - 1).getValue() > station.getDolnaGranicaPrzeplywu()) {
//                                sendNotification(station.getId(), station.getName(), "przep³yw from API-LowVal", przeplywRecords.get(przeplywRecords.size()-1).getValue(), -1);
//                            } else if (station.getStatus().getCurrentValue() > station.getDolnaGranicaPoziomu()) {
//                                sendNotification(station.getId(), station.getName(), "poziom from API-LowVal", -1.0, station.getStatus().getCurrentValue());
//                            }
//                        }
                    }
                }, new RetroFitErrorHelper(null));
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendNotification(String id, String name, String typ, double dVal, int iVal) {
        Log.e("FANTOM", "wys³ano ¿¹danie o powiadomieniu " + typ);
        Intent i = new Intent();
        i.setAction(NotificationReceiver._KEY);
        i.putExtra(NotificationReceiver._ID, id);
        i.putExtra(NotificationReceiver._NAME, name);
        if(dVal != -1){
            i.putExtra(NotificationReceiver._dVAL, dVal);
        } else {
            i.putExtra(NotificationReceiver._iVAL, iVal);
        }
        sendBroadcast(i);
    }
}
