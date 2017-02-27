package com.example.venetatodorova.chucknorrisjokes.services;
import android.util.Log;
import com.example.venetatodorova.chucknorrisjokes.views.CountdownView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ApiCaller {

    private static final String API_URL = "http://api.icndb.com/jokes/random?escape=javascript";
    private CountdownView countdown;
    private Disposable disposable;
    private Consumer<String> consumer;

    public ApiCaller(CountdownView countdown, Consumer<String> consumer) {
        this.countdown = countdown;
        this.consumer = consumer;
    }

    public void startDownload() {
        disposable = Observable.interval(6, TimeUnit.SECONDS)
                .flatMap(ignored -> Observable.fromCallable(this::getRandomJoke))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(consumer, Throwable::printStackTrace);
    }

    private void startTimer() {
        Random random = new Random();
        int timer = random.nextInt(10) + 1;
        countdown.start(timer);
    }

    private String getRandomJoke() {
        String joke = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(API_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getInputStream())));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            JSONObject jsonObj = new JSONObject(stringBuilder.toString());
            JSONObject value = jsonObj.getJSONObject("value");
            joke = value.getString("joke");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        Log.i("ApiCaller", "joke downloaded");
        return joke;
    }

    public void stopTask() {
        disposable.dispose();
    }
}
