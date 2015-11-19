package fantomit.zwalkowepegle.services;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.inject.Inject;

import java.net.SocketTimeoutException;

import de.greenrobot.event.EventBus;
import fantomit.zwalkowepegle.APImodels.ApkVersion;
import fantomit.zwalkowepegle.ZwalkiApplication;
import fantomit.zwalkowepegle.utils.AktualizacjaEvent;
import fantomit.zwalkowepegle.utils.RetroFitErrorHelper;
import fantomit.zwalkowepegle.webservices.UpdateWebService;
import roboguice.service.RoboService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by mmar12 on 2015-10-12.
 */
public class UpdateService extends RoboService {

    double verName = -1;
    int verCode = -1;
    String fileName;

    ApkVersion newVersion;

    private DownloadManager mDownloader;
    @Inject
    UpdateWebService updateWS;
    @Inject
    EventBus eventBus;

    @Override
    public void onCreate() {
        super.onCreate();
        mDownloader = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            verName = Double.parseDouble(extras.getString("verName"));
            verCode = extras.getInt("verCode");
            Log.e("UpdateService", "Uruchomiony");

            Observable<ApkVersion> result = updateWS.getCurrentVersion();

            result.observeOn(AndroidSchedulers.mainThread()).subscribe(apkVersion -> {
                newVersion = apkVersion;
                if (apkVersion.getVerName() > verName) {
                    Log.e("UpdateService", "nowa wersja");
                    eventBus.post(new AktualizacjaEvent(false));
                } else if (apkVersion.getVerName() == verName && apkVersion.getVerCode() > verCode) {
                    eventBus.post(new AktualizacjaEvent(false));
                    Log.e("UpdateService", "nowa wersja");
                }
            }, new RetroFitErrorHelper(null));

        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void onEvent(AktualizacjaEvent event) {
        Log.e("EVENT", "event aktualizacji w UpdateService");
        if (event.czyPobrac()) {
            downloadAPK();
        }
    }

    private void downloadAPK() {
        Log.e("UpdateService", "Pobieranie APK");
        fileName = "Pegle-" + Double.toString(newVersion.getVerName()) + "B" + Integer.toString(newVersion.getVerCode()) + "-release.apk";
        String URL = ZwalkiApplication.MY_API_SOURCE + "/phocadownload/apk/" + fileName;
        String ext = MimeTypeMap.getFileExtensionFromUrl(URL);
        MimeTypeMap mimeMap = MimeTypeMap.getSingleton();
        String mime = mimeMap.getMimeTypeFromExtension(ext);

        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(URL));
        req.setTitle("Pegle - " + fileName);
        req.setMimeType(mime);
        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        //req.setAllowedOverMetered(true);
        req.setAllowedOverRoaming(true);
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        req.setVisibleInDownloadsUi(true);

        mDownloader.enqueue(req);
    }
}
