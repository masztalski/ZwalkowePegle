package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import javax.inject.Inject;

import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.R;
import fantomit.zwalkowepegle.Statics;
import fantomit.zwalkowepegle.ZwalkiApplication;
import fantomit.zwalkowepegle.db.repositories.StationRepository;

public class NoteDialog extends AppCompatDialogFragment {

    private String stationId;
    private String notes;

    private EditText etNotes;

    @Inject
    StationRepository repoStation;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ZwalkiApplication.getApp().component.inject(this);
        if (getArguments() != null) {
            stationId = getArguments().getString(Statics._STATION_ID);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.note_dialog, null, false);
        etNotes = (EditText) rootView.findViewById(R.id.etNotes);

        loadDataFromDb();

        builder
                .setView(rootView)
                .setPositiveButton("OK", (DialogInterface dialog, int which) -> {
                    notes = etNotes.getText().toString();
                    Station station = repoStation.findById(stationId);
                    if (station != null) {
                        station.setNotes(notes);
                        repoStation.createOrUpdate(station);
                        Toast.makeText(getActivity(), "Uda³o siê utworzyæ notatkê", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Nie uda³o siê utworzyæ notatki", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .setTitle("Notatki")
                .setNegativeButton("Anuluj", (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                });


        return builder.create();
    }

    private void loadDataFromDb() {
        Station station = repoStation.findById(stationId);
        if (station != null && station.getNotes() != null) {
            etNotes.setText(station.getNotes());
        }
    }
}
