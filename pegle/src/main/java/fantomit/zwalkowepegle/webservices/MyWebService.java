package fantomit.zwalkowepegle.webservices;

import java.util.List;

import fantomit.zwalkowepegle.APImodels.StationListObject;
import retrofit.http.GET;
import rx.Observable;

/**
 * Created by mmar12 on 2015-10-21.
 */
public interface MyWebService {
    @GET("/stacje/get")
    Observable<List<StationListObject>> getListaStacji();
}
