package com.example.youcyou.rxandroiddemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;


public class MainActivity extends AppCompatActivity {

    private String tag = "youcyou";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Observer<String> observer = new Observer<String>() {
            @Override
            public void onNext(String s) {
                Log.e(tag, "Item: " + s);
            }

            @Override
            public void onCompleted() {
                Log.e(tag, "Completed!");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(tag, "Error!");
            }
        };


        Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello");
                subscriber.onNext("Hi");
                subscriber.onNext("Aloha");
                subscriber.onCompleted();
            }
        });

        Observable observable2 = Observable.just("Hello", "Hi", "Aloha");

        String[] words = {"Hello", "Hi", "Aloha"};
        Observable observable3 = Observable.from(words);

        observable3.subscribe(observer);


        String[] names = {"Hello111", "Hi111", "Aloha111"};
        Observable.from(names)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String name) {
                        Log.e(tag, name);
                    }
                });


    }
}
