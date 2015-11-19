package fantomit.zwalkowepegle.webservices;

import java.util.List;

import fantomit.zwalkowepegle.APImodels.MyRecord;
import fantomit.zwalkowepegle.APImodels.WojStation;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface WrotkaWebService {
    @GET("/API/stationHistoryApi.php")
    Observable<List<MyRecord>> getRecords(@Query("id")String id, @Query("kind") String kind, @Query("from") String startDate, @Query("to") String endDate);
    @GET("/API/wojewodztwoApi.php")
    Observable<List<WojStation>> getStations(@Query("woj")String wojewodztwo);
}
