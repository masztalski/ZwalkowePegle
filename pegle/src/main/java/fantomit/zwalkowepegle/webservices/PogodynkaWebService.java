package fantomit.zwalkowepegle.webservices;

import java.util.List;

import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.APImodels.StationListObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PogodynkaWebService {
    @GET("station/hydro/")
    Call<Station> getStacja(@Query("id") String id);

    @GET("map/?category=hydro")
    Call<List<StationListObject>> getListaStacji();
}
