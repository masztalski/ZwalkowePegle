package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.rey.material.widget.Switch;

import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.R;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import roboguice.fragment.RoboDialogFragment;

public class EditCustomStationDialog extends RoboDialogFragment {

    private Switch notifSwitcher;
    private Button bClear;
    private TextView notifHint;
    private RadioGroup statesSwitcher;

    private boolean cleared = false;

    @Inject
    private StationRepository repoStation;

    private Station mStation;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.edit_station, null);
        bClear = (Button) rootView.findViewById(R.id.bClear);
        notifSwitcher = (Switch) rootView.findViewById(R.id.notif_switcher);
        notifHint = (TextView) rootView.findViewById(R.id.notif_hint);
        statesSwitcher = (RadioGroup) rootView.findViewById(R.id.statesSwitcher);

        String idStation = getArguments().getString("ID");
        mStation = repoStation.findById(idStation);
        if(mStation.isNotifByPrzeplyw()){
            notifSwitcher.setChecked(false);
            notifHint.setText("Powiadomiaj dla progowego przep³ywu");
        } else {
            notifSwitcher.setChecked(true);
            notifHint.setText("Powiadomiaj dla progowego poziomu");
        }

        notifSwitcher.setOnCheckedChangeListener((Switch view, boolean checked) -> {
                statesSwitcher.check(mStation.getNotifCheckedId());
                if(checked){
                    notifHint.setText("Powiadomiaj dla progowego poziomu");
                    mStation.setNotifByPrzeplyw(false);
                } else{
                    notifHint.setText("Powiadomiaj dla progowego przep³ywu");
                    mStation.setNotifByPrzeplyw(true);
                }
        });

        bClear.setOnClickListener((View v) -> {
                mStation.setIsByDefaultCustomized(false);
                mStation.setIsUserCustomized(false);
                cleared = true;
                Toast.makeText(getActivity(), "Przywrócono stany charakterystyczne na pobrane z serwera", Toast.LENGTH_SHORT);
            }
        );

        if(mStation.getNotifCheckedId() != -1){
            statesSwitcher.check(mStation.getNotifCheckedId());
        } else {
            statesSwitcher.check(R.id.lw);
        }


        statesSwitcher.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
                mStation.setNotifCheckedId(checkedId);
                    switch (checkedId) {
                        case R.id.lw:
                            mStation.setNotifHint("LW");
                            if (!mStation.isNotifByPrzeplyw()) {
                                if(mStation.getLw_poziom() != -1){
                                    mStation.setDolnaGranicaPoziomu(mStation.getLw_poziom());
                                } else {
                                    Double var = new Double(mStation.getStatus().getLowValue());
                                    mStation.setDolnaGranicaPoziomu(var.intValue());
                                }
                            } else {
                                if(mStation.getLw_przeplyw() != -1) {
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
                                if(mStation.getMw2_poziom() != -1){
                                    mStation.setDolnaGranicaPoziomu(mStation.getMw2_poziom());
                                } else {
                                    Double var = new Double(mStation.getStatus().getHighValue());
                                    mStation.setDolnaGranicaPoziomu(var.intValue());
                                }
                            } else {
                                if(mStation.getMw2_przeplyw() != -1) {
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
                                if(mStation.getHw_poziom() != -1){
                                    mStation.setDolnaGranicaPoziomu(mStation.getHw_poziom());
                                } else {
                                    Double var = new Double(mStation.getStatus().getHighValue());
                                    mStation.setDolnaGranicaPoziomu(var.intValue());
                                }
                            } else {
                                if(mStation.getHw_przeplyw() != -1) {
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

        builder.setView(rootView)
                .setTitle("Edytuj Stacjê")
                .setMessage("Wybierz wg czego chcesz byæ powiadamiany i od jakiego poziomu")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repoStation.createOrUpdate(mStation);
                    }
                })
                .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}
