package fantomit.zwalkowepegle.di.modules;

import android.app.AlarmManager;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fantomit.zwalkowepegle.ZwalkiApplication;

@Module
@Singleton
public class ApplicationModule {
    ZwalkiApplication app;

    public ApplicationModule(ZwalkiApplication app) {
        this.app = app;
    }

    @Provides
    @Singleton
    ZwalkiApplication providesApplication() {
        return app;
    }

    @Provides
    @Singleton
    AlarmManager providesAlarmManager() {
        return (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
    }
}
