package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import fantomit.zwalkowepegle.R;

public class AboutDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("About / Help")
                .setMessage(R.string.about)
                .setPositiveButton("Ok", (DialogInterface dialog, int which) -> {
                            dismiss();
                        }
                );
        return alert.create();
    }
}
