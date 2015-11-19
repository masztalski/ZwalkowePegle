package fantomit.zwalkowepegle.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class OnApkDownloadComplete extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && context != null) {
            DownloadManager dMgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Bundle extras = intent.getExtras();
            if (extras.containsKey(DownloadManager.EXTRA_DOWNLOAD_ID)) {
                Log.e("DOWNLOAD Completed", "Aplikacja gotowa do instalacji");
                Uri result = dMgr.getUriForDownloadedFile(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
                if (result.toString().contains("Pegle")) {
                    Intent i = new Intent();
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setAction(Intent.ACTION_VIEW);
                    i.setDataAndType(result, "application/vnd.android.package-archive");
                    Log.d("Lofting", "About to install new .apk");
                    context.startActivity(i);
                }
            }
        }
    }
}
