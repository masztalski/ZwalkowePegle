package fantomit.zwalkowepegle.webservices;

import fantomit.zwalkowepegle.APImodels.ApkVersion;
import retrofit.http.GET;
import rx.Observable;

/**
 * Created by mmar12 on 2015-10-12.
 */
public interface UpdateWebService {
    @GET("/phocadownload/apk/curVersion.txt")
    Observable<ApkVersion> getCurrentVersion();

}
