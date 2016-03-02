package fantomit.zwalkowepegle;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import fantomit.zwalkowepegle.APImodels.MyRecord;
import fantomit.zwalkowepegle.APImodels.PrzeplywRecord;
import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.controllers.StationController;
import fantomit.zwalkowepegle.dialogs.EditCustomStationDialog;
import fantomit.zwalkowepegle.dialogs.MapDialog;
import fantomit.zwalkowepegle.dialogs.NoteDialog;
import fantomit.zwalkowepegle.interfaces.StationDetailsInterface;

public class StationDetails extends AppCompatActivity implements StationDetailsInterface {

    @Inject
    StationController mController;

    @Bind(R.id.currentLevel)
    TextView mCurrentLevel;
    @Bind(R.id.currentPrzewplyw)
    TextView mCurrentPrzeplyw;
    @Bind(R.id.updateDate)
    TextView mUpdateDate;
    @Bind(R.id.trend)
    ImageView mTrend;
    @Bind(R.id.chartLevel)
    LineChart mLevelChart;
    @Bind(R.id.chartPrzeplyw)
    LineChart mPrzeplywChart;
    @Bind(R.id.linlaHeaderProgress)
    LinearLayout mProgressLayout;
    private MenuItem mFavButton;
    @Bind(R.id.level_switches)
    RadioGroup mLevelSwitches;
    @Bind(R.id.przeplyw_switches)
    RadioGroup mPrzeplywSwitches;

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

    private static final SimpleDateFormat _DATE_FORMATER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Calendar calendar;

    private void setupActivity() {
        calendar = Calendar.getInstance();
        ZwalkiApplication.getApp().component.inject(this);
        setContentView(R.layout.station_details);
        mController.setView(this);
        ButterKnife.bind(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.primary)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity();
        createListners();

        Log.i(getClass().getSimpleName(), Boolean.toString(getIntent().getExtras() != null));
        showProgressSpinner();
        if (getIntent().getExtras().containsKey(Statics._STATION_ID) && savedInstanceState == null) {
            mController.setmLevelHistoricStates(null);
            mController.setmPrzeplywHistoricStates(null);
            mController.loadStacja(getIntent().getStringExtra(Statics._STATION_ID));
        } else if (savedInstanceState != null) {
            loadView(true);
        }

        mLevelSwitches.setOnCheckedChangeListener(levelListener);
        mPrzeplywSwitches.setOnCheckedChangeListener(przeplywListner);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_station, menu);
        mFavButton = menu.findItem(R.id.action_favourite);
        mFavButton.setIcon(mController.isStationFav() ? R.drawable.star_selector_filled : R.drawable.star_selector_white);
        Log.i(StationDetails.class.getSimpleName(), "Stacja " + mController.getStacja().getName() + "-" + mController.getStacja().getId() + (mController.isStationFav() ? " ULUBIONA" : " NIE ULUBIONA"));
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
                    item.setIcon(R.drawable.star_outline_white);
                    Toast.makeText(this, "Usuniêto z ulubionych", Toast.LENGTH_SHORT).show();
                } else {
                    mController.addToFavourite();
                    item.setIcon(R.drawable.star_selector_filled);
                    Toast.makeText(this, "Dodano do ulubionych", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_edit:
                EditCustomStationDialog dialog = new EditCustomStationDialog();
                extras.clear();
                extras.putString(Statics._STATION_ID, mController.getStacja().getId());
                dialog.setArguments(extras);
                dialog.show(fm, EditCustomStationDialog.class.getSimpleName());
                break;
            case R.id.action_map:
                extras.clear();
                extras.putFloat(Statics._LONGITUDE, mController.getStacja().getLon());
                extras.putFloat(Statics._LANGITUDE, mController.getStacja().getLan());
                extras.putString(Statics._STATION_NAME, mController.getStacja().getName());
                MapDialog mapDialog = new MapDialog();
                mapDialog.setArguments(extras);
                mapDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.hidetitle);
                mapDialog.show(fm, MapDialog.class.getSimpleName());
                break;
            case R.id.action_note:
                extras.clear();
                extras.putString(Statics._STATION_ID, mController.getStacja().getId());
                NoteDialog noteDialog = new NoteDialog();
                noteDialog.setArguments(extras);
                noteDialog.show(fm, NoteDialog.class.getSimpleName());
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
        if (mController == null || mController.getStacja() == null || mLevelSwitches == null) {
            Toast.makeText(this, "Wyst¹pi³ nieznany b³¹d. Powiadom Developera o nazwie stacji/rzeki gdzie wyst¹pi³ b³¹d", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(getClass().getSimpleName(), mController.getStacja().getName() != null ? mController.getStacja().getName() + " - loadView" : "Invalid station - loadView");

        if (mController.getStacja().getLw_poziom() == -1 || mController.getStacja().getStatus().getLowValue() == 0) {
            mLevelSwitches.findViewById(R.id.lw_level).setVisibility(View.GONE);
        }
        if (mController.getStacja().getLw_przeplyw() == -1 || mController.getStacja().getLowDischargeValue() == 0.0) {
            mPrzeplywSwitches.findViewById(R.id.lw_przeplyw).setVisibility(View.GONE);
        }
        if (mController.getStacja().getMw2_poziom() == -1) {
            mLevelSwitches.findViewById(R.id.mw2_level).setVisibility(View.GONE);
        }
        if (mController.getStacja().getMw2_przeplyw() == -1) {
            mPrzeplywSwitches.findViewById(R.id.mw2_przeplyw).setVisibility(View.GONE);
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
            mUpdateDate.setText("Dane z " + dzien + " " + godzina + " GMT");
        }
        mCurrentLevel.setText(Integer.toString(mController.getStacja().getStatus().getCurrentValue()) + " cm");
        List<PrzeplywRecord> przeplywRecords = mController.getStacja().getDischargeRecords();
        String przeplyw = "";
        if (przeplywRecords != null && !przeplywRecords.isEmpty()) {
            przeplyw = Double.toString(przeplywRecords.get(przeplywRecords.size() - 1).getValue()) + " m<sup>3</sup>/s";
        }
        if (przeplyw.isEmpty()) {
            przeplyw = "- m<sup>3</sup>/s";
        }
        mCurrentPrzeplyw.setText(Html.fromHtml(przeplyw), TextView.BufferType.SPANNABLE);

        String trend = mController.getStacja().getTrend();
        if (trend.equals("const")) {
            mTrend.setImageResource(R.drawable.trending_neutral);
            mTrend.setMinimumHeight(48);
        } else if (trend.equals("down")) {
            mTrend.setImageResource(R.drawable.trending_down);
            mTrend.setMinimumHeight(48);
        } else if (trend.equals("up")) {
            mTrend.setImageResource(R.drawable.trending_up);
            mTrend.setMinimumHeight(48);
        } else {
            mTrend.setImageResource(R.drawable.help_black);
            mTrend.setMinimumHeight(36);
        }
        mLevelChart.setNoDataText(getString(R.string.no_data_msg));
        mPrzeplywChart.setNoDataText(getString(R.string.no_data_msg));
        if (loadExistingData) {
            loadDataToLevelChart();
            loadDataToPrzeplywChart();
        }

        mLevelChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                if (BuildConfig.DEBUG) {
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
                if (mLevelChart.getLineData().getDataSetCount() > 1) {
                    if (mLevelChart.getLineData().getDataSetByIndex(0).getYMax() > mLevelChart.getLineData().getDataSetByIndex(1).getYMax()) {
                        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(0).getYMax() + 5);
                    } else {
                        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(1).getYMax() + 5);
                    }

                    if (mLevelChart.getLineData().getDataSetByIndex(0).getYMin() > mLevelChart.getLineData().getDataSetByIndex(1).getYMin()) {
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
                if ((lowestXIndex == 0 || (lowestXIndex >= 300 && lowestXIndex <= 380)) && !mController.isTriggerFired) { //po³owa miesi¹ca = 360
                    //Za³aduj dla kolejnego miesi¹ca

                    Log.i(getClass().getSimpleName(), "OnChartScale - Obecnie jest danych: " + Integer.toString(mLevelChart.getXAxis().getValues().size()));
                    mController.isTriggerFired = true;
                    try {
                        calendar.setTime(_DATE_FORMATER.parse(mLevelChart.getXAxis().getValues().get(0)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String end = mLevelChart.getXAxis().getValues().get(0); //najwczeœniejsza data
                    calendar.add(Calendar.MONTH, -1);
                    String begin = _DATE_FORMATER.format(calendar.getTime());
                    String controlDate = begin.substring(0, 9);
                    if (!controlDate.equals(mController.firstRecordDate)) {
                        Log.i(getClass().getSimpleName(), "OnChartScale - Load another set");
                        mController.loadHistoricStates(Statics._LEVEL, begin, end, false);
                    }
                }
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                if (mLevelChart.getLineData().getDataSetCount() > 1) {
                    if (mLevelChart.getLineData().getDataSetByIndex(0).getYMax() > mLevelChart.getLineData().getDataSetByIndex(1).getYMax()) {
                        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(0).getYMax() + 5);
                    } else {
                        mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(1).getYMax() + 5);
                    }

                    if (mLevelChart.getLineData().getDataSetByIndex(0).getYMin() > mLevelChart.getLineData().getDataSetByIndex(1).getYMin()) {
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
                if ((lowestXIndex == 0 || (lowestXIndex >= 300 && lowestXIndex <= 380)) && !mController.isTriggerFired) { //po³owa miesi¹ca = 360
                    //Za³aduj dla kolejnego miesi¹ca
                    Log.i(getClass().getSimpleName(), "OnTranslate: Obecnie jest danych: " + Integer.toString(mLevelChart.getXAxis().getValues().size()));
                    mController.isTriggerFired = true;
                    try {
                        calendar.setTime(_DATE_FORMATER.parse(mLevelChart.getXAxis().getValues().get(0)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String end = mLevelChart.getXAxis().getValues().get(0); //najwczeœniejsza data
                    calendar.add(Calendar.MONTH, -1);
                    //calendar.add(Calendar.DAY_OF_MONTH, -1);
                    String begin = _DATE_FORMATER.format(calendar.getTime());
                    String controlDate = begin.substring(0, 9);
                    if (!controlDate.equals(mController.firstRecordDate)) {
                        Log.i(getClass().getSimpleName(), "OnTranslate - Load another set");
                        mController.loadHistoricStates(Statics._LEVEL, begin, end, false);
                    }
                }
            }
        });

        mLevelChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(StationDetails.this, mLevelChart.getXAxis().getValues().get(e.getXIndex()) + " : " + Float.toString(e.getVal()) + " cm", Toast.LENGTH_SHORT);
                mToast.show();
            }

            @Override
            public void onNothingSelected() {
            }
        });

        mPrzeplywChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                if (BuildConfig.DEBUG) {
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
                if (mPrzeplywChart.getLineData().getDataSetCount() > 1) {
                    if (mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax()) {
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() + 2);
                    } else {
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax() + 2);
                    }

                    if (mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMin()) {
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMin() - 2);
                    } else {
                        mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() - 2);
                    }
                } else {
                    mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() + 2);
                    mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() - 2);
                }
                int lowestXIndex = mPrzeplywChart.getLowestVisibleXIndex();
                //Log.e("CHART-Scale", Integer.toString(lowestXIndex));
                if ((lowestXIndex == 0 || (lowestXIndex >= 300 && lowestXIndex <= 380)) && !mController.isTriggerFired) { //po³owa miesi¹ca = 360
                    //Za³aduj dla kolejnego miesi¹ca
                    Log.i(getClass().getSimpleName(), "OnChartScale - Obecnie jest danych: " + Integer.toString(mPrzeplywChart.getXAxis().getValues().size()));
                    mController.isTriggerFired = true;
                    try {
                        calendar.setTime(_DATE_FORMATER.parse(mPrzeplywChart.getXAxis().getValues().get(0)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String end = mPrzeplywChart.getXAxis().getValues().get(0); //najwczeœniejsza data
                    calendar.add(Calendar.MONTH, -1);

                    String begin = _DATE_FORMATER.format(calendar.getTime());
                    String controlDate = begin.substring(0, 9);
                    if (!controlDate.equals(mController.firstRecordDate)) {
                        Log.i(getClass().getSimpleName(), "OnChartScale - Load another set");
                        mController.loadHistoricStates(Statics._PRZEPLYW, begin, end, false);
                    }
                }
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                if (mPrzeplywChart.getLineData().getDataSetCount() > 1) {
                    if (mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax()) {
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() + 2);
                    } else {
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax() + 2);
                    }

                    if (mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMin()) {
                        mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMin() - 2);
                    } else {
                        mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() - 2);
                    }
                } else {
                    mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMax() + 2);
                    mPrzeplywChart.getAxisLeft().setAxisMinValue(mPrzeplywChart.getLineData().getDataSetByIndex(0).getYMin() - 2);
                }
                int lowestXIndex = mPrzeplywChart.getLowestVisibleXIndex();
                //Log.e("CHART-Translate", Integer.toString(lowestXIndex));
                if ((lowestXIndex == 0 || (lowestXIndex >= 300 && lowestXIndex <= 380)) && !mController.isTriggerFired) { //po³owa miesi¹ca = 360
                    //Za³aduj dla kolejnego miesi¹ca
                    Log.i(getClass().getSimpleName(), "OnChartTranslate - Obecnie jest danych: " + Integer.toString(mPrzeplywChart.getXAxis().getValues().size()));
                    mController.isTriggerFired = true;
                    try {
                        calendar.setTime(_DATE_FORMATER.parse(mPrzeplywChart.getXAxis().getValues().get(0)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String end = mPrzeplywChart.getXAxis().getValues().get(0); //najwczeœniejsza data
                    calendar.add(Calendar.MONTH, -1);
                    //calendar.add(Calendar.DAY_OF_MONTH, -1);
                    String begin = _DATE_FORMATER.format(calendar.getTime());
                    String controlDate = begin.substring(0, 9);
                    if (!controlDate.equals(mController.firstRecordDate)) {
                        Log.i(getClass().getSimpleName(), "OnChartTranslate - Load another set");
                        mController.loadHistoricStates(Statics._PRZEPLYW, begin, end, false);
                    }
                }
            }
        });

        mPrzeplywChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                if (mToast != null) mToast.cancel();
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
        Log.i(getClass().getSimpleName(), stacja.getName() + " - loadLevelChart");
        List<MyRecord> stateRecords = mController.getmLevelHistoricStates();
        mController.firstRecordDate = stateRecords != null && stateRecords.get(0) != null ? stateRecords.get(0).getDate().substring(0, 9) : "";
        List<Entry> yVals = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        if (stateRecords == null || stateRecords.isEmpty()) {
            return;
        }
        for (MyRecord record : stateRecords) {
            yVals.add(new Entry(new Float(record.getValue()), i));
            labels.add(record.getDate());
            i++;
        }

        LineDataSet pobrane = new LineDataSet(yVals, "Poziom wody [cm]");
        pobrane.setDrawValues(false);
        pobrane.setCircleRadius(0);
        pobrane.setLineWidth(1.5f);
        pobrane.setColor(ContextCompat.getColor(this, R.color.level_chart));

        LineData data = new LineData(labels, pobrane);
        mLevelChart.setData(data);
        YAxis leftAxis = mLevelChart.getAxisLeft();
        mLevelChart.getAxisRight().setEnabled(false);
        leftAxis.setStartAtZero(false);
        if (mLevelChart.getLineData().getDataSetCount() > 1) {
            if (data.getYMax() > mLevelChart.getLineData().getDataSetByIndex(1).getYMax()) {
                mLevelChart.getAxisLeft().setAxisMaxValue(data.getYMax() + 5);
            } else {
                mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(1).getYMax() + 5);
            }

            if (data.getYMin() > mLevelChart.getLineData().getDataSetByIndex(1).getYMin()) {
                mLevelChart.getAxisLeft().setAxisMaxValue(mLevelChart.getLineData().getDataSetByIndex(1).getYMin() - 5);
            } else {
                mLevelChart.getAxisLeft().setAxisMinValue(data.getYMin() - 5);
            }
        } else {
            mLevelChart.getAxisLeft().setAxisMaxValue(data.getYMax() + 5);
            mLevelChart.getAxisLeft().setAxisMinValue(data.getYMin() - 5);
        }
        Log.i(getClass().getSimpleName(), "Ymin: " + Float.toString(data.getYMin()));
        mLevelChart.setTouchEnabled(true);
        mLevelChart.setScaleYEnabled(false);
        mLevelChart.setDescription("");
        mLevelChart.setNoDataText(getString(R.string.no_data_msg));
        mLevelChart.setAutoScaleMinMaxEnabled(true);
        mLevelChart.setDrawGridBackground(false);
        if (!mController.isTriggerFired) {
            mLevelChart.moveViewToX(mLevelChart.getLineData().getXValCount() - 1);
        }
        mLevelChart.notifyDataSetChanged();
        mLevelChart.invalidate();
        if (!mController.isTriggerFired) {
            float zoomX = mLevelChart.getXValCount() / 72;
            mLevelChart.zoom(zoomX, 1.0f, 0, 0);
        }
        mController.isTriggerFired = false;
        if (mLevelChart.isEmpty()) {
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
        Log.i(getClass().getSimpleName(), stacja.getName() + " - loadPrzeplywChart");
        List<MyRecord> stateRecords = mController.getmPrzeplywHistoricStates();
        List<Entry> yVals = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        if (stateRecords == null || stateRecords.isEmpty()) {
            return;
        }
        for (MyRecord record : stateRecords) {
            yVals.add(new Entry(new Float(record.getValue()), i));
            labels.add(record.getDate());
            i++;
        }

        LineDataSet pobrane = new LineDataSet(yVals, "Przep³yw [m3/s]");
        pobrane.setDrawValues(false);
        pobrane.setCircleRadius(0);
        pobrane.setLineWidth(1.5f);
        pobrane.setColor(ContextCompat.getColor(this, R.color.przeplyw_chart));

        LineData data = new LineData(labels, pobrane);
        if (data != null && data.getDataSetCount() > 0) {
            mPrzeplywChart.setData(data);
        }
        YAxis leftAxis = mLevelChart.getAxisLeft();
        mPrzeplywChart.getAxisRight().setEnabled(false);
        leftAxis.setStartAtZero(false);
        if (mPrzeplywChart.getLineData().getDataSetCount() > 1) {
            if (data.getYMax() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax()) {
                mPrzeplywChart.getAxisLeft().setAxisMaxValue(data.getYMax() + 2);
            } else {
                mPrzeplywChart.getAxisLeft().setAxisMaxValue(mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMax() + 2);
            }

            if (data.getYMin() > mPrzeplywChart.getLineData().getDataSetByIndex(1).getYMin()) {
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
        if (!mController.isTriggerFired) {
            float zoomX = mPrzeplywChart.getXValCount() / 72;
            mPrzeplywChart.zoom(zoomX, 1.0f, 0, 0);
        }
        mController.isTriggerFired = false;
        if (mPrzeplywChart.isEmpty()) {
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
        charakterystyczne.setCircleRadius(0);
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
        charakterystyczne.setCircleRadius(0);
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
            if (mLevelChart != null && mLevelChart.getLineData() != null) {
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
            }
        };

        przeplywListner = (RadioGroup group, int checkedId) -> {
            if (mPrzeplywChart != null && mPrzeplywChart.getLineData() != null) {
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
            }
        };
    }
}





