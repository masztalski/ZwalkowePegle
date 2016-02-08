package fantomit.zwalkowepegle;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.adapters.RiverListAdapter;
import fantomit.zwalkowepegle.controllers.MainController;
import fantomit.zwalkowepegle.dialogs.AboutDialog;
import fantomit.zwalkowepegle.dialogs.ChooseWojewodztwoDialog;
import fantomit.zwalkowepegle.dialogs.ConfirmDeleteDialog;
import fantomit.zwalkowepegle.dialogs.ConfirmExitDialog;
import fantomit.zwalkowepegle.interfaces.MainActivityInterface;
import fantomit.zwalkowepegle.receivers.FavsDownloadReceiver;

public class MainActivity extends AppCompatActivity implements MainActivityInterface {

    @Inject
    MainController mController;
    @Inject
    EventBus eventBus;
    @Bind(R.id.lvRivers)
    ListView lvRivers;
    @Bind(R.id.linlaHeaderProgress)
    LinearLayout mProgressLayout;
    private int orientation;

    private RiverListAdapter mAdapter;

    private void setupActivity() {
        ZwalkiApplication.getApp().component.inject(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.primary)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        orientation = getRequestedOrientation();
        setContentView(R.layout.simple_list);
        ButterKnife.bind(this);
        if (eventBus.isRegistered(this)) {
            eventBus.register(this);
        }

        findViewById(R.id.header).setVisibility(View.VISIBLE);
        mController.setView(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity();
        AppRater.app_launched(this);
        if (mController.hasWojewodztwoChanged()) {
            ChooseWojewodztwoDialog dialog = new ChooseWojewodztwoDialog();
            FragmentManager fm = getSupportFragmentManager();
            dialog.show(fm, "Choose Wojwodztwo Dialog");
        } else {
            mController.getListaStacji();
        }

        lvRivers.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                    Intent i = new Intent(MainActivity.this, RiverStations.class);
                    i.putExtra(Statics._RIVER_ID, mController.getRivers().get(position).getId());
                    startActivity(i);
                }
        );

        lvRivers.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);

        lvRivers.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            private int nr = 0;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
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
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                nr = 0;
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_selection, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
                        FragmentManager fm = getSupportFragmentManager();
                        Bundle extras = new Bundle();
                        extras.putIntegerArrayList(Statics._RIVER_POS, mAdapter.getSelection());
                        dialog.setArguments(extras);
                        dialog.show(fm, "Potwierdzenie");
                        nr = 0;
                        mAdapter.clearSelection();
                        mode.finish();
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
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
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, Settings.class);
                startActivity(i);
                break;
            case R.id.action_about:
                FragmentManager fm = getSupportFragmentManager();
                AboutDialog dialog = new AboutDialog();
                dialog.show(fm, "About");
                break;
            case R.id.action_refresh:
                mController.checkPlywalnosc();
                Toast.makeText(this, "Rozpoczêto odœwie¿anie danych", Toast.LENGTH_SHORT).show();
                break;
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

    private void sortRivers() {
        Comparator<River> ALPHABETICAL_ORDER1 = (River object1, River object2) -> {
            int res = String.CASE_INSENSITIVE_ORDER.compare(object1.getRiverName(), object2.getRiverName());
            return res;
        };
        Collections.sort(mController.getRivers(), ALPHABETICAL_ORDER1);
    }

    @Override
    public void displayRivers() {
        lvRivers.setEnabled(true);
        setRequestedOrientation(orientation);
        getSupportActionBar().setSubtitle("woj. " + mController.getWojewodztwoFromSettings());

        sortRivers();

        if (mAdapter == null) {
            mAdapter = new RiverListAdapter(this, mController.getRivers(), mController.plywalnoscRzek);
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
        dialog.show(fm, ConfirmExitDialog.class.getSimpleName());
    }
}
