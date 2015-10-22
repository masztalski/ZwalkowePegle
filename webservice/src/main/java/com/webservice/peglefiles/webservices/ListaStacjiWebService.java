package com.webservice.peglefiles.webservices;

import com.models.StationListObject;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Headers;
import rx.Observable;

public interface ListaStacjiWebService {
    public static final String HEADER_USER_AGENT = "User-Agent: Pegle-WebService";

    @Headers(HEADER_USER_AGENT)
    @GET("/map/?category=hydro")
    Observable<List<StationListObject>> getListaStacji();
}
