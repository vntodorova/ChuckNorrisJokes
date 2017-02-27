package com.example.venetatodorova.chucknorrisjokes.services;

import android.util.Log;

import com.example.venetatodorova.chucknorrisjokes.activities.MainActivity;
import com.example.venetatodorova.chucknorrisjokes.models.Example;
import com.example.venetatodorova.chucknorrisjokes.rest.ApiClient;
import com.example.venetatodorova.chucknorrisjokes.rest.ApiInterface;
import com.example.venetatodorova.chucknorrisjokes.views.CountdownView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ApiCaller {
    private CountdownView countdown;
    private Disposable disposable;
    private Function<Example, Observable<Long>> function;

    public ApiCaller(CountdownView countdown, Function<Example, Observable<Long>> function) {
        this.countdown = countdown;
        this.function = function;
    }

    public void startDownload(int randomTime) {
        if (MainActivity.isDatabaseFull()) {
            return;
        }
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        disposable = Observable.interval(0, randomTime, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .flatMap(ignored -> apiService.getJoke())
                .doOnNext(aLong -> countdown.start(randomTime))
                .flatMap(function)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> Log.i("onNext", String.valueOf(res)), Throwable::printStackTrace);
    }

    public void stopTask() {
        disposable.dispose();
    }
}
