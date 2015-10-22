package fantomit.zwalkowepegle;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.inject.Inject;

import java.util.Collections;
import java.util.Comparator;

import de.greenrobot.event.EventBus;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.adapters.StationListAdapter;
import fantomit.zwalkowepegle.controllers.RiverStationsController;
import fantomit.zwalkowepegle.interfaces.RiverStationsInterface;
import fantomit.zwalkowepegle.utils.StationsLoadedEvent;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class RiverStations extends RoboActionBarActivity implements RiverStationsInterface {

    @Inject
    private RiverStationsController mController;
    private StationListAdapter mAdapter;
    @Inject
    private EventBus eventBus;

    @InjectView(R.id.lvRivers)
    ListView mStations;
    @InjectView(R.id.linlaHeaderProgress)
    private LinearLayout mProgressLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_list);
        mController.setView(this);
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
        findViewById(R.id.header).setVisibility(View.GONE);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.primary)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent().getExtras().containsKey("RIVER") && savedInstanceState == null) {
            mController.loadStations(getIntent().getExtras().getInt("RIVER"));
            getSupportActionBar().setTitle(mController.getRiverName());
        } else if (savedInstanceState != null) {
            refreshList();
        }
        mStations.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                Intent i = new Intent(RiverStations.this, StationDetails.class);
                i.putExtra("STATION_ID", mController.getStations().get(position).getId());
                startActivity(i);
            }
        );

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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

    public void onEvent(StationsLoadedEvent event) {
        refreshList();
    }

    public void refreshList() {
        if(mController.getStations() != null && mController.getStations().size() > 0) {
            Comparator<Station> RIVER_KM_ORDER = (Station object1, Station object2) -> {
                    int res = Double.compare(object2.getStatus().getRiverCourseKm(), object1.getStatus().getRiverCourseKm());
                    return res;
                }
            ;
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
