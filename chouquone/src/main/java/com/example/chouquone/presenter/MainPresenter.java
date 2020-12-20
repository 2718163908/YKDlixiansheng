package com.example.chouquone.presenter;
import com.example.mylianxi.base.BasePresenter;
import com.example.mylianxi.bean.UserBean;
import com.example.mylianxi.contract.MainContract;
import com.example.mylianxi.model.MainModel;
import com.example.mylianxi.net.Callback;

public class MainPresenter extends BasePresenter<MainContract.IMainView> implements MainContract.IMainPresenter {
    private MainContract.IMainModel mainModel;

    public MainPresenter(MainContract.IMainView mainView) {
        this.mainModel = new MainModel(this);
    }

    @Override
    public void login(String name, String password) {

    }

    @Override
    public void loginResult(String result) {
        mainModel.getLoginData(result, new Callback<UserBean>() {
            @Override
            public void onSuccess(UserBean userBean) {
                iView.getLoginData(userBean);
            }

            @Override
            public void onFail(String msg) {

            }
        });
    }
}
