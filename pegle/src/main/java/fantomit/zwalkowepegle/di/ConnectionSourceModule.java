package fantomit.zwalkowepegle.di;

import android.app.Application;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import fantomit.zwalkowepegle.db.DatabaseHelper;
import fantomit.zwalkowepegle.db.repositories.RiverRepository;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.db.repositories.impl.RiverSQLImpl;
import fantomit.zwalkowepegle.db.repositories.impl.SettingsSQLImpl;
import fantomit.zwalkowepegle.db.repositories.impl.StationSQLImpl;

public class ConnectionSourceModule extends AbstractModule implements Provider<ConnectionSource> {
    private Application app;

    public ConnectionSourceModule(Application app) {
        this.app = app;
    }

    @Override
    protected void configure() {
        bind(ConnectionSource.class).toProvider(this).in(Singleton.class);
        bind(RiverRepository.class).to(RiverSQLImpl.class);
        bind(StationRepository.class).to(StationSQLImpl.class);
        bind(SettingsRepository.class).to(SettingsSQLImpl.class);
    }

    @Override
    public ConnectionSource get() {
        return new AndroidConnectionSource(new DatabaseHelper(app));
    }
}
