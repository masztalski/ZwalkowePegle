package fantomit.zwalkowepegle.utils;

import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;

import fantomit.zwalkowepegle.BuildConfig;
import fantomit.zwalkowepegle.interfaces.MainInterface;
import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import rx.functions.Action1;

public class RetroFitErrorHelper implements Action1<Throwable>, ErrorHandler {

    MainInterface mView;

    public RetroFitErrorHelper(MainInterface mView) {
        this.mView = mView;
    }

    @Override
    public void call(Throwable throwable) {
        if (throwable instanceof RetrofitError) {
            RetrofitError er = (RetrofitError) throwable;
            if (er.getKind() == RetrofitError.Kind.NETWORK) {
                if (mView != null) {
                    mView.displayToast("B³¹d sieci. Spróbuj uruchomiæ aplikacjê ponownie");
                    if(!BuildConfig.DEBUG) Answers.getInstance().logCustom(new CustomEvent("RetrofitError")
                            .putCustomAttribute("type", "network error"));
                    mView.hideProgressSpinner();
                    return;
                }
            }
            if (er.getResponse() != null) {
                try {
                    if (er.getResponse().getBody() != null) {
                        Log.e("RetrofitError", IOUtils.toString(er.getResponse().getBody().in()));
                        if(!BuildConfig.DEBUG) Answers.getInstance().logCustom(new CustomEvent("RetrofitError")
                                .putCustomAttribute("type", IOUtils.toString(er.getResponse().getBody().in())));
                        if (mView != null) mView.hideProgressSpinner();
                    } else {
                        Log.e("RetrofitError", er.getMessage());
                        if(!BuildConfig.DEBUG) Answers.getInstance().logCustom(new CustomEvent("RetrofitError")
                                .putCustomAttribute("type", er.getMessage()));
                        if (mView != null) mView.hideProgressSpinner();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("RetrofitError", er.getMessage());
                if(!BuildConfig.DEBUG) Answers.getInstance().logCustom(new CustomEvent("RetrofitError")
                        .putCustomAttribute("type", er.getMessage()));
                if (mView != null) mView.hideProgressSpinner();
            }
        } else {
            if (throwable != null && throwable.getMessage() != null) {
                Log.e("RetrofitError", throwable.getMessage());
                if(!BuildConfig.DEBUG) Answers.getInstance().logCustom(new CustomEvent("RetrofitError")
                        .putCustomAttribute("type", throwable.getMessage()));
            } else {
                Log.e("RetrofitError", "brak wiadomoœci");
                if(!BuildConfig.DEBUG) Answers.getInstance().logCustom(new CustomEvent("RetrofitError")
                        .putCustomAttribute("type", "brak wiadomoœci"));
            }
            if (mView != null) mView.hideProgressSpinner();
        }
    }

    @Override
    public Throwable handleError(RetrofitError cause) {
        mView.displayToast(cause.getMessage());
        return new Throwable(cause.getCause());
    }
}
