package fantomit.zwalkowepegle.db.repositories;

import java.util.List;

import fantomit.zwalkowepegle.APImodels.Station;

public interface StationRepository {
    List<Station> getAll();

    Station findById(String id);

    boolean createOrUpdate(Station s);

    boolean delete(Station s);

    boolean deleteAll();

}
