package fantomit.zwalkowepegle.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import fantomit.zwalkowepegle.services.UpdateService;

/**
 * Created by mmar12 on 2015-10-12.
 */
public class UpdateReceiver extends BroadcastReceiver {
    public static final String _UPDATE = "update";

    public UpdateReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        Intent i = new Intent(context, UpdateService.class);
        i.putExtra("verCode", extras.getInt("verCode"));
        i.putExtra("verName", extras.getString("verName"));
        context.startService(i);

    }
}
