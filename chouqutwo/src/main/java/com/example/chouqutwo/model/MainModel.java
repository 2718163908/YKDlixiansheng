package com.example.chouqutwo.model;
import com.example.mylianxi.contract.MainContract;
import com.example.mylianxi.net.Callback;
import com.example.mylianxi.net.RetrofitUtils;

public class MainModel implements MainContract.IMainModel {
    private MainContract.IMainPresenter presenter;

    public MainModel(MainContract.IMainPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public <T> void getLoginData(String url, Callback<T> callback) {
        RetrofitUtils.getInstance().get(url, callback);
    }
}
