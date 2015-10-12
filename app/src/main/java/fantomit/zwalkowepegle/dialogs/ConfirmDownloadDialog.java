package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.google.inject.Inject;

import de.greenrobot.event.EventBus;
import fantomit.zwalkowepegle.utils.AktualizacjaEvent;
import roboguice.fragment.RoboDialogFragment;

/**
 * Created by mmar12 on 2015-10-12.
 */
public class ConfirmDownloadDialog extends RoboDialogFragment {
    @Inject
    EventBus eventBus;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("Dostêpna nowa wersja aplikacji")
                .setMessage("Czy chcesz teraz pobraæ i zainstalowaæ aplikacjê?")
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eventBus.post(new AktualizacjaEvent(true));
                        dismiss();
                    }
                })
                .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return alert.create();
    }
}
