package com.pfj.mymap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LocationClient client;
    private TextView tvPosition;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = new LocationClient(getApplicationContext());
        client.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        String[] allPermissons = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
        };
        List<String> permissions = new ArrayList<>();
        for (String p : allPermissons) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(p);
            }
        }
        if (!permissions.isEmpty()) {
            String[] permissoinsArr = permissions.toArray(new String[permissions.size()]);
            //申请权限
            ActivityCompat.requestPermissions(this, permissoinsArr, 1);
        } else {
            init();
        }
    }


    private void init() {
        initUI();
        initLocation();
        client.start();
    }

    private void initUI() {
        tvPosition = findViewById(R.id.tv_position);
        mapView = findViewById(R.id.mapview);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);//开启我的定位
    }


    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);// 5s更新一次
        option.setIsNeedAddress(true);//获取详细地址信息
        //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors); //传感器模式，只能进行GPS定位
        client.setLocOption(option);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


    /**
     * 申请权限回调
     *
     * @param requestCode                   : 请求码
     * @param permissions                   :申请的权限数组
     * @param grantResults:授权结果数组，同意为0否则不为0
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    //遍历判断是否同意授权
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {//拒绝权限申请
                            Toast.makeText(this, "必须同意所有权限才能使用该应用", Toast.LENGTH_SHORT).show();
                            finish();//退出程序
                            return;
                        }
                        init();
                    }
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition
                    .append("纬度：").append(location.getLatitude()).append("\n")
                    .append("经线：").append(location.getLongitude()).append("\n")
                    .append("国家：").append(location.getCountry()).append("\n")
                    .append("省：").append(location.getProvince()).append("\n")
                    .append("市：").append(location.getCity()).append("\n")
                    .append("区：").append(location.getDistrict()).append("\n")
                    .append("街道：").append(location.getStreet()).append("\n")
                    .append("定位方式：");
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络");
            }
            tvPosition.setText(currentPosition);
            if (location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                locateToCurPos(location);

            }

        }
    }

    private void locateToCurPos(BDLocation location) {
        if (isFirstLocate){
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(msu);
            msu = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(msu);
            isFirstLocate = false;
        }
        // 我的位置信息
        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());
        MyLocationData data = builder.build();
        baiduMap.setMyLocationData(data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.stop();//停止定位
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);//关闭我的定位

    }
}

