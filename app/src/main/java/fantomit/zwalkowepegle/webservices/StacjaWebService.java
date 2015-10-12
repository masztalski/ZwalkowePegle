package fantomit.zwalkowepegle.webservices;


import fantomit.zwalkowepegle.APImodels.Station;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface StacjaWebService {
    @GET("/station/hydro/")
    Observable<Station> getStacja(@Query("id") String id);
}
