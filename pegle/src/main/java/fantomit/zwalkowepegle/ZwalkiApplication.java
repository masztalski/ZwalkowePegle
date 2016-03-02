package fantomit.zwalkowepegle;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import fantomit.zwalkowepegle.di.components.ApplicationComponent;
import fantomit.zwalkowepegle.di.components.DaggerApplicationComponent;
import fantomit.zwalkowepegle.di.modules.ApplicationModule;
import fantomit.zwalkowepegle.di.modules.NetworkModule;
import fantomit.zwalkowepegle.di.modules.WebServiceModule;
import io.fabric.sdk.android.Fabric;

public class ZwalkiApplication extends Application {
    public static final String POGODYNKA_API = "http://monitor.pogodynka.pl/api/";
    public static final String WROTKA_API = "http://wrotka.pwr.edu.pl/";

    public static ApplicationComponent component;
    public static ZwalkiApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .networkModule(new NetworkModule())
                .webServiceModule(new WebServiceModule())
                .build();

        instance = this;

        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }

    public static ZwalkiApplication getApp() {
        return instance;
    }
}
