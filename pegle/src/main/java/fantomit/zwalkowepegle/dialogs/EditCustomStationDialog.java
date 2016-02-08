package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.Switch;

import javax.inject.Inject;

import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.R;
import fantomit.zwalkowepegle.Statics;
import fantomit.zwalkowepegle.ZwalkiApplication;
import fantomit.zwalkowepegle.db.repositories.StationRepository;


public class EditCustomStationDialog extends AppCompatDialogFragment {

    private Switch notifSwitcher;
    private Button bClear;
    private TextView notifHint;
    private RadioGroup statesSwitcher;

    private boolean cleared = false;

    @Inject
    StationRepository repoStation;

    private Station mStation;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ZwalkiApplication.getApp().component.inject(this);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.edit_station, null);
        setupView(rootView);

        String idStation = getArguments().getString(Statics._STATION_ID);
        mStation = repoStation.findById(idStation);
        if (mStation.isNotifByPrzeplyw()) {
            notifSwitcher.setChecked(false);
            notifHint.setText("Powiadomiaj dla progowego przep³ywu");
        } else {
            notifSwitcher.setChecked(true);
            notifHint.setText("Powiadomiaj dla progowego poziomu");
        }

        if (mStation.getNotifCheckedId() != -1) {
            statesSwitcher.check(mStation.getNotifCheckedId());
        } else {
            statesSwitcher.check(R.id.lw);
            mStation.setNotifCheckedId(R.id.lw);
        }

        notifSwitcher.setOnCheckedChangeListener((Switch view, boolean checked) -> {
            statesSwitcher.check(mStation.getNotifCheckedId());
            if (checked) {
                notifHint.setText("Powiadomiaj dla progowego poziomu");
                mStation.setNotifByPrzeplyw(false);
            } else {
                notifHint.setText("Powiadomiaj dla progowego przep³ywu");
                mStation.setNotifByPrzeplyw(true);
            }
        });

        setListeners();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView)
                .setTitle("Edytuj powiadomienia dla stacji")
                .setMessage("Wybierz wg czego chcesz byæ powiadamiany i od jakiego poziomu")
                .setPositiveButton("Ok", (DialogInterface dialog, int which) -> {
                            repoStation.createOrUpdate(mStation);
                        }
                )
                .setNegativeButton("Anuluj", (DialogInterface dialog, int which) -> {
                            dismiss();
                        }
                );
        return builder.create();
    }

    private void setListeners() {
        bClear.setOnClickListener((View v) -> {
                    mStation.setIsByDefaultCustomized(false);
                    mStation.setIsUserCustomized(false);
                    cleared = true;
                    Toast.makeText(getActivity(), "Przywrócono stany charakterystyczne na pobrane z serwera", Toast.LENGTH_SHORT);
                }
        );

        statesSwitcher.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
                    mStation.setNotifCheckedId(checkedId);
                    switch (checkedId) {
                        case R.id.lw:
                            mStation.setNotifHint("LW");
                            if (!mStation.isNotifByPrzeplyw()) {
                                if (mStation.getLw_poziom() != -1) {
                                    mStation.setDolnaGranicaPoziomu(mStation.getLw_poziom());
                                } else {
                                    Double var = new Double(mStation.getStatus().getLowValue());
                                    mStation.setDolnaGranicaPoziomu(var.intValue());
                                }
                            } else {
                                if (mStation.getLw_przeplyw() != -1) {
                                    mStation.setDolnaGranicaPrzeplywu(mStation.getLw_przeplyw());
                                } else {
                                    Double var = new Double(mStation.getLowDischargeValue());
                                    mStation.setDolnaGranicaPrzeplywu(var.intValue());
                                }
                            }
                            break;
                        case R.id.mw2:
                            mStation.setNotifHint("MW");
                            if (!mStation.isNotifByPrzeplyw()) {
                                if (mStation.getMw2_poziom() != -1) {
                                    mStation.setDolnaGranicaPoziomu(mStation.getMw2_poziom());
                                } else {
                                    Double var = new Double(mStation.getStatus().getHighValue());
                                    mStation.setDolnaGranicaPoziomu(var.intValue());
                                }
                            } else {
                                if (mStation.getMw2_przeplyw() != -1) {
                                    mStation.setDolnaGranicaPrzeplywu(mStation.getMw2_przeplyw());
                                } else {
                                    Double var = new Double(mStation.getHighDischargeValue());
                                    mStation.setDolnaGranicaPrzeplywu(var.intValue());
                                }
                            }
                            break;
                        case R.id.hw:
                            mStation.setNotifHint("HW");
                            if (!mStation.isNotifByPrzeplyw()) {
                                if (mStation.getHw_poziom() != -1) {
                                    mStation.setDolnaGranicaPoziomu(mStation.getHw_poziom());
                                } else {
                                    Double var = new Double(mStation.getStatus().getHighValue());
                                    mStation.setDolnaGranicaPoziomu(var.intValue());
                                }
                            } else {
                                if (mStation.getHw_przeplyw() != -1) {
                                    mStation.setDolnaGranicaPrzeplywu(mStation.getHw_przeplyw());
                                } else {
                                    Double var = new Double(mStation.getHighDischargeValue());
                                    mStation.setDolnaGranicaPrzeplywu(var.intValue());
                                }
                            }
                            break;
                    }
                }
        );
    }

    private void setupView(View rootView) {
        bClear = (Button) rootView.findViewById(R.id.bClear);
        notifSwitcher = (Switch) rootView.findViewById(R.id.notif_switcher);
        notifHint = (TextView) rootView.findViewById(R.id.notif_hint);
        statesSwitcher = (RadioGroup) rootView.findViewById(R.id.statesSwitcher);
    }
}
