package fantomit.zwalkowepegle;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseCrashReporting;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import fantomit.zwalkowepegle.di.ConnectionSourceModule;
import fantomit.zwalkowepegle.di.EventBusModule;
import fantomit.zwalkowepegle.di.WebServiceModule;
import roboguice.RoboGuice;

public class ZwalkiApplication extends Application {

    public static final String API_ENDPOINT ="http://monitor.pogodynka.pl/api";
    public static final String APK_SOURCE = "http://wrotka.pwr.wroc.pl/apk";

    @Override
    public void onCreate() {
        super.onCreate();
        ParseCrashReporting.enable(this);
        Parse.initialize(this, "wZJdQMEhzbpktFIBG0lYk8BFPCbCceavDKX2hRNK", "hPySZNm4GmBYZCoRtWGqHcU1a6CMz6liMKCcnGpO");
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        RoboGuice.getOrCreateBaseApplicationInjector
                (this, RoboGuice.DEFAULT_STAGE, RoboGuice.newDefaultRoboModule(this), new WebServiceModule(), new EventBusModule(), new ConnectionSourceModule(this));
    }
}
