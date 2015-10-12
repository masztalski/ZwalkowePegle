package fantomit.zwalkowepegle.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.squareup.okhttp.OkHttpClient;

import fantomit.zwalkowepegle.APImodels.ApkVersion;
import fantomit.zwalkowepegle.ZwalkiApplication;
import fantomit.zwalkowepegle.services.UpdateService;
import fantomit.zwalkowepegle.webservices.ListaStacjiWebService;
import fantomit.zwalkowepegle.webservices.StacjaWebService;
import fantomit.zwalkowepegle.webservices.UpdateWebService;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class WebServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ListaStacjiWebService.class).toProvider(ListaStacjiWSProvider.class).in(Singleton.class);
        bind(StacjaWebService.class).toProvider(StacjaWSProvider.class).in(Singleton.class);
        bind(UpdateWebService.class).toProvider(UpdateWSProvider.class).in(Singleton.class);
    }

    public static class ListaStacjiWSProvider implements Provider<ListaStacjiWebService> {

        @Override
        public ListaStacjiWebService get() {
            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(ZwalkiApplication.API_ENDPOINT)
                    .setClient(new OkClient(new OkHttpClient()))
                    .setLogLevel(RestAdapter.LogLevel.FULL);
            RestAdapter restAdapter = builder.build();
            return restAdapter.create(ListaStacjiWebService.class);
        }

    }

    public static class StacjaWSProvider implements Provider<StacjaWebService> {
        @Override
        public StacjaWebService get() {
            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(ZwalkiApplication.API_ENDPOINT)
                    .setClient(new OkClient(new OkHttpClient()))
                    .setLogLevel(RestAdapter.LogLevel.FULL);
            RestAdapter restAdapter = builder.build();
            return restAdapter.create(StacjaWebService.class);
        }
    }

    public static class UpdateWSProvider implements Provider<UpdateWebService> {
        @Override
        public UpdateWebService get() {
            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(ZwalkiApplication.APK_SOURCE)
                    .setClient(new OkClient(new OkHttpClient()))
                    .setLogLevel(RestAdapter.LogLevel.FULL);
            RestAdapter restAdapter = builder.build();
            return restAdapter.create(UpdateWebService.class);
        }
    }

}
