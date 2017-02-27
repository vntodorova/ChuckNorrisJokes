package com.example.venetatodorova.chucknorrisjokes.rest;

import com.example.venetatodorova.chucknorrisjokes.models.Example;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("random?escape=javascript")
    Observable<Example> getJoke();
}
