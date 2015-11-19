package fantomit.zwalkowepegle.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fantomit.zwalkowepegle.services.FavsDownloadService;

public class FavsDownloadReceiver extends BroadcastReceiver {
    public static final String _ACTION = "akcja";

    public FavsDownloadReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, FavsDownloadService.class);
        context.startService(i);
    }
}
