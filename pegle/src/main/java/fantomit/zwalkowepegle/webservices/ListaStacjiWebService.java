package fantomit.zwalkowepegle.webservices;

import com.google.inject.Singleton;

import java.util.List;

import fantomit.zwalkowepegle.APImodels.StationListObject;
import retrofit.http.GET;
import rx.Observable;

public interface ListaStacjiWebService {
    @GET("/map/?category=hydro")
    Observable<List<StationListObject>> getListaStacji();
}
