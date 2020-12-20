package com.example.chouqutwo.net;

import java.util.HashMap;

public interface WorkInterface {
    public <T> void get(String url, Callback<T> callback);
    public <T> void post(String url, Callback<T> callback);
    public <T> void posts(String url, HashMap<String, String> map, Callback<T> callback);
}