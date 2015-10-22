package fantomit.zwalkowepegle.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.squareup.okhttp.OkHttpClient;

import fantomit.zwalkowepegle.ZwalkiApplication;
import fantomit.zwalkowepegle.webservices.ListaStacjiWebService;
import fantomit.zwalkowepegle.webservices.MyWebService;
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
        bind(MyWebService.class).toProvider(MyWSProvider.class).in(Singleton.class);
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

    public static class MyWSProvider implements Provider<MyWebService> {
        @Override
        public MyWebService get() {
            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint("http://10.253.10.98:8080")
                    .setClient(new OkClient(new OkHttpClient()))
                    .setLogLevel(RestAdapter.LogLevel.FULL);
            RestAdapter restAdapter = builder.build();
            return restAdapter.create(MyWebService.class);
        }
    }

}
