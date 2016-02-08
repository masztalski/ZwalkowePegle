package fantomit.zwalkowepegle;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.rey.material.widget.Switch;

import java.io.File;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import fantomit.zwalkowepegle.controllers.SettingsController;
import fantomit.zwalkowepegle.dialogs.AboutDialog;
import fantomit.zwalkowepegle.dialogs.FileDialog;
import fantomit.zwalkowepegle.interfaces.SettingsInterface;
import fantomit.zwalkowepegle.receivers.FavsDownloadReceiver;

public class Settings extends AppCompatActivity implements SettingsInterface {
    @Bind(R.id.wojewodztwo_choose)
    Spinner spinWojewodztwa;
    @Bind(R.id.notification_switch)
    Switch notification_switch;
    @Bind(R.id.time_choose)
    Spinner spinTime;
    @Bind(R.id.clearFav)
    Button clearFavs;
    @Bind(R.id.apply)
    Button apply;
    @Bind(R.id.notif_hint)
    TextView notif_hint;
    @Bind(R.id.addData)
    Button addData;
    @Bind(R.id.defStates_hint)
    TextView defStates_hint;
    @Bind(R.id.defStates_switch)
    Switch defStates_switch;
    @Bind(R.id.dolnoslaskie)
    Button dolnoslaskie;

    @Inject
    SettingsController mController;

    private void setupActivity() {
        ZwalkiApplication.getApp().component.inject(this);
        setContentView(R.layout.settings);
        mController.setView(this);
        ButterKnife.bind(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.primary)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ustawienia");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.provinices_values, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinWojewodztwa.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.interwal, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinTime.setAdapter(adapter2);

        setValuesFromDb();
        if (!mController.wojewodztwo.equals("dolnoœl¹skie")) {
            dolnoslaskie.setVisibility(View.GONE);
        }

        setupListeners();
    }

    private void setupListeners() {
        spinWojewodztwa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mController.wojewodztwo = (String) parent.getAdapter().getItem(position);
                mController.wojPos = position;

                if (!BuildConfig.DEBUG) {
                    Answers.getInstance().logCustom(new CustomEvent("Wczytanie województwa")
                            .putCustomAttribute("name", mController.wojewodztwo));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: //1 godzina
                        mController.timeOfDownloading = 60;
                        break;
                    case 1: //2 godziny
                        mController.timeOfDownloading = 2 * 60;
                        break;
                    case 2: //3 godziny
                        mController.timeOfDownloading = 3 * 60;
                        break;
                    case 3: //1 dzieñ
                        mController.timeOfDownloading = 24 * 60;
                        break;
                    default:
                        mController.timeOfDownloading = 60;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        notification_switch.setOnCheckedChangeListener((Switch aSwitch, boolean notificationsEnabled) -> {
                    spinTime.setEnabled(notificationsEnabled);
                    mController.notificationEnabled = notificationsEnabled;
                    notif_hint.setText(notificationsEnabled ? "W³¹czone powiadomienia o ulubionych" : "Wy³¹czone powiadomienia o ulubionych");
                }
        );

        addData.setOnClickListener((View v) -> {
                    File path = new File(Environment.getExternalStorageDirectory() + "//DIR//");
                    FileDialog fileDialog = new FileDialog(Settings.this, path);
                    fileDialog.setFileEndsWith(".peg");
                    fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                        public void fileSelected(File file) {
                            Log.i(getClass().getSimpleName(), "selected file " + file.toString());
                            Toast.makeText(Settings.this, "Wczytano plik ze stanami charakterystycznymi. ZatwierdŸ zmiany aby za³adowaæ dane do stacji", Toast.LENGTH_SHORT).show();
                            mController.readFromFile(file);
                        }
                    });
                    fileDialog.showDialog();
                }
        );

        clearFavs.setOnClickListener((View v) -> {
                    mController.deleteFavs();
                    Toast.makeText(Settings.this, "Usuniêto ulubione", Toast.LENGTH_SHORT).show();
                }
        );

        apply.setOnClickListener((View v) -> {
                    if (mController.timeOfDownloading != 60) {
                        Intent i = new Intent();
                        i.setAction(FavsDownloadReceiver._ACTION);
                        sendBroadcast(i);
                    }
                    mController.saveSettings();
                    Toast.makeText(Settings.this, "Zapisano zmiany", Toast.LENGTH_SHORT).show();
                    finish();
                }
        );

        defStates_switch.setOnCheckedChangeListener((Switch aSwitch, boolean stanyZPogodynkiEnabled) -> {
            defStates_hint.setText(stanyZPogodynkiEnabled ? "W³¹czone stany charakterystyczne z Pogodynki" : "Wy³¹czone stany charakterystyczne z Pogodynki");
            mController.stanyPogodynkaEnabled = stanyZPogodynkiEnabled;
        });

        dolnoslaskie.setOnClickListener((View v) -> {
            mController.getStany();
            Toast.makeText(Settings.this, "Wczytano plik ze stanami charakterystycznymi dla woj. dolnoœl¹skiego", Toast.LENGTH_SHORT).show();
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
        mController.wojewodztwo = (String) spinWojewodztwa.getSelectedItem();
        notification_switch.setChecked(set.isNotificationEnabled());
        defStates_switch.setChecked(set.isStanyPogodynkaEnabled());
        if (set.isStanyPogodynkaEnabled()) {
            defStates_hint.setText("W³¹czone stany charakterystyczne z Pogodynki");
        } else {
            defStates_hint.setText("Wy³¹czone stany charakterystyczne z Pogodynki");
        }
        if (set.isNotificationEnabled()) {
            notif_hint.setText("W³¹czone powiadomienia o ulubionych");
        } else {
            notif_hint.setText("Wy³¹czone powiadomienia o ulubionych");
        }

        switch (set.getTime()) {
            case 60: //1 godzina
                spinTime.setSelection(0);
                break;
            case 2 * 60: //2 godziny
                spinTime.setSelection(1);
                break;
            case 3 * 60: //3 godziny
                spinTime.setSelection(2);
                break;
            case 24 * 60: //1 dzieñ
                spinTime.setSelection(3);
                break;
            default:
                spinTime.setSelection(0);
        }
    }

    @Override
    public void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
