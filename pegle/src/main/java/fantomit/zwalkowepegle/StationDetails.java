package fantomit.zwalkowepegle;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.inject.Inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fantomit.zwalkowepegle.APImodels.MyRecord;
import fantomit.zwalkowepegle.APImodels.PrzeplywRecord;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.controllers.StationController;
import fantomit.zwalkowepegle.dialogs.EditCustomStationDialog;
import fantomit.zwalkowepegle.dialogs.MapDialog;
import fantomit.zwalkowepegle.dialogs.NoteDialog;
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
    @InjectView(R.id.updateDate)
    TextView mUpdateDate;
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

    private Toast mToast;

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
            mController.setmLevelHistoricStates(null);
            mController.setmPrzeplywHistoricStates(null);
            mController.loadStacja(getIntent().getStringExtra("STATION_ID"));
        } else if (savedInstanceState != null) {
            loadView(true);
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
        FragmentManager fm = getSupportFragmentManager();
        Bundle extras = new Bundle();
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
                EditCustomStationDialog dialog = new EditCustomStationDialog();
                extras.clear();
                extras.putString("ID", mController.getStacja().getId());
                dialog.setArguments(extras);
                dialog.show(fm, "Edit Station");
                break;
            case R.id.action_map:
                extras.clear();
                extras.putFloat("LON", mController.getStacja().getLon());
                extras.putFloat("LAN", mController.getStacja().getLan());
                extras.putString("NAME", mController.getStacja().getName());
                MapDialog mapDialog = new MapDialog();
                mapDialog.setArguments(extras);
                mapDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.hidetitle);
                mapDialog.show(fm, "Map Dialog");
                break;
            case R.id.action_note:
                extras.clear();
                extras.putString("ID", mController.getStacja().getId());
                NoteDialog noteDialog = new NoteDialog();
                noteDialog.setArguments(extras);
                noteDialog.show(fm, "Notes");
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
    public void loadView(boolean loadExistingData) {
        Log.e("FANTOM", mController.getStacja().getName() + " - loadView");
        if (mController == null || mController.getStacja() == null) {
            Toast.makeText(this, "Wyst¹pi³ nieznany b³¹d. Powiadom Developera o nazwie stacji/rzeki gdzie wyst¹pi³ b³¹d", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mController.getStacja().getHw_poziom() == -1) {
            mLevelSwitches.findViewById(R.id.hw_level).setVisibility(View.GONE);
        }
        if (mController.getStacja().getHw_przeplyw() == -1) {
            mPrzeplywSwitches.findViewById(R.id.hw_przeplyw).setVisibility(View.GONE);
        }
        if (!mController.pogodynkaStatesEnabled() && !mController.isUserCustomized()) {
            mLevelSwitches.findViewById(R.id.lw_level).setVisibility(View.GONE);
            mLevelSwitches.findViewById(R.id.mw2_level).setVisibility(View.GONE);
            mLevelSwitches.findViewById(R.id.hw_level).setVisibility(View.GONE);
            mPrzeplywSwitches.findViewById(R.id.lw_przeplyw).setVisibility(View.GONE);
            mPrzeplywSwitches.findViewById(R.id.mw2_przeplyw).setVisibility(View.GONE);
            mPrzeplywSwitches.findViewById(R.id.hw_przeplyw).setVisibility(View.GONE);
        }
        getSupportActionBar().setTitle(mController.getStacja().getName());
        String date = mController.getStacja().getStatus().getCurrentDate();
        if (date.contains("T") && date.contains("Z")) {
            String dzien = date.substring(0, date.indexOf('T'));
            String godzina = date.substring(date.indexOf('T') + 1, date.indexOf('Z'));
            //getSupportActionBar().setSubtitle(dzien + '\n' + godzina + " GMT");
            mUpdateDate.setText("Dane z " + dzien + " " + godzina + " GMT");
        }
        mCurrentLevel.setText(Integer.toString(mController.getStacja().getStatus().getCurrentValue()) + " cm");
        List<PrzeplywRecord> przeplywRecords = mController.getStacja().getDischargeRecords();
        String przeplyw = "";
        if (przeplywRecords != null && !przeplywRecords.isEmpty()) {
            przeplyw = Double.toString(przeplywRecords.get(przeplywRecords.size() - 1).getValue()) + " m<sup>3</sup>/s";
        }
        if(przeplyw.isEmpty()){
            przeplyw = "- m<sup>3</sup>/s";
        }
        mCurrentPrzeplyw.setText(Html.fromHtml(przeplyw), TextView.BufferType.SPANNABLE);

//        String tempVar = Double.toString(mController.getStacja().getStatus().getWarningValue());
//        mWarningLevel.setText((!tempVar.equals("0.0") ? tempVar : "-") + " cm");
//        tempVar = Double.toString(mController.getStacja().getStatus().getAlarmValue());
//        mAlarmLevel.setText((!tempVar.equals("0.0") ? tempVar : "-") + " cm");
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
        mLevelChart.setNoDataText(getString(R.string.no_data_msg));
        mPrzeplywChart.setNoDataText(getString(R.string.no_data_msg));
        if(loadExistingData){
            loadDataToLevelChart();
            loadDataToPrzeplywChart();
        }

        mLevelChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                if(BuildConfig.DEBUG){
                    Toast.makeText(StationDetails.this, "X Range: " + Integer.toString(mLevelChart.getLowestVisibleXIndex()) + " - "
                            + Integer.toString(mLevelChart.getHighestVisibleXIndex()) + " for Zoom X: " + Float.toString(mLevelChart.getScaleX()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                if(mLevelChart.getLineData().getDataSetCount() > 1){
                    if(mLevelChart.getLineData().getDataSetByIndex(0).getYMax() > mLevelChart.getLineData().getDataSetByIndex(1).getYMax()){
                        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(0).getYMax() + 5);
                    } else{
                        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(1).getYMax() + 5);
                    }

                    if(mLevelChart.getLineData().getDataSetByIndex(0).getYMin() > mLevelChart.getLineData().getDataSetByIndex(1).getYMin()){
                        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(1).getYMin() - 5);
                    } else {
                        mLevelChart.getAxisLeft().setAxisMinValue(mLevelChart.getLineData().getDataSetByIndex(0).getYMin() - 5);
                    }
                } else {
                    mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(0).getYMax() + 5);
                    mLevelChart.getAxisLeft().setAxisMinValue(mLevelChart.getLineData().getDataSetByIndex(0).getYMin() - 5);
                }
                int lowestXIndex = mLevelChart.getLowestVisibleXIndex();
                //Log.e("CHART", Integer.toString(lowestXIndex));
                if(lowestXIndex >= 300 && lowestXIndex <= 380 && !mController.isTriggerFired){ //po³owa miesi¹ca = 360
                    //Za³aduj dla kolejnego miesi¹ca

                    Log.e("CHART", "Obecnie jest danych: " + Integer.toString(mLevelChart.getXAxis().getValues().size()));
                    mController.isTriggerFired = true;
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        c.setTime(sdf.parse(mLevelChart.getXAxis().getValues().get(0)));
                    } catch (ParseException e){
                        e.printStackTrace();
                    }
                    String end = mLevelChart.getXAxis().getValues().get(0); //najwczeœniejsza data
                    c.add(Calendar.MONTH, -1);
                    //c.add(Calendar.DAY_OF_MONTH, -1);
                    String begin = sdf.format(c.getTime());
                    String controlDate = begin.substring(0, 9);
                    if(!controlDate.equals(mController.firstRecordDate)) {
                        Log.e("CHART", "Load another set");
                        mController.loadHistoricStates("level", begin, end, false);
                    }
                }
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                if(mLevelChart.getLineData().getDataSetCount() > 1){
                    if(mLevelChart.getLineData().getDataSetByIndex(0).getYMax() > mLevelChart.getLineData().getDataSetByIndex(1).getYMax()){
                        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(0).getYMax() + 5);
                    } else{
                        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(1).getYMax() + 5);
                    }

                    if(mLevelChart.getLineData().getDataSetByIndex(0).getYMin() > mLevelChart.getLineData().getDataSetByIndex(1).getYMin()){
                        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(1).getYMin() - 5);
                    } else {
                        mLevelChart.getAxisLeft().setAxisMinValue(mLevelChart.getLineData().getDataSetByIndex(0).getYMin() - 5);
                    }
                } else {
                    mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(0).getYMax() + 5);
                    mLevelChart.getAxisLeft().setAxisMinValue(mLevelChart.getLineData().getDataSetByIndex(0).getYMin() - 5);
                }
                int lowestXIndex = mLevelChart.getLowestVisibleXIndex();
                //Log.e("CHART", Integer.toString(lowestXIndex));
                if(lowestXIndex >= 300 && lowestXIndex <= 380 && !mController.isTriggerFired){ //po³owa miesi¹ca = 360
                    //Za³aduj dla kolejnego miesi¹ca
                    Log.e("CHART", "Obecnie jest danych: " + Integer.toString(mLevelChart.getXAxis().getValues().size()));
                    mController.isTriggerFired = true;
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        c.setTime(sdf.parse(mLevelChart.getXAxis().getValues().get(0)));
                    } catch (ParseException e){
                        e.printStackTrace();
                    }
                    String end = mLevelChart.getXAxis().getValues().get(0); //najwczeœniejsza data
                    c.add(Calendar.MONTH, -1);
                    //c.add(Calendar.DAY_OF_MONTH, -1);
                    String begin = sdf.format(c.getTime());
                    String controlDate = begin.substring(0, 9);
                    if(!controlDate.equals(mController.firstRecordDate)) {
                        Log.e("CHART", "Load another set");
                        mController.loadHistoricStates("level", begin, end, false);
                    }
                }
            }
        });

        mLevelChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                if(mToast != null) mToast.cancel();
                mToast = Toast.makeText(StationDetails.this, mLevelChart.getXAxis().getValues().get(e.getXIndex()) + " : " + Float.toString(e.getVal()) + " cm", Toast.LENGTH_SHORT);
                mToast.show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        mPrzeplywChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                if(BuildConfig.DEBUG){
                    Toast.makeText(StationDetails.this, "X Range: " + Integer.toString(mPrzeplywChart.getLowestVisibleXIndex()) + " - "
                            + Integer.toString(mPrzeplywChart.getHighestVisibleXIndex()) + " for Zoom X: " + Float.toString(mLevelChart.getScaleX()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                if(mPrzeplywChart.getLineData().getDataSetCount() > 1){
                    if(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax()){
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() + 2);
                    } else{
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax() + 2);
                    }

                    if(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMin()){
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMin() - 2);
                    } else {
                        mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() - 2);
                    }
                } else {
                    mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() + 2);
                    mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() - 2);
                }
                int lowestXIndex = mPrzeplywChart.getLowestVisibleXIndex();
                //Log.e("CHART", Integer.toString(lowestXIndex));
                if (lowestXIndex >= 300 && lowestXIndex <= 380 && !mController.isTriggerFired) { //po³owa miesi¹ca = 360
                    //Za³aduj dla kolejnego miesi¹ca
                    Log.e("CHART", "Obecnie jest danych: " + Integer.toString(mPrzeplywChart.getXAxis().getValues().size()));
                    mController.isTriggerFired = true;
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        c.setTime(sdf.parse(mPrzeplywChart.getXAxis().getValues().get(0)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String end = mPrzeplywChart.getXAxis().getValues().get(0); //najwczeœniejsza data
                    c.add(Calendar.MONTH, -1);

                    String begin = sdf.format(c.getTime());
                    String controlDate = begin.substring(0, 9);
                    if(!controlDate.equals(mController.firstRecordDate)) {
                        Log.e("CHART", "Load another set");
                        mController.loadHistoricStates("przeplyw", begin, end, false);
                    }
                }
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                if(mPrzeplywChart.getLineData().getDataSetCount() > 1){
                    if(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax()){
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() + 2);
                    } else{
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax() + 2);
                    }

                    if(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMin()){
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMin() - 2);
                    } else {
                        mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() - 2);
                    }
                } else {
                    mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() + 2);
                    mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() - 2);
                }
                int lowestXIndex = mPrzeplywChart.getLowestVisibleXIndex();
                //Log.e("CHART", Integer.toString(lowestXIndex));
                if (lowestXIndex >= 300 && lowestXIndex <= 380 && !mController.isTriggerFired) { //po³owa miesi¹ca = 360
                    //Za³aduj dla kolejnego miesi¹ca
                    Log.e("CHART", "Obecnie jest danych: " + Integer.toString(mPrzeplywChart.getXAxis().getValues().size()));
                    mController.isTriggerFired = true;
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        c.setTime(sdf.parse(mPrzeplywChart.getXAxis().getValues().get(0)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String end = mPrzeplywChart.getXAxis().getValues().get(0); //najwczeœniejsza data
                    c.add(Calendar.MONTH, -1);
                    //c.add(Calendar.DAY_OF_MONTH, -1);
                    String begin = sdf.format(c.getTime());
                    String controlDate = begin.substring(0,9);
                    if(!controlDate.equals(mController.firstRecordDate)) {
                        Log.e("CHART", "Load another set");
                        mController.loadHistoricStates("przeplyw", begin, end, false);
                    }
                }
            }
        });

        mPrzeplywChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                if(mToast != null) mToast.cancel();
                mToast = Toast.makeText(StationDetails.this, mPrzeplywChart.getXAxis().getValues().get(e.getXIndex()) + " : " + Float.toString(e.getVal()) + " m3/s", Toast.LENGTH_SHORT);
                mToast.show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    @Override
    public void loadDataToLevelChart() {
        Station stacja = mController.getStacja();
        if (stacja == null) {
            return;
        }
        Log.e("FANTOM", stacja.getName() + " - loadLevelChart");
        //List<StateRecord> stateRecords = stacja.getWaterStateRecords();
        List<MyRecord> stateRecords = mController.getmLevelHistoricStates();
        mController.firstRecordDate = stateRecords.get(0).getDate().substring(0,9);
        List<Entry> yVals = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        if (stateRecords == null || stateRecords.isEmpty()) {
            return;
        }
        for (MyRecord record : stateRecords) {
//            String date = record.getDate_level();
//            String dzien = date.substring(0, date.indexOf('T'));
//            String godzina = date.substring(date.indexOf('T') + 1, date.indexOf('Z'));
            yVals.add(new Entry(new Float(record.getValue()), i));
            labels.add(record.getDate());
            i++;
        }

        LineDataSet pobrane = new LineDataSet(yVals, "Poziom wody [cm]");
        pobrane.setDrawValues(false);
        pobrane.setCircleSize(0);
        pobrane.setLineWidth(1.5f);
        pobrane.setColor(ContextCompat.getColor(this, R.color.level_chart));

        LineData data = new LineData(labels, pobrane);
        mLevelChart.setData(data);
        YAxis leftAxis = mLevelChart.getAxisLeft();
        mLevelChart.getAxisRight().setEnabled(false);
        leftAxis.setStartAtZero(false);
        if(mLevelChart.getLineData().getDataSetCount() > 1){
            if(data.getYMax() > mLevelChart.getLineData().getDataSetByIndex(1).getYMax()){
                mLevelChart.getAxisLeft().setAxisMaxValue(data.getYMax() + 5);
            } else{
                mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(1).getYMax() + 5);
            }
            
            if(data.getYMin() > mLevelChart.getLineData().getDataSetByIndex(1).getYMin()){
                mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(1).getYMin() - 5);
            } else {
                mLevelChart.getAxisLeft().setAxisMinValue(data.getYMin() - 5);
            }
        } else {
            mLevelChart.getAxisLeft().setAxisMaxValue(data.getYMax() + 5);
            mLevelChart.getAxisLeft().setAxisMinValue(data.getYMin() - 5);
        }
        Log.e("CHART", "Ymin: " + Float.toString(data.getYMin()));
        mLevelChart.setTouchEnabled(true);
        mLevelChart.setScaleYEnabled(false);
        mLevelChart.setDescription("");
        mLevelChart.setNoDataText(getString(R.string.no_data_msg));
        mLevelChart.setAutoScaleMinMaxEnabled(true);
        mLevelChart.setDrawGridBackground(false);
        if(!mController.isTriggerFired) {
            mLevelChart.moveViewToX(mLevelChart.getLineData().getXValCount() - 1);
        }
        mLevelChart.notifyDataSetChanged();
        mLevelChart.invalidate();
        float zoomX = mLevelChart.getXValCount()/72;
        mLevelChart.zoom(zoomX, 1.0f, 0, 0);
        mController.isTriggerFired = false;
        if(mLevelChart.isEmpty()){
            mLevelSwitches.findViewById(R.id.nothing_level).setVisibility(View.GONE);
        }
        hideProgressSpinner();
    }

    @Override
    public void loadDataToPrzeplywChart() {
        mPrzeplywChart.setNoDataText(getString(R.string.no_data_msg));
        Station stacja = mController.getStacja();
        if (stacja == null) {
            return;
        }
        Log.e("FANTOM", stacja.getName() + " - loadPrzeplywChart");
        //List<PrzeplywRecord> stateRecords = stacja.getDischargeRecords();
        List<MyRecord> stateRecords = mController.getmPrzeplywHistoricStates();
        List<Entry> yVals = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        if (stateRecords == null || stateRecords.isEmpty()) {
            return;
        }
        for (MyRecord record : stateRecords) {
//            String date = ;
//            String dzien = date.substring(0, date.indexOf('T'));
//            String godzina = date.substring(date.indexOf('T') + 1, date.indexOf('Z'));
            yVals.add(new Entry(new Float(record.getValue()), i));
            labels.add(record.getDate());
            i++;
        }

        LineDataSet pobrane = new LineDataSet(yVals, "Przep³yw [m3/s]");
        pobrane.setDrawValues(false);
        pobrane.setCircleSize(0);
        pobrane.setLineWidth(1.5f);
        pobrane.setColor(ContextCompat.getColor(this, R.color.przeplyw_chart));

        LineData data = new LineData(labels, pobrane);
        if (data != null && data.getDataSetCount() > 0) {
            mPrzeplywChart.setData(data);
        }
        YAxis leftAxis = mLevelChart.getAxisLeft();
        mPrzeplywChart.getAxisRight().setEnabled(false);
        leftAxis.setStartAtZero(false);
        if(mPrzeplywChart.getLineData().getDataSetCount() > 1){
            if(data.getYMax() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax()){
                mPrzeplywChart.getAxisLeft().setAxisMaxValue(data.getYMax() + 2);
            } else{
                mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax() + 2);
            }

            if(data.getYMin() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMin()){
                mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMin() - 2);
            } else {
                mPrzeplywChart.getAxisLeft().setAxisMinValue(data.getYMin() - 2);
            }
        } else {
            mPrzeplywChart.getAxisLeft().setAxisMaxValue(data.getYMax() + 2);
            mPrzeplywChart.getAxisLeft().setAxisMinValue(data.getYMin() - 2);
        }
        mPrzeplywChart.setTouchEnabled(true);
        mPrzeplywChart.setScaleYEnabled(false);
        mPrzeplywChart.setDescription("");
        mPrzeplywChart.setAutoScaleMinMaxEnabled(true);
        mPrzeplywChart.setDrawGridBackground(false);
        if (!mController.isTriggerFired) {
            mPrzeplywChart.moveViewToX(mPrzeplywChart.getLineData().getXValCount() - 1);
        }
        mPrzeplywChart.notifyDataSetChanged();
        mPrzeplywChart.invalidate();
        float zoomX = mPrzeplywChart.getXValCount()/72;
        mPrzeplywChart.zoom(zoomX, 1.0f, 0, 0);
        mController.isTriggerFired = false;
        if(mPrzeplywChart.isEmpty()){
            mPrzeplywSwitches.findViewById(R.id.nothing_przeplyw).setVisibility(View.GONE);
        }
        hideProgressSpinner();
    }

    @Override
    protected void onDestroy() {
        mController.setView(null);
        super.onDestroy();
    }

    private void addCharakterystycznyLevel(@WaterStates int state) {
        List<Entry> yVals2 = new ArrayList<>();
        List<MyRecord> stateRecords = mController.getmLevelHistoricStates();
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
        charakterystyczne.setColor(ContextCompat.getColor(this, R.color.black));
        mLevelChart.getLineData().addDataSet(charakterystyczne);
        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getYMax() + 5);
        mLevelChart.getAxisLeft().setAxisMinValue(mLevelChart.getLineData().getYMin() - 5);
        mLevelChart.notifyDataSetChanged();
        mLevelChart.invalidate();
    }

    private void addCharakterystycznyPrzeplyw(@WaterStates int state) {
        List<Entry> yVals = new ArrayList<>();
        List<MyRecord> stateRecords = mController.getmPrzeplywHistoricStates();
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
        charakterystyczne.setColor(ContextCompat.getColor(this, R.color.black));

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

    public void loadNewDataToLevelChart(){

    }
}





