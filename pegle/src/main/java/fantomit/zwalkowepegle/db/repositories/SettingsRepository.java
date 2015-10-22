package fantomit.zwalkowepegle.db.repositories;

import fantomit.zwalkowepegle.DBmodels.Settings;

public interface SettingsRepository {
    Settings getSettings();
    boolean createOrUpdate(Settings set);
}
