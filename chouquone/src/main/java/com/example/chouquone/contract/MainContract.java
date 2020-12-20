package com.example.chouquone.contract;

import com.example.mylianxi.base.BaseView;
import com.example.mylianxi.bean.UserBean;
import com.example.mylianxi.net.Callback;

public class MainContract {
    public interface IMainModel {
        <T> void getLoginData(String url, Callback<T> callback);
    }
    public interface IMainPresenter {
        void login(String name, String password);
        void loginResult(String result);
    }
    public interface IMainView extends BaseView {
        String getUserName();
        String getPassword();
        void getLoginData(UserBean userBean);
    }
}
