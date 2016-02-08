package fantomit.zwalkowepegle;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.Comparator;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.adapters.StationListAdapter;
import fantomit.zwalkowepegle.controllers.RiverStationsController;
import fantomit.zwalkowepegle.events.StationsLoadedEvent;
import fantomit.zwalkowepegle.interfaces.RiverStationsInterface;

public class RiverStations extends AppCompatActivity implements RiverStationsInterface {
    @Inject
    RiverStationsController mController;
    private StationListAdapter mAdapter;
    @Inject
    EventBus eventBus;

    @Bind(R.id.lvRivers)
    ListView mStations;
    @Bind(R.id.linlaHeaderProgress)
    LinearLayout mProgressLayout;

    private void setupActivity() {
        ZwalkiApplication.getApp().component.inject(this);
        setContentView(R.layout.simple_list);
        ButterKnife.bind(this);
        mController.setView(this);
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
        findViewById(R.id.header).setVisibility(View.GONE);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.primary)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity();
        if (getIntent().getExtras().containsKey(Statics._RIVER_ID) && savedInstanceState == null) {
            mController.setRiverId(getIntent().getExtras().getInt(Statics._RIVER_ID));
            mController.loadStations();
            getSupportActionBar().setTitle(mController.getRiverName());
        } else if (savedInstanceState != null) {
            refreshList();
        }
        mStations.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                    Intent i = new Intent(RiverStations.this, StationDetails.class);
                    i.putExtra(Statics._STATION_ID, mController.getStations().get(position).getId());
                    startActivity(i);
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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

    @Subscribe
    public void poZaladowaniuStacji(StationsLoadedEvent event) {
        refreshList();
    }

    public void refreshList() {
        if (mController.getStations() != null && mController.getStations().size() > 0) {
            Comparator<Station> RIVER_KM_ORDER = (Station object1, Station object2) ->
                    Double.compare(object2.getStatus().getRiverCourseKm(), object1.getStatus().getRiverCourseKm());
            Collections.sort(mController.getStations(), RIVER_KM_ORDER);
            mController.isSorted = true;

            if (mAdapter == null) {
                mAdapter = new StationListAdapter(this, mController.getStations());
                mStations.setAdapter(mAdapter);
            } else {
                mAdapter.stations = mController.getStations();
            }
            mAdapter.notifyDataSetChanged();
            hideProgressSpinner();
        }
    }

    @Override
    protected void onDestroy() {
        mController.setView(null);
        super.onDestroy();
    }

}
