package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import javax.inject.Inject;

import fantomit.zwalkowepegle.Statics;
import fantomit.zwalkowepegle.ZwalkiApplication;
import fantomit.zwalkowepegle.events.UsuwanieRzekiEvent;

public class ConfirmDeleteDialog extends AppCompatDialogFragment {

    @Inject
    EventBus eventBus;

    private ArrayList<Integer> riverPos = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ZwalkiApplication.getApp().component.inject(this);
        if (getArguments().containsKey(Statics._RIVER_POS)) {
            riverPos = getArguments().getIntegerArrayList(Statics._RIVER_POS);
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("Usuwanie rzeki z bazy danych")
                .setMessage("Czy chcesz usun¹æ rzekê z bazy danych?")
                .setPositiveButton("Tak", (DialogInterface dialog, int which) -> {
                            eventBus.post(new UsuwanieRzekiEvent(true, riverPos));
                        }
                )
                .setNegativeButton("Nie", (DialogInterface dialog, int which) -> {
                            eventBus.post(new UsuwanieRzekiEvent(false, riverPos));
                        }
                );
        return alert.create();
    }
}
