package com.example.chouqutwo.net;

public interface Callback<T> {
    void onSuccess(T t);
    void onFail(String msg);
}