package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;


public class AppRaterDialog extends AppCompatDialogFragment {
    private final static String APP_TITLE = "Zwa³kowe Pegle";
    private final static String APP_PNAME = "fantomit.zwalkowepegle";

    private SharedPreferences.Editor editor;

    public void setEditor(SharedPreferences.Editor editor){
        this.editor = editor;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle("Oceñ " + APP_TITLE)
                .setMessage("Jeœli lubisz " + APP_TITLE + ", proszê poœwiêæ chwilê na ich ocenê. Dziêki za wsparcie!")
                .setPositiveButton("Oceñ " + APP_TITLE,(DialogInterface dialog, int which)  -> {
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Nie, dziêki", (DialogInterface dialog, int which) -> {
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();
                    }
                    dialog.dismiss();
                })
                .setNeutralButton("Przypomnij póŸniej", (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                });
        return builder.create();
    }
}
