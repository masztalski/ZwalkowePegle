package fantomit.zwalkowepegle.db.repositories;

import com.j256.ormlite.dao.ForeignCollection;

import java.util.ArrayList;
import java.util.List;

import fantomit.zwalkowepegle.DBmodels.River;

public interface RiverRepository {
    public List<River> getAll();
    public ArrayList<String> getCorrespondingStationsId(River r);
    public River findById(int id);
    public boolean createOrUpdate(River r);
    public boolean delete(River r);
    public boolean deleteAll();
}
