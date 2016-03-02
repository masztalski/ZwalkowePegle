package fantomit.zwalkowepegle.webservices;

import java.util.List;

import fantomit.zwalkowepegle.APImodels.GCM;
import fantomit.zwalkowepegle.APImodels.MyRecord;
import fantomit.zwalkowepegle.APImodels.WojStation;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface WrotkaWebService {
    @GET("API/stationHistoryApi.php")
    Call<List<MyRecord>> getRecords(@Query("id") String id, @Query("kind") String kind, @Query("from") String startDate, @Query("to") String endDate);

    @GET("API/wojewodztwoApi.php")
    Call<List<WojStation>> getStations(@Query("woj") String wojewodztwo);

    @GET("phocadownload/stany.peg")
    Call<ResponseBody> getStany();

    @FormUrlEncoded
    @POST("API/gcm.php")
    Call<ResponseBody> storeRegId(@Field("reg_id") String reg_id);
}
