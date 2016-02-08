package fantomit.zwalkowepegle.di.modules;


import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fantomit.zwalkowepegle.webservices.PogodynkaWebService;
import fantomit.zwalkowepegle.webservices.WrotkaWebService;
import retrofit2.Retrofit;

@Module
@Singleton
public class WebServiceModule {
    @Singleton
    @Provides
    PogodynkaWebService provideListaStacjiWS(@Named("pogodynka") Retrofit retrofit) {
        return retrofit.create(PogodynkaWebService.class);
    }

    @Singleton
    @Provides
    WrotkaWebService provideWrotkaWS(@Named("wrotka") Retrofit retrofit) {
        return retrofit.create(WrotkaWebService.class);
    }
}
