package fantomit.zwalkowepegle;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import fantomit.zwalkowepegle.di.ConnectionSourceModule;
import fantomit.zwalkowepegle.di.EventBusModule;
import fantomit.zwalkowepegle.di.WebServiceModule;
import io.fabric.sdk.android.Fabric;
import roboguice.RoboGuice;

public class ZwalkiApplication extends Application {

    public static final String API_ENDPOINT = "http://monitor.pogodynka.pl/api";
    public static final String MY_API_SOURCE = "http://wrotka.pwr.wroc.pl";
    //public static final String BUGANALYTICS_KEY = "57a3c03

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        RoboGuice.getOrCreateBaseApplicationInjector
                (this, RoboGuice.DEFAULT_STAGE, RoboGuice.newDefaultRoboModule(this), new WebServiceModule(), new EventBusModule(), new ConnectionSourceModule(this));
    }
}
