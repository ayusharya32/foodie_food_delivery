package com.pentaware.foodie;

import android.app.Application;
import android.util.Log;

import java.io.IOException;
import java.net.SocketException;

import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class FoodieApplication extends Application {
    private static final String TAG = "FoodieApplicationyy";

    @Override
    public void onCreate() {
        super.onCreate();

        setRxJavaUndeliveredExceptionHandler();
    }

    private void setRxJavaUndeliveredExceptionHandler() {
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if ((e instanceof IOException) || (e instanceof SocketException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                return;
            }

            Log.d(TAG, "Undelivered Exception Received: " + e.toString());
        });
    }
}
