package fantomit.zwalkowepegle;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.inject.Inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import fantomit.zwalkowepegle.APImodels.PrzeplywRecord;
import fantomit.zwalkowepegle.APImodels.StateRecord;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.controllers.StationController;
import fantomit.zwalkowepegle.dialogs.EditCustomStationDialog;
import fantomit.zwalkowepegle.interfaces.StationDetailsInterface;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class StationDetails extends RoboActionBarActivity implements StationDetailsInterface {

    @Inject
    StationController mController;

    @InjectView(R.id.currentLevel)
    TextView mCurrentLevel;
    @InjectView(R.id.currentPrzewplyw)
    TextView mCurrentPrzeplyw;
    @InjectView(R.id.warningLevel)
    TextView mWarningLevel;
    @InjectView(R.id.alarmLevel)
    TextView mAlarmLevel;
    @InjectView(R.id.trend)
    ImageView mTrend;
    @InjectView(R.id.chartLevel)
    LineChart mLevelChart;
    @InjectView(R.id.chartPrzeplyw)
    LineChart mPrzeplywChart;
    @InjectView(R.id.linlaHeaderProgress)
    private LinearLayout mProgressLayout;
    private MenuItem mFavButton;
    @InjectView(R.id.level_switches)
    private RadioGroup mLevelSwitches;
    @InjectView(R.id.przeplyw_switches)
    private RadioGroup mPrzeplywSwitches;

    private RadioGroup.OnCheckedChangeListener levelListener;
    private RadioGroup.OnCheckedChangeListener przeplywListner;

    @IntDef({LW, MW, HW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface WaterStates {
    }

    public static final int LW = 1;
    public static final int MW = 2;
    public static final int HW = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_details);
        mController.setView(this);
        createListners();

        if (getIntent().getExtras().containsKey("STATION_ID") && savedInstanceState == null) {
            mController.loadStacja(getIntent().getStringExtra("STATION_ID"));
        } else if (savedInstanceState != null) {
            loadView();
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.primary)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLevelSwitches.setOnCheckedChangeListener(levelListener);
        mPrzeplywSwitches.setOnCheckedChangeListener(przeplywListner);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_station, menu);
        mFavButton = menu.findItem(R.id.action_favourite);
        if (mController.isStationFav()) {
            mFavButton.setIcon(R.drawable.ic_star_white_36dp);
            Log.e("ULBIONE", "Stacja " + mController.getStacja().getName() + " ULUBIONA");
        } else {
            mFavButton.setIcon(R.drawable.ic_star_outline_white_36dp);
            Log.e("ULBIONE", "Stacja " + mController.getStacja().getName() + " NIE ULUBIONA");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_favourite:
                if (mController.isStationFav()) {
                    mController.deleteFromFavourite();
                    item.setIcon(R.drawable.ic_star_outline_white_36dp);
                    Toast.makeText(this, "Usuniêto z ulubionych", Toast.LENGTH_SHORT).show();
                } else {
                    mController.addToFavourite();
                    item.setIcon(R.drawable.ic_star_white_36dp);
                    Toast.makeText(this, "Dodano do ulubionych", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_edit:
                FragmentManager fm = getSupportFragmentManager();
                EditCustomStationDialog dialog = new EditCustomStationDialog();
                Bundle extras = new Bundle();
                extras.putString("ID", mController.getStacja().getId());
                dialog.setArguments(extras);
                dialog.show(fm, "Edit Station");
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

    @Override
    public void loadView() {
        if(mController == null || mController.getStacja() == null){
            Toast.makeText(this, "Wyst¹pi³ nieznany b³¹d. Powiadom Developera o nazwie stacji/rzeki gdzie wyst¹pi³ b³¹d", Toast.LENGTH_SHORT).show();
            return;
        }
        if(mController.getStacja().getHw_poziom() == -1) {
            mLevelSwitches.findViewById(R.id.hw_level).setVisibility(View.GONE);
        }
        if(mController.getStacja().getHw_przeplyw() == -1){
            mPrzeplywSwitches.findViewById(R.id.hw_przeplyw).setVisibility(View.GONE);
        }
        if(!mController.pogodynkaStatesEnabled()){
            mLevelSwitches.findViewById(R.id.lw_level).setVisibility(View.GONE);
            mLevelSwitches.findViewById(R.id.mw2_level).setVisibility(View.GONE);
            mLevelSwitches.findViewById(R.id.hw_level).setVisibility(View.GONE);
            mPrzeplywSwitches.findViewById(R.id.lw_przeplyw).setVisibility(View.GONE);
            mPrzeplywSwitches.findViewById(R.id.mw2_przeplyw).setVisibility(View.GONE);
            mPrzeplywSwitches.findViewById(R.id.hw_przeplyw).setVisibility(View.GONE);
        }
        Log.e("FANTOM", mController.getStacja().getName() + " - loadView");
        getSupportActionBar().setTitle(mController.getStacja().getName());
        String date = mController.getStacja().getStatus().getCurrentDate();
        if(date.contains("T") && date.contains("Z")) {
            String dzien = date.substring(0, date.indexOf('T'));
            String godzina = date.substring(date.indexOf('T') + 1, date.indexOf('Z'));
            getSupportActionBar().setSubtitle(dzien + '\n' + godzina + " GMT");
        }
        mCurrentLevel.setText(Integer.toString(mController.getStacja().getStatus().getCurrentValue()) + " cm");
        List<PrzeplywRecord> przeplywRecords = mController.getStacja().getDischargeRecords();
        String przeplyw = "";
        if(przeplywRecords != null && !przeplywRecords.isEmpty()) {
            przeplyw = Double.toString(przeplywRecords.get(przeplywRecords.size() - 1).getValue()) + " m<sup>3</sup>/s";
        }
        mCurrentPrzeplyw.setText(Html.fromHtml(przeplyw), TextView.BufferType.SPANNABLE);
        mWarningLevel.setText(Double.toString(mController.getStacja().getStatus().getWarningValue()) + " cm");
        mAlarmLevel.setText(Double.toString(mController.getStacja().getStatus().getAlarmValue()) + " cm");
        String trend = mController.getStacja().getTrend();
        if (trend.equals("const")) {
            mTrend.setImageResource(R.drawable.ic_trending_neutral_black_48dp);
        } else if (trend.equals("down")) {
            mTrend.setImageResource(R.drawable.ic_trending_down_black_48dp);
        } else if (trend.equals("up")) {
            mTrend.setImageResource(R.drawable.ic_trending_up_black_48dp);
        } else {
            mTrend.setImageResource(R.drawable.ic_help_black_36dp);
        }
        loadDataToLevelChart();
        loadDataToPrzeplywChart();

    }

    public void loadDataToLevelChart() {
        Station stacja = mController.getStacja();
        if (stacja == null) {
            return;
        }
        Log.e("FANTOM", stacja.getName() + " - loadLevelChart");
        List<StateRecord> stateRecords = stacja.getWaterStateRecords();
        List<Entry> yVals = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        if(stateRecords == null || stateRecords.isEmpty()){
            return;
        }
        for (StateRecord record : stateRecords) {
            String date = record.getDate();
            String dzien = date.substring(0, date.indexOf('T'));
            String godzina = date.substring(date.indexOf('T') + 1, date.indexOf('Z'));
            yVals.add(new Entry(new Float(record.getValue()), i));
            labels.add(dzien + ' ' + godzina);
            i++;
        }

        LineDataSet pobrane = new LineDataSet(yVals, "Wykres stanu wody za ostatnie 3 doby");
        pobrane.setDrawValues(false);
        pobrane.setCircleSize(0);
        pobrane.setLineWidth(1.5f);
        pobrane.setColor(getResources().getColor(R.color.level_chart));

        LineData data = new LineData(labels, pobrane);
        mLevelChart.setData(data);
        YAxis leftAxis = mLevelChart.getAxisLeft();
        mLevelChart.getAxisRight().setEnabled(false);
        leftAxis.setStartAtZero(false);
        mLevelChart.getAxisLeft().setAxisMaxValue(data.getYMax() + 5);
        mLevelChart.getAxisLeft().setAxisMinValue(data.getYMin() - 5);
        mLevelChart.setTouchEnabled(false);
        mLevelChart.setDescription("");
        mLevelChart.setAutoScaleMinMaxEnabled(true);
        mLevelChart.setDrawGridBackground(false);
        mLevelChart.notifyDataSetChanged();
        mLevelChart.invalidate();
    }

    public void loadDataToPrzeplywChart() {
        Station stacja = mController.getStacja();
        if (stacja == null) {
            return;
        }
        List<PrzeplywRecord> stateRecords = stacja.getDischargeRecords();
        List<Entry> yVals = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        if(stateRecords == null || stateRecords.isEmpty()){
            return;
        }
        for (PrzeplywRecord record : stateRecords) {
            String date = record.getDate();
            String dzien = date.substring(0, date.indexOf('T'));
            String godzina = date.substring(date.indexOf('T') + 1, date.indexOf('Z'));
            yVals.add(new Entry(new Float(record.getValue()), i));
            labels.add(dzien + ' ' + godzina);
            i++;
        }

        LineDataSet pobrane = new LineDataSet(yVals, "Przep³yw");
        pobrane.setDrawValues(false);
        pobrane.setCircleSize(0);
        pobrane.setLineWidth(1.5f);
        pobrane.setColor(getResources().getColor(R.color.przeplyw_chart));

        LineData data = new LineData(labels, pobrane);
        if (data != null && data.getDataSetCount() > 0) {
            mPrzeplywChart.setData(data);
        }
        YAxis leftAxis = mLevelChart.getAxisLeft();
        mPrzeplywChart.getAxisRight().setEnabled(false);
        leftAxis.setStartAtZero(false);
        mPrzeplywChart.getAxisLeft().setAxisMaxValue(data.getYMax()+3);
        mPrzeplywChart.getAxisLeft().setAxisMinValue(data.getYMin()-3);
        mPrzeplywChart.setTouchEnabled(false);
        mPrzeplywChart.setDescription("");
        mPrzeplywChart.setAutoScaleMinMaxEnabled(true);
        mPrzeplywChart.setDrawGridBackground(false);
        mPrzeplywChart.notifyDataSetChanged();
        mPrzeplywChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        mController.setView(null);
        super.onDestroy();
    }

    private void addCharakterystycznyLevel(@WaterStates int state) {
        List<Entry> yVals2 = new ArrayList<>();
        List<StateRecord> stateRecords = mController.getStacja().getWaterStateRecords();
        String label = "";
        if (mController.getStacja().isUserCustomized() || mController.getStacja().isByDefaultCustomized()) {
            float var;
            switch (state) {
                case LW:
                    var = new Float(mController.getStacja().getLw_poziom());
                    label = "LW";
                    break;
                case MW:
                    var = new Float(mController.getStacja().getMw2_poziom());
                    label = "MW";
                    break;
                case HW:
                    var = new Float(mController.getStacja().getHw_poziom());
                    label = "HW";
                    break;
                default:
                    return;
            }
            if (var == -1) return;
            for (int itr = 0; itr < stateRecords.size(); itr++) {
                yVals2.add(new Entry(var, itr));
            }

        } else {
            float var;
            switch (state) {
                case LW:
                    var = new Float(mController.getStacja().getStatus().getLowValue());
                    label = "LW";
                    break;
                case MW:
                    var = new Float(mController.getStacja().getStatus().getHighValue());
                    label = "MW";
                    break;
                default:
                    return;
            }
            if (var == -1) return;

            for (int itr = 0; itr < stateRecords.size(); itr++) {
                yVals2.add(new Entry(var, itr));
            }
        }
        LineDataSet charakterystyczne = new LineDataSet(yVals2, label);
        charakterystyczne.setDrawValues(false);
        charakterystyczne.setCircleSize(0);
        charakterystyczne.setLineWidth(1.5f);
        charakterystyczne.setColor(getResources().getColor(R.color.black));
        mLevelChart.getLineData().addDataSet(charakterystyczne);
        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getYMax() + 5);
        mLevelChart.getAxisLeft().setAxisMinValue(mLevelChart.getLineData().getYMin() - 5);
        mLevelChart.notifyDataSetChanged();
        mLevelChart.invalidate();
    }

    private void addCharakterystycznyPrzeplyw(@WaterStates int state) {
        List<Entry> yVals = new ArrayList<>();
        List<PrzeplywRecord> stateRecords = mController.getStacja().getDischargeRecords();
        String label = "";
        if (mController.getStacja().isUserCustomized() || mController.getStacja().isByDefaultCustomized()) {
            float var;
            switch (state) {
                case LW:
                    var = new Float(mController.getStacja().getLw_przeplyw());
                    label = "LW";
                    break;
                case MW:
                    var = new Float(mController.getStacja().getMw2_przeplyw());
                    label = "MW2";
                    break;
                case HW:
                    var = new Float(mController.getStacja().getHw_przeplyw());
                    label = "HW";
                    break;
                default:
                    return;
            }
            if (var == -1) return;
            for (int itr = 0; itr < stateRecords.size(); itr++) {
                yVals.add(new Entry(var, itr));
            }
        } else {
            float var;
            switch (state) {
                case LW:
                    var = new Float(mController.getStacja().getLowDischargeValue());
                    label = "LW";
                    break;
                case MW:
                    var = new Float(mController.getStacja().getHighDischargeValue());
                    label = "MW";
                    break;
                default:
                    return;
            }
            if (var == -1) return;
            for (int itr = 0; itr < stateRecords.size(); itr++) {
                yVals.add(new Entry(var, itr));
            }
        }
        LineDataSet charakterystyczne = new LineDataSet(yVals, label);
        charakterystyczne.setDrawValues(false);
        charakterystyczne.setCircleSize(0);
        charakterystyczne.setLineWidth(1.5f);
        charakterystyczne.setColor(getResources().getColor(R.color.black));

        mPrzeplywChart.getLineData().addDataSet(charakterystyczne);
        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getYMax() + 3);
        mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getYMin() - 3);
        mPrzeplywChart.notifyDataSetChanged();
        mPrzeplywChart.invalidate();
    }

    private void createListners() {
        levelListener = (RadioGroup group, int checkedId) -> {
                switch (checkedId) {
                    case R.id.nothing_level:
                        if (mLevelChart.getLineData().getDataSetCount() > 1) {
                            mLevelChart.getLineData().removeDataSet(1);
                            mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getYMax() + 5);
                            mLevelChart.getAxisLeft().setAxisMinValue(mLevelChart.getLineData().getYMin() - 5);
                            mLevelChart.notifyDataSetChanged();
                            mLevelChart.invalidate();
                        }
                        break;
                    case R.id.lw_level:
                        if (mLevelChart.getLineData().getDataSetCount() > 1) {
                            mLevelChart.getLineData().removeDataSet(1);
                            mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getYMax() + 5);
                            mLevelChart.getAxisLeft().setAxisMinValue(mLevelChart.getLineData().getYMin() - 5);
                            mLevelChart.notifyDataSetChanged();
                            mLevelChart.invalidate();
                        }
                        addCharakterystycznyLevel(LW);
                        break;
                    case R.id.mw2_level:
                        if (mLevelChart.getLineData().getDataSetCount() > 1) {
                            mLevelChart.getLineData().removeDataSet(1);
                            mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getYMax() + 5);
                            mLevelChart.getAxisLeft().setAxisMinValue(mLevelChart.getLineData().getYMin() - 5);
                            mLevelChart.notifyDataSetChanged();
                            mLevelChart.invalidate();
                        }
                        addCharakterystycznyLevel(MW);
                        break;
                    case R.id.hw_level:
                        if (mLevelChart.getLineData().getDataSetCount() > 1) {
                            mLevelChart.getLineData().removeDataSet(1);
                            mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getYMax() + 5);
                            mLevelChart.getAxisLeft().setAxisMinValue(mLevelChart.getLineData().getYMin() - 5);
                            mLevelChart.notifyDataSetChanged();
                            mLevelChart.invalidate();
                        }
                        addCharakterystycznyLevel(HW);
                        break;
                }
        };


        przeplywListner = (RadioGroup group, int checkedId) -> {
                switch (checkedId) {
                    case R.id.nothing_przeplyw:
                        if (mPrzeplywChart.getLineData().getDataSetCount() > 1) {
                            mPrzeplywChart.getLineData().removeDataSet(1);
                            mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getYMax() + 3);
                            mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getYMin() - 3);
                            mPrzeplywChart.notifyDataSetChanged();
                            mPrzeplywChart.invalidate();
                        }
                        break;
                    case R.id.lw_przeplyw:
                        if (mPrzeplywChart.getLineData().getDataSetCount() > 1) {
                            mPrzeplywChart.getLineData().removeDataSet(1);
                            mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getYMax() + 3);
                            mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getYMin() - 3);
                            mPrzeplywChart.notifyDataSetChanged();
                            mPrzeplywChart.invalidate();
                        }
                        addCharakterystycznyPrzeplyw(LW);
                        break;
                    case R.id.mw2_przeplyw:
                        if (mPrzeplywChart.getLineData().getDataSetCount() > 1) {
                            mPrzeplywChart.getLineData().removeDataSet(1);
                            mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getYMax() + 3);
                            mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getYMin() - 3);
                            mPrzeplywChart.notifyDataSetChanged();
                            mPrzeplywChart.invalidate();
                        }
                        addCharakterystycznyPrzeplyw(MW);
                        break;
                    case R.id.hw_przeplyw:
                        if (mPrzeplywChart.getLineData().getDataSetCount() > 1) {
                            mPrzeplywChart.getLineData().removeDataSet(1);
                            mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getYMax() + 3);
                            mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getYMin() - 3);
                            mPrzeplywChart.notifyDataSetChanged();
                            mPrzeplywChart.invalidate();
                        }
                        addCharakterystycznyPrzeplyw(HW);
                        break;
                }
        };
    }
}





