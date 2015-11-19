package fantomit.zwalkowepegle.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.google.inject.Inject;

import de.greenrobot.event.EventBus;
import fantomit.zwalkowepegle.DBmodels.Settings;
import fantomit.zwalkowepegle.R;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.utils.WojewodztwoChoosedEvent;
import roboguice.fragment.RoboDialogFragment;

public class ChooseWojewodztwoDialog extends RoboDialogFragment {
    @Inject
    SettingsRepository repoSettings;

    Settings set;
    private String wojewodztwo = "";
    private int choicedItem = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        set = repoSettings.getSettings();
        if(set != null){
            wojewodztwo = set.getWojewodztwo();
        }

        String[] wojewodztwa = getResources().getStringArray(R.array.provinices_values);
        for(int i = 0; i < wojewodztwa.length; i++){
            if(wojewodztwo.equals(wojewodztwa[i])){
                choicedItem = i;
                break;
            }
        }

        builder.setSingleChoiceItems(R.array.provinices_values, choicedItem, (DialogInterface dialog, int which) -> {
                    wojewodztwo = wojewodztwa[which];}
        );
        builder.setPositiveButton("Ok", (DialogInterface dialog, int which) -> {
                    set.setWojewodztwo(wojewodztwo);
                    set.setHasWojewodztwoChanged(true);
                    repoSettings.createOrUpdate(set);
                    EventBus.getDefault().post(new WojewodztwoChoosedEvent(wojewodztwo));
            }
        );
        return builder.create();
    }
}