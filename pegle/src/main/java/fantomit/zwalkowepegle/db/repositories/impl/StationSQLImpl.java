package fantomit.zwalkowepegle.db.repositories.impl;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.db.repositories.StationRepository;

public class StationSQLImpl implements StationRepository {

    private Dao<Station, String> dao;

    @Inject
    public StationSQLImpl(ConnectionSource connSource) {
        try {
            this.dao = DaoManager.createDao(connSource, Station.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Station> getAll() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Station findById(String id) {
        try {
            return id != null ? dao.queryForId(id) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean createOrUpdate(Station s) {
        try {
            Dao.CreateOrUpdateStatus status = dao.createOrUpdate(s);
            return status.isCreated() || status.isUpdated();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(Station s) {
        try {
            int result = dao.delete(s);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteAll() {
        try {
            int result = dao.delete(dao.queryForAll());
            return result > 0;
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
