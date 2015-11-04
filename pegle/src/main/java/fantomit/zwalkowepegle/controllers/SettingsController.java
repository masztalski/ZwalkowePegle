package fantomit.zwalkowepegle.controllers;

import android.util.Log;

import com.annimon.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.DBmodels.Settings;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;

@Singleton
public class SettingsController {

    @Inject
    private StationRepository repoStacje;
    @Inject
    private SettingsRepository repoSettings;

    public boolean notificationEnabled = true;
    public int timeOfDownloading = 60;
    public String wojewodztwo = "dolnoœl¹skie";
    public int wojPos = 0;
    public boolean stanyPogodynkaEnabled = false;
    private static int id = 1;

    private Settings settings;

    public SettingsController() {
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
        settings.setWojewodztwo(wojewodztwo);
        settings.setWojPos(wojPos);
        repoSettings.createOrUpdate(settings);
    }

    public Settings getSettings() {
        this.settings = repoSettings.getSettings();
        return repoSettings.getSettings();
    }

    public Station getStation(String id) {
        return repoStacje.findById(id);
    }

    public void readFromFile(File file) {
        StringBuilder json = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                json.append(line);
                json.append('\n');
            }
            br.close();

            JsonElement obj = new JsonParser().parse(json.toString());
            JsonArray jArr = obj.getAsJsonArray();

            Stream.of(jArr)
                    .forEach(jObj -> {
                        try {
                            Station station_js = new Gson().fromJson(jObj, Station.class);
                            Station stacjaDB = repoStacje.findById(station_js.getId());
                            if (stacjaDB != null) {
                                stacjaDB.setLlw_poziom(station_js.getLlw_poziom());
                                //stacjaDB.setLlw_przeplyw(station_js.getLlw_przeplyw());
                                stacjaDB.setLw_poziom(station_js.getLw_poziom());
                                stacjaDB.setLw_przeplyw(station_js.getLw_przeplyw());
                                //stacjaDB.setMw1_poziom(station_js.getMw1_poziom());
                                //stacjaDB.setMw1_przeplyw(station_js.getMw1_przeplyw());
                                stacjaDB.setMw2_poziom(station_js.getMw2_poziom());
                                stacjaDB.setMw2_przeplyw(station_js.getMw2_przeplyw());
                                stacjaDB.setHw_poziom(station_js.getHw_poziom());
                                stacjaDB.setHw_przeplyw(station_js.getHw_przeplyw());
                                stacjaDB.setDolnaGranicaPoziomu(station_js.getLw_poziom());
                                stacjaDB.setDolnaGranicaPrzeplywu(station_js.getLw_przeplyw());
                                stacjaDB.setIsUserCustomized(true);
                                Log.e("UPDATE", repoStacje.createOrUpdate(stacjaDB) ? "Succes" : "fail");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
