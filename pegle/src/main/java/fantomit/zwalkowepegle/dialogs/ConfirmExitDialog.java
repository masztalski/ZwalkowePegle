package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

public class ConfirmExitDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("Wyjœcie z aplikacji")
                .setMessage("Czy na pewno chcesz wyjœæ z aplikacji?")
                .setPositiveButton("Tak", (DialogInterface dialog, int which) -> {
                            getActivity().finish();
                        }
                )
                .setNegativeButton("Anuluj", (DialogInterface dialog, int which) -> {
                            dismiss();
                        }
                );
        return alert.create();
    }
}

