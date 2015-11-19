package fantomit.zwalkowepegle.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.google.inject.Inject;

import java.util.Calendar;
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
        if (repoSettings.getSettings() == null) {
            Toast.makeText(this, "B³¹d bazy danych. Odinstaluj i zainstaluj aplikacjê ponownie", Toast.LENGTH_SHORT).show();
            return -1;
        }
        boolean isNotificationEnabled = repoSettings.getSettings().isNotificationEnabled();
        int time = repoSettings.getSettings().getTime();

        c.add(Calendar.MINUTE, time);
        Log.e("SERWIS-CZAS ", Integer.toString(time));
        Log.e("NOTIFICATIONS", isNotificationEnabled ? "ENABLED" : "DISABLED");

        Log.e("Fantom", "serwis uruchomiony");

        aMgr.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), PendingIntent.getBroadcast(this, 999999, i, PendingIntent.FLAG_UPDATE_CURRENT));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                FavsDownloadService.this.stopSelf();
            }
        }, 1000 * 60);

        if (isNotificationEnabled) {
            List<Station> stations = repoStacja.getAll();
            Stream.of(stations)
                    .filter(station -> station.isFav())
                    .map(station -> station.getId())
                    .forEach(id -> {
                        Observable<Station> result = stacjaWS.getStacja(id);
                        result.observeOn(AndroidSchedulers.mainThread()).subscribe(station -> {
                            //jak spe³nione warunki to ustaw powiadomienie
                            Log.e("Fantom", "Pobrano stacje ulubion¹");
                            Station s = repoStacja.findById(station.getId());
                            if (s != null) {
                                station.setIsFav(s.isFav());
                                station.setNotifByPrzeplyw(s.isNotifByPrzeplyw());
                                station.setIsByDefaultCustomized(s.isByDefaultCustomized());
                                station.setDolnaGranicaPoziomu(s.getDolnaGranicaPoziomu());
                                station.setDolnaGranicaPrzeplywu(s.getDolnaGranicaPrzeplywu());
                                station.setIsUserCustomized(s.isUserCustomized());
                            }
                            List<PrzeplywRecord> przeplywRecords = station.getDischargeRecords();

                            if (s != null && station.isNotifByPrzeplyw() && s.getLastPrzeplywTriger() != przeplywRecords.get(przeplywRecords.size() - 1).getValue()
                                    && przeplywRecords.get(przeplywRecords.size() - 1).getValue() >= station.getDolnaGranicaPrzeplywu()) {
                                s.setLastPrzeplywTriger(przeplywRecords.get(przeplywRecords.size() - 1).getValue());
                                sendNotification(station.getId(), station.getName(), "przep³yw > " + Double.toString(station.getDolnaGranicaPrzeplywu()), przeplywRecords.get(przeplywRecords.size() - 1).getValue(), -1);
                            } else if (s != null && !station.isNotifByPrzeplyw() && s.getLastPoziomTriger() != station.getStatus().getCurrentValue()
                                    && station.getStatus().getCurrentValue() >= station.getDolnaGranicaPoziomu()) {
                                s.setLastPoziomTriger(station.getStatus().getCurrentValue());
                                sendNotification(station.getId(), station.getName(), "poziom > " + Integer.toString(station.getDolnaGranicaPoziomu()), -1.0, station.getStatus().getCurrentValue());
                            }
                        }, new RetroFitErrorHelper(null));
                    });
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendNotification(String id, String name, String typ, double dVal, int iVal) {
        Log.e("FANTOM", "wys³ano ¿¹danie o powiadomieniu " + typ);
        Intent i = new Intent();
        i.setAction(NotificationReceiver._KEY);
        i.putExtra(NotificationReceiver._ID, id);
        i.putExtra(NotificationReceiver._NAME, name);
        i.putExtra(NotificationReceiver._TYPE, typ);
        if (dVal != -1) {
            i.putExtra(NotificationReceiver._dVAL, dVal);
        } else {
            i.putExtra(NotificationReceiver._iVAL, iVal);
        }
        sendBroadcast(i);
    }
}
