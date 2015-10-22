package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.google.inject.Inject;

import de.greenrobot.event.EventBus;
import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.utils.UsuwanieRzekiEvent;
import roboguice.fragment.RoboDialogFragment;

public class ConfirmDeleteDialog extends RoboDialogFragment {

    @Inject
    EventBus eventBus;

    private int riverPos = -1;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(getArguments().containsKey("riverPos")){
            riverPos = getArguments().getInt("riverPos", -1);
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("Usuwanie rzeki z bazy danych")
                .setMessage("Czy chcesz usun¹æ rzekê z bazy danych?")
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eventBus.post(new UsuwanieRzekiEvent(true, riverPos));
                    }
                })
                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eventBus.post(new UsuwanieRzekiEvent(false, riverPos));
                    }
                });
        return alert.create();
    }
}
