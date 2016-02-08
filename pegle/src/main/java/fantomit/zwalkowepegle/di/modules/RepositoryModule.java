package fantomit.zwalkowepegle.di.modules;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fantomit.zwalkowepegle.ZwalkiApplication;
import fantomit.zwalkowepegle.db.DatabaseHelper;
import fantomit.zwalkowepegle.db.repositories.RiverRepository;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;
import fantomit.zwalkowepegle.db.repositories.StationRepository;
import fantomit.zwalkowepegle.db.repositories.impl.RiverSQLImpl;
import fantomit.zwalkowepegle.db.repositories.impl.SettingsSQLImpl;
import fantomit.zwalkowepegle.db.repositories.impl.StationSQLImpl;

@Module
@Singleton
public class RepositoryModule {

    @Provides
    @Singleton
    ConnectionSource providesConnectionSource(ZwalkiApplication app) {
        return new AndroidConnectionSource(new DatabaseHelper(app));
    }

    @Provides
    @Singleton
    RiverRepository providesRiverRepository(ConnectionSource connectionSource) {
        return new RiverSQLImpl(connectionSource);
    }

    @Provides
    @Singleton
    SettingsRepository providesSettingsRepository(ConnectionSource connectionSource) {
        return new SettingsSQLImpl(connectionSource);
    }

    @Provides
    @Singleton
    StationRepository providesStationRepository(ConnectionSource connectionSource) {
        return new StationSQLImpl(connectionSource);
    }
}
