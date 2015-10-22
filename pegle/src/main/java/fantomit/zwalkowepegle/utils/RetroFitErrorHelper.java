package fantomit.zwalkowepegle.utils;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import fantomit.zwalkowepegle.interfaces.MainInterface;
import retrofit.RetrofitError;
import rx.functions.Action1;

public class RetroFitErrorHelper implements Action1<Throwable> {

    MainInterface mView;
    public RetroFitErrorHelper(MainInterface mView){
        this.mView = mView;
    }

    @Override
    public void call(Throwable throwable) {
        if (throwable instanceof RetrofitError) {
            RetrofitError er = (RetrofitError) throwable;
            if (er.getKind() == RetrofitError.Kind.NETWORK) {
                if (mView != null) {
                    mView.displayToast("B³¹d sieci. Spróbuj uruchomiæ aplikacjê ponownie");
                   // mView.hideProgressSpinner();
                }
            }
            if (er.getResponse() != null) {
                try {
                    if (er.getResponse().getBody() != null) {
                        Log.e("RetrofitError", IOUtils.toString(er.getResponse().getBody().in()));
                        if(mView != null) mView.hideProgressSpinner();
                    } else {
                        Log.e("RetrofitError", er.getMessage());
                        if(mView != null) mView.hideProgressSpinner();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("RetrofitError", er.getMessage());
                if(mView != null) mView.hideProgressSpinner();
            }
        } else {
            if(throwable != null && throwable.getMessage() != null) {
                Log.e("RetrofitError", throwable.getMessage());
            } else {
                Log.e("RetrofitError", "brak wiadomoœci");
            }
            if(mView != null) mView.hideProgressSpinner();
        }
    }
}
