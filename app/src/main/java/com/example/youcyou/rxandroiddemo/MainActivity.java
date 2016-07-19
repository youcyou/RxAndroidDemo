package com.example.youcyou.rxandroiddemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private String tag = "youcyou";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        new Thread(new Runnable() {
            @Override
            public void run() {
                test();
            }
        }).start();

    }

    private void test() {

        Subscriber<String> observer = new Subscriber<String>() {

            @Override
            public void onStart() {
                super.onStart();
                Log.e(tag, "observer onStart thread :" + Thread.currentThread());
            }

            @Override
            public void onNext(String s) {
                Log.e(tag, "observer onNext thread :" + Thread.currentThread());
                Log.e(tag, "Item: " + s);
            }

            @Override
            public void onCompleted() {
                Log.e(tag, "observer onCompleted thread :" + Thread.currentThread());
                Log.e(tag, "Completed!");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(tag, "observer onError thread :" + Thread.currentThread());
                Log.e(tag, "Error!");
            }
        };


        Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.e(tag, "observable thread :" + Thread.currentThread());
                subscriber.onNext("Hello");
                subscriber.onNext("Hi");
                subscriber.onNext("Aloha");
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.newThread())
                ;

        observable.subscribe(observer);
        ;
//        observable.observeOn(Schedulers.immediate()).subscribeOn(Schedulers.newThread()).subscribe(observer);
//
//        Observable observable2 = Observable.just("Hello", "Hi", "Aloha");

//        String[] words = {"Hello", "Hi", "Aloha"};
//        Observable observable3 = Observable.from(words).observeOn(Schedulers.newThread());
//
//        observable3.subscribe(observer);


    }


}
