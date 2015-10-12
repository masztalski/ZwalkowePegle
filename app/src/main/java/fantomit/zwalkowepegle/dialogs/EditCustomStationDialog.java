package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private Station station;


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
        station = repoStation.findById(idStation);

        notifSwitcher.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                if(checked){
                    notifHint.setText("Powiadomiaj dla progowego poziomu");
                    station.setNotifByPrzeplyw(false);
                } else{
                    notifHint.setText("Powiadomiaj dla progowego przep³ywu");
                    station.setNotifByPrzeplyw(true);
                }
            }
        });

        bClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                station.setIsByDefaultCustomized(false);
                station.setIsUserCustomized(false);
                cleared = true;
                Toast.makeText(getActivity(), "Przywrócono stany charakterystyczne na pobrane z serwera", Toast.LENGTH_SHORT);
            }
        });

        if(station.getNotifCheckedId() != -1){
            statesSwitcher.check(station.getNotifCheckedId());
        } else {
            statesSwitcher.check(R.id.llw);
            station.setNotifCheckedId(R.id.llw);
        }

        statesSwitcher.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                station.setNotifCheckedId(checkedId);
                    switch (checkedId) {
                        case R.id.llw:
                            station.setNotifHint("LLW");
                            if (station.isNotifByPrzeplyw()) {
                                station.setDolnaGranicaPoziomu(station.getLlw_poziom());
                            } else {
                                station.setDolnaGranicaPrzeplywu(station.getLlw_przeplyw());
                            }
                            break;
                        case R.id.lw:
                            station.setNotifHint("LW");
                            if (station.isNotifByPrzeplyw()) {
                                station.setDolnaGranicaPoziomu(station.getLw_poziom());
                            } else {
                                station.setDolnaGranicaPrzeplywu(station.getLw_przeplyw());
                            }
                            break;
                        case R.id.mw1:
                            station.setNotifHint("MW1");
                            if (station.isNotifByPrzeplyw()) {
                                station.setDolnaGranicaPoziomu(station.getMw1_poziom());
                            } else {
                                station.setDolnaGranicaPrzeplywu(station.getMw1_przeplyw());
                            }
                            break;
                        case R.id.mw2:
                            station.setNotifHint("MW2");
                            if (station.isNotifByPrzeplyw()) {
                                station.setDolnaGranicaPoziomu(station.getMw2_poziom());
                            } else {
                                station.setDolnaGranicaPrzeplywu(station.getMw2_przeplyw());
                            }
                            break;
                        case R.id.hw:
                            station.setNotifHint("HW");
                            if (station.isNotifByPrzeplyw()) {
                                station.setDolnaGranicaPoziomu(station.getHw_poziom());
                            } else {
                                station.setDolnaGranicaPrzeplywu(station.getHw_przeplyw());
                            }
                            break;
                        default:
                            station.setNotifHint("LLW");
                            if (station.isNotifByPrzeplyw()) {
                                station.setDolnaGranicaPoziomu(station.getLlw_poziom());
                            } else {
                                station.setDolnaGranicaPrzeplywu(station.getLlw_przeplyw());
                            }
                    }
            }
        });

        builder.setView(rootView)
                .setTitle("Edytuj Stacjê")
                .setMessage("Wybierz wg czego chcesz byæ powiadamiany i od jakiego poziomu")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repoStation.createOrUpdate(station);
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
