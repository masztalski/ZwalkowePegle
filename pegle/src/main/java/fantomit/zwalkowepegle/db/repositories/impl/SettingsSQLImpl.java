package fantomit.zwalkowepegle.db.repositories.impl;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

import javax.inject.Inject;

import fantomit.zwalkowepegle.DBmodels.Settings;
import fantomit.zwalkowepegle.db.repositories.SettingsRepository;

public class SettingsSQLImpl implements SettingsRepository {

    private Dao<Settings, Integer> dao;

    @Inject
    public SettingsSQLImpl(ConnectionSource connSource) {
        try {
            this.dao = DaoManager.createDao(connSource, Settings.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Settings getSettings() {
        try {
            return !dao.queryForAll().isEmpty() ? dao.queryForAll().get(0) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean createOrUpdate(Settings set) {
        try {
            Dao.CreateOrUpdateStatus status = dao.createOrUpdate(set);
            return status.isCreated() || status.isUpdated();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
