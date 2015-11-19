package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.EditText;
import android.widget.Toast;

import com.google.inject.Inject;

import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.R;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import roboguice.fragment.RoboDialogFragment;

public class NoteDialog extends RoboDialogFragment {

    private String stationId;
    private String notes;

    private EditText etNotes;

    @Inject
    StationRepository repoStation;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            stationId = getArguments().getString("ID");
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
