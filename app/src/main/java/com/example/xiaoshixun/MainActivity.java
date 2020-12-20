package com.example.xiaoshixun;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.ArrayList;
import java.util.List;

import mapapi.overlayutil.WalkingRouteOverlay;

public class MainActivity extends AppCompatActivity {
    private MapView mMapView;
    private BaiduMap baiduMap;
    private LocationClient locationClient;
    //    @BindView(R.id.mapView)
//    MapView mMapView;
//    @BindView(R.id.input)
    EditText input;
    //    @BindView(R.id.btn_search)
    Button btnSearch;
    //    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    //路径规划
//    @BindView(R.id.input_start)
    EditText inputStart;
    //    @BindView(R.id.input_end)
    EditText inputEnd;
    //    @BindView(R.id.btn_routePlan)
    Button btnRoutePlan;
    //    @BindView(R.id.recy_nodes)
    RecyclerView recyNodes;
    PoiSearch poiSearch;
    private List<PoiInfo> poiList;
    private SearchItemAdapter searchItemAdapter;
    private RoutePlanSearch routePlanSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLocationOption();
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.mapView);
        baiduMap = mMapView.getMap();

        quanxian();
        initView();
        //显示卫星图层
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        initLocationOption();
        //初始化检索
        initPoi();
        //初始化路径规划
        initRoutePlan();
    }

    private static final int LOCATION_CODE = 1;
    private LocationManager lm;//【位置管理】

    public void quanxian() {
        lm = (LocationManager) MainActivity.this.getSystemService(MainActivity.this.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//开了定位服务
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("BRG", "没有权限");
                // 没有权限，申请权限。
                // 申请授权。
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
//                        Toast.makeText(getActivity(), "没有权限", Toast.LENGTH_SHORT).show();

            } else {

                // 有权限了，去放肆吧。
//                        Toast.makeText(getActivity(), "有权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("BRG", "系统检测到未开启GPS定位服务");
            Toast.makeText(MainActivity.this, "系统检测到未开启GPS定位服务", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1315);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限被用户同意。

                } else {
                    // 权限被用户拒绝了。
                    Toast.makeText(MainActivity.this, "定位权限被禁止，相关地图功能无法使用！", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    /**
     * 初始化定位参数配置
     */

    private void initLocationOption() {
//定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        LocationClient locationClient = new LocationClient(getApplicationContext());
//声明LocationClient类实例并配置定位参数
        LocationClientOption locationOption = new LocationClientOption();
        MyLocationListener myLocationListener = new MyLocationListener();
//注册监听函数
        locationClient.registerLocationListener(myLocationListener);
//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("gcj02");
//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(1000);
//可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
//可选，设置是否需要地址描述
        locationOption.setIsNeedLocationDescribe(true);
//可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(false);
//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(true);
//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true);
//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true);
//可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
//可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false);
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode();
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000, 1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        locationClient.setLocOption(locationOption);
//开始定位
        locationClient.start();
    }

    private void initView() {
        input = findViewById(R.id.input);
        btnSearch = findViewById(R.id.btn_search);
        inputStart = findViewById(R.id.input_start);
        inputEnd = findViewById(R.id.input_end);
        btnRoutePlan = findViewById(R.id.btn_routePlan);
        recyclerView = findViewById(R.id.recyclerView);
        recyNodes = findViewById(R.id.recy_nodes);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        btnRoutePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchRoute();
            }
        });
    }

    /**
     * 实现定位回调
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            //获取纬度信息
            double latitude = location.getLatitude();
            //获取经度信息
            double longitude = location.getLongitude();
            //获取定位精度，默认值为0.0f
            float radius = location.getRadius();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
            String coorType = location.getCoorType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            int errorCode = location.getLocType();

        }
    }
    /************************检索*********************/
//    PoiSearch poiSearch;

    private void initPoi(){
        poiList = new ArrayList<>();
        searchItemAdapter = new SearchItemAdapter(this,poiList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(searchItemAdapter);

        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);

        searchItemAdapter.addListClick(new BaseAdapter.IListClick() {
            @Override
            public void itemClick(int pos) {
                //点击条目进行定位
                PoiInfo poiInfo = poiList.get(pos);
                MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(poiInfo.location);
                baiduMap.setMapStatus(status);
                drawCircle(poiInfo.location.latitude,poiInfo.location.longitude);
                addMark(poiInfo.location.latitude,poiInfo.location.longitude);
            }
        });
    }

    /**
     * 搜索
     */
    private void search(){
        String word = input.getText().toString();
        if(!TextUtils.isEmpty(word)){
            PoiCitySearchOption option = new PoiCitySearchOption();
            option.city("北京");
            option.keyword(word);
            poiSearch.searchInCity(option);
        }else{

        }
    }

    /**
     * 搜索的监听
     */
    OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            Log.i("TAG","onGetPoiResult");
            poiList.clear();
            if(poiResult.getAllPoi() != null && poiResult.getAllPoi().size() > 0){
                poiList.addAll(poiResult.getAllPoi());
                searchItemAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            Log.i("TAG","onGetPoiDetailResult");
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
            Log.i("TAG","onGetPoiDetailResult");
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            Log.i("TAG","onGetPoiIndoorResult");
        }
    };


    /*******************************路径规划*************************/
    private PlanNode startNode,endNode; //开始和结束的坐标点
    SuggestionSearch suggestionSearch; //地点检索的类
    SuggestAdapter suggestAdapter; //路径规划搜索出来的列表
    List<SuggestionResult.SuggestionInfo> suggestList; //地点检索的结果
    boolean isStart = true; //当前处理的是否是起点
    LatLng startLatLng; //起点的经纬度


    //初始化路径规划
    private void initRoutePlan(){

        suggestionSearch = SuggestionSearch.newInstance();
        suggestList = new ArrayList<>();
        suggestAdapter = new SuggestAdapter(this,suggestList);
        recyNodes.setLayoutManager(new LinearLayoutManager(this));
        recyNodes.setAdapter(suggestAdapter);
        //设置监听地点检索
        suggestionSearch.setOnGetSuggestionResultListener(suggestionResultListener);

        inputStart.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    isStart = true;
                    recyNodes.setVisibility(View.VISIBLE);
                }
            }
        });
        //监听起点输入框的变化
        inputStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //起点输入改变以后获取对应的起点数据
                SuggestionSearchOption option = new SuggestionSearchOption();
                option.city("北京");
                option.citylimit(true);
                option.keyword(s.toString());
                suggestionSearch.requestSuggestion(option);
            }
        });
        //监听终点输入框的光标
        inputEnd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    isStart = false;
                    recyNodes.setVisibility(View.VISIBLE);
                }
            }
        });
        //监听终点输入框的输入
        inputEnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //获取终点框对应的数据
                SuggestionSearchOption option = new SuggestionSearchOption();
                option.city("北京");
                option.citylimit(true);
                option.keyword(s.toString());
                suggestionSearch.requestSuggestion(option);
            }
        });


        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);

        suggestAdapter.addListClick(new BaseAdapter.IListClick() {
            @Override
            public void itemClick(int pos) {
                SuggestionResult.SuggestionInfo suggestionInfo = suggestList.get(pos);
                if(isStart){
                    inputStart.setText(suggestionInfo.getKey());
                    startLatLng = suggestionInfo.getPt();
                }else{
                    inputEnd.setText(suggestionInfo.getKey());
                }
                recyNodes.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 地点检索监听
     */
    OnGetSuggestionResultListener suggestionResultListener = new OnGetSuggestionResultListener() {
        @Override
        public void onGetSuggestionResult(SuggestionResult suggestionResult) {
            //地点检索结果
            if (suggestionResult.getAllSuggestions()!=null){
                suggestList.clear();
                suggestList.addAll(suggestionResult.getAllSuggestions());
                suggestAdapter.notifyDataSetChanged();
            }

        }
    };

    /**
     * 路径搜索的监听
     */
    OnGetRoutePlanResultListener routePlanResultListener = new OnGetRoutePlanResultListener() {
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
            Log.i("TAG","onGetWalkingRouteResult");

            //创建一个路径规划的类
            WalkingRouteOverlay walkingRouteOverlay = new WalkingRouteOverlay(baiduMap);
            //判断当前查找出来的路线
            if(walkingRouteResult.getRouteLines() != null && walkingRouteResult.getRouteLines().size() > 0){
                WalkingRouteLine walkingRouteLine = walkingRouteResult.getRouteLines().get(0);
                walkingRouteOverlay.setData(walkingRouteLine);
                walkingRouteOverlay.addToMap();
                //当前的定位移动到开始点并放大地图
                MapStatusUpdate status = MapStatusUpdateFactory.newLatLngZoom(startLatLng,16);
                baiduMap.setMapStatus(status);
            }else{
                Toast.makeText(MainActivity.this,"未搜索到相关路径",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
            Log.i("TAG","onGetTransitRouteResult");
        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
            Log.i("","onGetMassTransitRouteResult");
        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            Log.i("TAG","onGetDrivingRouteResult");
        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
            Log.i("TAG","onGetIndoorRouteResult");
        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
            Log.i("TAG","onGetBikingRouteResult");
        }
    };

    private void searchRoute(){
        String startName,endName;
        startName = inputStart.getText().toString();
        endName = inputEnd.getText().toString();
        if(TextUtils.isEmpty(startName) || TextUtils.isEmpty(endName)){
            Toast.makeText(this, "请输入正确起点和终点", Toast.LENGTH_SHORT).show();
        }else{
            startNode = PlanNode.withCityNameAndPlaceName("北京",startName);
            endNode = PlanNode.withCityNameAndPlaceName("北京",endName);
            WalkingRoutePlanOption option = new WalkingRoutePlanOption();
            option.from(startNode);
            option.to(endNode);
            //搜索路径
            routePlanSearch.walkingSearch(option);
        }
    }





    /**
     * 以当前的经纬度为圆心绘制一个圆
     * @param lat
     * @param gt
     */
    private void drawCircle(double lat,double gt){
        //设置圆心位置
        LatLng center = new LatLng(lat,gt);
        //设置圆对象
        CircleOptions circleOptions = new CircleOptions().center(center)
                .radius(2000)
                .fillColor(0)
                .stroke(new Stroke(1,0x0000)); //设置边框的宽度和颜色
        baiduMap.clear();
        //在地图上添加显示圆
        Overlay circle = baiduMap.addOverlay(circleOptions);

    }

    private void addMark(double lat,double gt){
        //定义Maker坐标点
        LatLng point = new LatLng(lat, gt);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.mipmap.icon_mark);
        //构建MarkerOption，用于在地图上添加Marker
        MarkerOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        option.animateType(MarkerOptions.MarkerAnimateType.jump);
        baiduMap.addOverlay(option);

    }

        @Override
        protected void onResume () {
            super.onResume();
            //在activity执行onResume时必须调用mMapView. onResume ()
            mMapView.onResume();
        }

        @Override
        protected void onPause () {
            super.onPause();
            //在activity执行onPause时必须调用mMapView. onPause ()
            mMapView.onPause();
        }

        @Override
        protected void onDestroy () {
            super.onDestroy();
            //在activity执行onDestroy时必须调用mMapView.onDestroy()
            mMapView.onDestroy();
        }
    }