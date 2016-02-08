package fantomit.zwalkowepegle.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.annimon.stream.Stream;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import fantomit.zwalkowepegle.APImodels.PrzeplywRecord;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.ZwalkiApplication;
import fantomit.zwalkowepegle.db.repositories.RiverRepository;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.receivers.FavsDownloadReceiver;
import fantomit.zwalkowepegle.receivers.NotificationReceiver;
import fantomit.zwalkowepegle.webservices.PogodynkaWebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavsDownloadService extends Service {

    @Inject
    StationRepository repoStacja;
    @Inject
    AlarmManager aMgr;
    @Inject
    PogodynkaWebService stacjaWS;

    @Inject
    SettingsRepository repoSettings;
    @Inject
    RiverRepository repoRiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        ZwalkiApplication.getApp().component.inject(this);
        Intent i = new Intent(FavsDownloadReceiver._ACTION);

        Calendar c = Calendar.getInstance();
        if (repoSettings.getSettings() == null) {
            Toast.makeText(this, "B³¹d bazy danych. Odinstaluj i zainstaluj aplikacjê ponownie", Toast.LENGTH_SHORT).show();
            return -1;
        }
        boolean isNotificationEnabled = repoSettings.getSettings().isNotificationEnabled();
        int time = repoSettings.getSettings().getTime();

        c.add(Calendar.MINUTE, time);
        Log.i(getClass().getSimpleName(), "SERWIS-CZAS " + Integer.toString(time));
        Log.i(getClass().getSimpleName(), "NOTIFICATIONS " + (isNotificationEnabled ? "ENABLED" : "DISABLED"));
        Log.i(getClass().getSimpleName(), "serwis uruchomiony");

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
                        Call<Station> result = stacjaWS.getStacja(id);
                        result.enqueue(new Callback<Station>() {
                            @Override
                            public void onResponse(Call<Station> call, Response<Station> response) {
                                Station station = response.body();
                                //jak spe³nione warunki to ustaw powiadomienie
                                Log.i(getClass().getSimpleName(), "Pobrano stacje ulubion¹");
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
                            }

                            @Override
                            public void onFailure(Call<Station> call, Throwable throwable) {
                                if (throwable != null) {
                                    if (throwable.getMessage() != null)
                                        Log.e("Retrofit", throwable.getMessage());
                                }
                            }
                        });
                    });
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendNotification(String id, String name, String typ, double dVal, int iVal) {
        Log.i(getClass().getSimpleName(), "wys³ano ¿¹danie o powiadomieniu " + typ);
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
