package fantomit.zwalkowepegle.di.modules;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fantomit.zwalkowepegle.ZwalkiApplication;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@Singleton
public class NetworkModule {

    @Provides
    @Singleton
    GsonConverterFactory providesGsonFactory() {
        return GsonConverterFactory.create();
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor providesHttpLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    @Provides
    @Singleton
    StethoInterceptor providesStethoInterceptor(){
        return new StethoInterceptor();
    }

    @Provides
    @Singleton
    OkHttpClient providesOKClient(HttpLoggingInterceptor interceptor, StethoInterceptor stethoInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addNetworkInterceptor(stethoInterceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Named("pogodynka")
    @Singleton
    Retrofit providesRestAdapter(OkHttpClient client, GsonConverterFactory gsonConverterFactory) {
        return new Retrofit.Builder()
                .baseUrl(ZwalkiApplication.POGODYNKA_API)
                .addConverterFactory(gsonConverterFactory)
                .client(client)
                .build();
    }

    @Provides
    @Named("wrotka")
    @Singleton
    Retrofit providesRestAdapter1(OkHttpClient client, GsonConverterFactory gsonConverterFactory) {
        return new Retrofit.Builder()
                .baseUrl(ZwalkiApplication.WROTKA_API)
                .addConverterFactory(gsonConverterFactory)
                .client(client)
                .build();
    }
}
