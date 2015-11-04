package fantomit.zwalkowepegle.di;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.squareup.okhttp.OkHttpClient;

import fantomit.zwalkowepegle.ZwalkiApplication;
import fantomit.zwalkowepegle.utils.RetroFitErrorHelper;
import fantomit.zwalkowepegle.webservices.ListaStacjiWebService;
import fantomit.zwalkowepegle.webservices.StacjaWebService;
import fantomit.zwalkowepegle.webservices.StationHistoryWebService;
import fantomit.zwalkowepegle.webservices.UpdateWebService;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;

public class WebServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Client.class).toInstance(new OkClient(new OkHttpClient()));
        bind(ListaStacjiWebService.class).toProvider(ListaStacjiWSProvider.class).in(Singleton.class);
        bind(StacjaWebService.class).toProvider(StacjaWSProvider.class).in(Singleton.class);
        bind(UpdateWebService.class).toProvider(UpdateWSProvider.class).in(Singleton.class);
        bind(StationHistoryWebService.class).toProvider(StationHistoryWSProvider.class).in(Singleton.class);
    }

    public static class ListaStacjiWSProvider implements Provider<ListaStacjiWebService> {
        @Inject
        Client client;

        @Override
        public ListaStacjiWebService get() {
            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(ZwalkiApplication.API_ENDPOINT)
                    .setClient(client)
                    .setErrorHandler(new RetroFitErrorHelper(null))
                    .setLogLevel(RestAdapter.LogLevel.FULL);
            RestAdapter restAdapter = builder.build();
            return restAdapter.create(ListaStacjiWebService.class);
        }

    }

    public static class StacjaWSProvider implements Provider<StacjaWebService> {
        @Inject
        Client client;

        @Override
        public StacjaWebService get() {
            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(ZwalkiApplication.API_ENDPOINT)
                    .setClient(client)
                    .setErrorHandler(new RetroFitErrorHelper(null))
                    .setLogLevel(RestAdapter.LogLevel.FULL);
            RestAdapter restAdapter = builder.build();
            return restAdapter.create(StacjaWebService.class);
        }
    }

    public static class UpdateWSProvider implements Provider<UpdateWebService> {
        @Inject
        Client client;

        @Override
        public UpdateWebService get() {
            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(ZwalkiApplication.MY_API_SOURCE)
                    .setClient(client)
                    .setErrorHandler(new RetroFitErrorHelper(null))
                    .setLogLevel(RestAdapter.LogLevel.FULL);
            RestAdapter restAdapter = builder.build();
            return restAdapter.create(UpdateWebService.class);
        }
    }

    public static class StationHistoryWSProvider implements Provider<StationHistoryWebService> {
        @Inject
        Client client;

        @Override
        public StationHistoryWebService get() {
            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(ZwalkiApplication.MY_API_SOURCE)
                    .setClient(client)
                    .setErrorHandler(new RetroFitErrorHelper(null))
                    .setLogLevel(RestAdapter.LogLevel.FULL);
            RestAdapter restAdapter = builder.build();
            return restAdapter.create(StationHistoryWebService.class);
        }
    }
}
