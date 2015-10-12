package fantomit.zwalkowepegle;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.buganalytics.trace.BugAnalytics;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.rey.material.widget.Switch;

import java.io.File;

import fantomit.zwalkowepegle.controllers.SettingsController;
import fantomit.zwalkowepegle.dialogs.AboutDialog;
import fantomit.zwalkowepegle.dialogs.FileDialog;
import fantomit.zwalkowepegle.receivers.FavsDownloadReceiver;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class Settings extends RoboActionBarActivity {
    @InjectView(R.id.wojewodztwo_choose)
    private Spinner spinWojewodztwa;
    @InjectView(R.id.notification_switch)
    private Switch notification_switch;
    @InjectView(R.id.time_choose)
    private Spinner spinTime;
    @InjectView(R.id.clearFav)
    private Button clearFavs;
    @InjectView(R.id.apply)
    private Button apply;
    @InjectView(R.id.notif_hint)
    private TextView notif_hint;
    @InjectView(R.id.addData)
    private Button addData;

    @Inject
    private SettingsController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.primary)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ustawienia");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.provinices_values, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinWojewodztwa.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.interwal, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinTime.setAdapter(adapter2);

        setValuesFromDb();


        spinWojewodztwa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mController.wojewodztwo = (String) parent.getAdapter().getItem(position);
                mController.wojPos = position;
                BugAnalytics.sendEvent ("Zmiana województwa");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: //15 minut
                        mController.timeOfDownloading = 15;
                        break;
                    case 1: //30 minut
                        mController.timeOfDownloading = 30;
                        break;
                    case 2: //1 godzina
                        mController.timeOfDownloading = 60;
                        break;
                    case 3: //2 godziny
                        mController.timeOfDownloading = 2 * 60;
                        break;
                    case 4: //3 godziny
                        mController.timeOfDownloading = 3 * 60;
                        break;
                    default:
                        mController.timeOfDownloading = 30;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        notification_switch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch aSwitch, boolean b) {
                if (b) {
                    spinTime.setEnabled(true);
                    mController.notificationEnabled = true;
                    notif_hint.setText("W³¹czone powiadomienia o ulubionych");
                } else {
                    spinTime.setEnabled(false);
                    mController.notificationEnabled = false;
                    notif_hint.setText("Wy³¹czone powiadomienia o ulubionych");
                }
            }
        });

        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                File path = new File(Environment.getExternalStorageDirectory() + "//DIR//");
                FileDialog fileDialog = new FileDialog(Settings.this, path);
                fileDialog.setFileEndsWith(".peg");
                fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                    public void fileSelected(File file) {
                        Log.e(getClass().getName(), "selected file " + file.toString());
                        mController.readFromFile(file);
                    }
                });
                fileDialog.showDialog();
                BugAnalytics.sendEvent("Wczytanie pliku stany.peg");
                Toast.makeText(Settings.this, "Wczytano plik ze stanami charakterystycznymi. ZatwierdŸ zmiany aby za³adowaæ dane do stacji", Toast.LENGTH_SHORT).show();
            }
        });

        clearFavs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.deleteFavs();
                Toast.makeText(Settings.this, "Usuniêto ulubione", Toast.LENGTH_SHORT).show();
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mController.timeOfDownloading != 30) {
                    Intent i = new Intent();
                    i.setAction(FavsDownloadReceiver._ACTION);
                    sendBroadcast(i);
                }
                mController.saveSettings();
                Toast.makeText(Settings.this, "Zapisano zmiany", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_about:
                FragmentManager fm = getSupportFragmentManager();
                AboutDialog dialog = new AboutDialog();
                dialog.show(fm, "About");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setValuesFromDb() {
        fantomit.zwalkowepegle.DBmodels.Settings set = mController.getSettings();
        spinWojewodztwa.setSelection(set.getWojPos());
        notification_switch.setChecked(set.isNotificationEnabled());
        if (set.isNotificationEnabled()) {
            notif_hint.setText("W³¹czone powiadomienia o ulubionych");
        } else {
            notif_hint.setText("Wy³¹czone powiadomienia o ulubionych");
        }

        switch (set.getTime()) {
            case 15: //15 minut
                spinTime.setSelection(0);
                break;
            case 30: //30 minut
                spinTime.setSelection(1);
                break;
            case 60: //1 godzina
                spinTime.setSelection(2);
                break;
            case 2 * 60: //2 godziny
                spinTime.setSelection(3);
                break;
            case 3 * 60: //3 godziny
                spinTime.setSelection(4);
                break;
            default:
                spinTime.setSelection(1);
        }
    }


}
