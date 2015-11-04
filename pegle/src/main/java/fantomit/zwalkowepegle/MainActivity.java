package fantomit.zwalkowepegle;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.internal.widget.ActionBarOverlayLayout;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import de.greenrobot.event.EventBus;
import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.adapters.RiverListAdapter;
import fantomit.zwalkowepegle.controllers.MainController;
import fantomit.zwalkowepegle.dialogs.ConfirmDeleteDialog;
import fantomit.zwalkowepegle.dialogs.ConfirmDownloadDialog;
import fantomit.zwalkowepegle.dialogs.ConfirmExitDialog;
import fantomit.zwalkowepegle.interfaces.MainActivityInterface;
import fantomit.zwalkowepegle.receivers.FavsDownloadReceiver;
import fantomit.zwalkowepegle.receivers.UpdateReceiver;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class MainActivity extends RoboActionBarActivity implements MainActivityInterface {
    @Inject
    private MainController mController;
    @Inject
    private EventBus eventBus;
    @InjectView(R.id.lvRivers)
    private ListView lvRivers;
    @InjectView(R.id.linlaHeaderProgress)
    private LinearLayout mProgressLayout;
    private int orientation;
    private long loadingTime = 0;

    private RiverListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("STATE", "onCreate");
        orientation = getRequestedOrientation();
        setContentView(R.layout.simple_list);
        if (eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
        findViewById(R.id.header).setVisibility(View.VISIBLE);
        mController.setView(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        loadingTime = Calendar.getInstance().getTimeInMillis();
        mController.getListaStacji();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.primary)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setSubtitle("woj. " + mController.getWojewodztwoFromSettings());

        lvRivers.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                    Intent i = new Intent(MainActivity.this, RiverStations.class);
                    i.putExtra("RIVER", mController.getRivers().get(position).getId());
                    startActivity(i);
                }
        );

        lvRivers.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);

        lvRivers.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            private int nr = 0;

            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    nr++;
                    mAdapter.setNewSelection(position, checked);
                } else {
                    nr--;
                    mAdapter.removeSelection(position);
                }
                mode.setTitle(nr + " wybrane");
            }

            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                nr = 0;
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_selection, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
                        FragmentManager fm = getSupportFragmentManager();
                        Bundle extras = new Bundle();
                        extras.putIntegerArrayList("riverPos", mAdapter.getSelection());
                        dialog.setArguments(extras);
                        dialog.show(fm, "Potwierdzenie");
                        nr = 0;
                        mAdapter.clearSelection();
                        mode.finish();
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {
                nr = 0;
                mAdapter.clearSelection();
            }
        });

        lvRivers.setOnItemLongClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                    lvRivers.setItemChecked(position, !mAdapter.isPositionChecked(position));
                    return true;
                }
        );
    }

    @Override
    protected void onResume() {
        Log.e("STATE", "onResume");
        if (mController.hasWojewodztwoChanged()) {
            lvRivers.setEnabled(false);
            if (mAdapter != null) {
                mAdapter.rivers = new ArrayList<>();
                mAdapter.notifyDataSetChanged();
            }
            mController.getListaStacji();
            getSupportActionBar().setSubtitle("woj. " + mController.getWojewodztwoFromSettings());
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, Settings.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showProgressSpinner() {
        mProgressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressSpinner() {
        mProgressLayout.setVisibility(View.GONE);
    }

    @Override
    public Date getToday() {
        Calendar c = Calendar.getInstance();
        return c.getTime();
    }

    @Override
    public void displayRivers() {
        lvRivers.setEnabled(true);
        setRequestedOrientation(orientation);

        if (!mController.isSorted) {
            Comparator<River> ALPHABETICAL_ORDER1 = (River object1, River object2) -> {
                int res = String.CASE_INSENSITIVE_ORDER.compare(object1.getRiverName(), object2.getRiverName());
                return res;
            };
            Collections.sort(mController.getRivers(), ALPHABETICAL_ORDER1);
            mController.isSorted = true;
        }

        if (mAdapter == null) {
            mAdapter = new RiverListAdapter(this, mController.getRivers(), mController.plywalnoscRzek);
            loadingTime = Calendar.getInstance().getTimeInMillis() - loadingTime;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(loadingTime);

            if (!BuildConfig.DEBUG) {
                if (mController.hasWojewodztwoChanged()) {
                    Answers.getInstance().logCustom(new CustomEvent("Loading Time[download all]")
                            .putCustomAttribute("time", loadingTime));
                } else {
                    Answers.getInstance().logCustom(new CustomEvent("Loading Time[only display]")
                            .putCustomAttribute("time", loadingTime));
                }
            }
            lvRivers.setAdapter(mAdapter);
        } else {
            mAdapter.rivers = mController.getRivers();
            mAdapter.plywalnosc = mController.plywalnoscRzek;
        }
        mAdapter.notifyDataSetChanged();
        hideProgressSpinner();

        Intent i = new Intent();
        i.setAction(FavsDownloadReceiver._ACTION);
        sendBroadcast(i);

        runAktualizacjaService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mController.setView(null);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        ConfirmExitDialog dialog = new ConfirmExitDialog();
        dialog.show(fm, "confirm Exit");
    }

    @Override
    public void displayAktualizacjaDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ConfirmDownloadDialog dialog = new ConfirmDownloadDialog();
        dialog.show(fm, "confirm Download");
    }

    @Override
    public void runAktualizacjaService() {
        int curVersionCode = -1;
        String curVersion = "";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
            curVersionCode = packageInfo.versionCode;
            curVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.e("VERSION", curVersion + " " + Integer.toString(curVersionCode));
        //Start Update Service
//        Intent i = new Intent();
//        i.putExtra("verName", curVersion);
//        i.putExtra("verCode", curVersionCode);
//        i.setAction(UpdateReceiver._UPDATE);
//        sendBroadcast(i);
        //=====
    }

    @Override
    public void displayProgress(int stringResId, String messageAlt) {
        ((TextView) mProgressLayout.findViewById(R.id.progress_Text)).setText("Trwa ³adowanie danych: " + (stringResId != 0 ? this.getString(stringResId) : messageAlt));
    }
}
