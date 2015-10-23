package fantomit.zwalkowepegle;

import android.app.Application;

import com.buganalytics.trace.BugAnalytics;
import com.splunk.mint.Mint;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import fantomit.zwalkowepegle.di.ConnectionSourceModule;
import fantomit.zwalkowepegle.di.EventBusModule;
import fantomit.zwalkowepegle.di.WebServiceModule;
import roboguice.RoboGuice;

public class ZwalkiApplication extends Application {

    public static final String API_ENDPOINT ="http://monitor.pogodynka.pl/api";
    public static final String APK_SOURCE = "http://wrotka.pwr.wroc.pl/phocadownload/apk";
    public static final String BUGANALYTICS_KEY = "57a3c0364d140831";

    @Override
    public void onCreate() {
        super.onCreate();
        if(!BuildConfig.DEBUG) {
            BugAnalytics.setup(this, BUGANALYTICS_KEY);
            Mint.initAndStartSession(this, "81382ab4");
        }
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        RoboGuice.getOrCreateBaseApplicationInjector
                (this, RoboGuice.DEFAULT_STAGE, RoboGuice.newDefaultRoboModule(this), new WebServiceModule(), new EventBusModule(), new ConnectionSourceModule(this));
    }
}
