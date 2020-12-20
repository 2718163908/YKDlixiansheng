package com.example.chouquone.net;

public interface Callback<T> {
    void onSuccess(T t);
    void onFail(String msg);
}