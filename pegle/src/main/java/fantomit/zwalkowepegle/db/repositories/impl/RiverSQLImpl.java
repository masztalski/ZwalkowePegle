package fantomit.zwalkowepegle.db.repositories.impl;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.db.repositories.RiverRepository;

public class RiverSQLImpl implements RiverRepository {

    private ConnectionSource connSource;
    private RiverRepository rivers;
    private Dao<River, Integer> dao;

    @Inject
    public RiverSQLImpl(ConnectionSource connSource, RiverRepository rivers) throws SQLException {
        this.connSource = connSource;
        this.rivers = rivers;
        this.dao = DaoManager.createDao(connSource, River.class);
    }

    @Override
    public List<River> getAll() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<String> getCorrespondingStationsId(River r) {
        try {
            return dao.queryForMatching(r).get(0).getConnectedStations();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean createOrUpdate(River r) {
        try {
            Dao.CreateOrUpdateStatus status = dao.createOrUpdate(r);
            return status.isCreated() || status.isUpdated();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public River findById(int id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean delete(River r) {
        try {
            int result = dao.delete(r);
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
