package fantomit.zwalkowepegle.controllers;

import android.os.Environment;
import android.util.Log;

import com.annimon.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fantomit.zwalkowepegle.APImodels.GCM;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.DBmodels.Settings;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.events.DataLoadedEvent;
import fantomit.zwalkowepegle.interfaces.SettingsInterface;
import fantomit.zwalkowepegle.webservices.WrotkaWebService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class SettingsController {

    private StationRepository repoStacje;
    private SettingsRepository repoSettings;

    public boolean notificationEnabled = true;
    public int timeOfDownloading = 60;
    public String wojewodztwo = "dolnoœl¹skie";
    public int wojPos = 0;
    public boolean stanyPogodynkaEnabled = false;
    private boolean extraDataLoaded = false;

    private Settings settings;

    private WrotkaWebService wrotkaWS;
    private SettingsInterface mView;

    @Inject
    public SettingsController(StationRepository repoStacje, SettingsRepository repoSettings, WrotkaWebService wrotkaWS) {
        this.repoStacje = repoStacje;
        this.repoSettings = repoSettings;
        this.wrotkaWS = wrotkaWS;
    }

    public void deleteFavs() {
        List<Station> stacje = repoStacje.getAll();
        Stream.of(stacje)
                .forEach(s -> {
                    s.setIsFav(false);
                    repoStacje.createOrUpdate(s);
                });
    }

    public void saveSettings() {
        settings.setNotificationEnabled(notificationEnabled);
        settings.setTime(timeOfDownloading);
        settings.setStanyPogodynkaEnabled(stanyPogodynkaEnabled);
        if (wojewodztwo.equals(repoSettings.getSettings().getWojewodztwo())) {
            settings.setHasWojewodztwoChanged(false);
        } else {
            settings.setHasWojewodztwoChanged(true);
        }
        if (extraDataLoaded) {
            EventBus.getDefault().post(new DataLoadedEvent());
            extraDataLoaded = false;
        }
        settings.setWojewodztwo(wojewodztwo);
        settings.setWojPos(wojPos);
        repoSettings.createOrUpdate(settings);
    }

    public Settings getSettings() {
        this.settings = repoSettings.getSettings();
        return repoSettings.getSettings();
    }

    public void getStany() {
        wrotkaWS.getStany().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String fileName = "stany.peg";
                try {
                    File path = Environment.getExternalStorageDirectory();
                    File file = new File(path, fileName);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    IOUtils.write(response.body().bytes(), fileOutputStream);
                    if (file.exists()) readFromFile(file);
                } catch (IOException e) {
                    Log.e("Stany.peg", "Error while writing file!");
                    Log.e("Stany.peg", e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                if (t.getMessage() != null) Log.e("Retrofit", t.getMessage());
                else Log.e("Retrofit", "B³¹d przy po³¹czeniu do wrotki");
                if (mView != null) {
                    if (t.getMessage() != null) mView.displayToast(t.getMessage());
                    else mView.displayToast("B³¹d po³¹czenia");
                }
            }
        });

    }

    public void processReadedData(String json) {
        JsonElement obj = new JsonParser().parse(json.toString());
        JsonArray jArr = obj.getAsJsonArray();
        extraDataLoaded = true;

        Stream.of(jArr)
                .forEach(jObj -> {
                    try {
                        Station station_js = new Gson().fromJson(jObj, Station.class);
                        Station stacjaDB = repoStacje.findById(station_js.getId());
                        if (stacjaDB != null) {
                            stacjaDB.setLlw_poziom(station_js.getLlw_poziom());
                            stacjaDB.setLw_poziom(station_js.getLw_poziom());
                            stacjaDB.setLw_przeplyw(station_js.getLw_przeplyw());
                            stacjaDB.setMw2_poziom(station_js.getMw2_poziom());
                            stacjaDB.setMw2_przeplyw(station_js.getMw2_przeplyw());
                            stacjaDB.setHw_poziom(station_js.getHw_poziom());
                            stacjaDB.setHw_przeplyw(station_js.getHw_przeplyw());
                            stacjaDB.setDolnaGranicaPoziomu(station_js.getLw_poziom());
                            stacjaDB.setDolnaGranicaPrzeplywu(station_js.getLw_przeplyw());
                            stacjaDB.setIsUserCustomized(true);
                            Log.i(getClass().getSimpleName(), "Update : " + (repoStacje.createOrUpdate(stacjaDB) ? "Succes" : "fail"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void readFromFile(File file) {
        if (!file.exists()) {
            return;
        }
        StringBuilder json = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                json.append(line);
                json.append('\n');
            }
            br.close();
            processReadedData(json.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void setView(SettingsInterface view) {
        this.mView = view;
    }

}
