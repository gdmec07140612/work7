package com.example.asus1.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import java.text.DecimalFormat;
import java.util.logging.LogRecord;

public class GPSActivity extends AppCompatActivity implements Handler.Callback {
    //显示百度地图的视图控件继承于ViewGroup
    MapView mMapView = null;
    // 百度地图控件
    BaiduMap mBaiduMap = null;
    //GPS定位标注点
    private Marker mMarkerGPS;
    //GSM定位标注点
    private Marker mMarkerGSM;
    //百度地图的坐标点
    private LatLng position = null;
    //bitMap的信息
    BitmapDescriptor bd = null;
    //基站经度
    private Double gsmlng = 0.0;
    //基站纬度
    private Double gsmlat = 0.0;
    //ＧＰＳ经度
    private Double gpslng = 0.0;
    //ＧＰＳ纬度
    private Double gpslat = 0.0;
    //Handle
    private Handler locationHandler;
    private Bitmap bmp;
    //GPS
    public final static int Gps_Location = 1;
    //GSM
    public final static int Gsm_Location = 2;
    //GSM菜单
    public final int MENU_GSM = 1;
    //GPS菜单
    public final int MENU_GPS = 2;
    //平均值菜单
    public final int MENU_DIS = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        SDKInitializer.initialize(getApplicationContext());
        bd = BitmapDescriptorFactory.fromResource(R.drawable.speed_port);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        position = new LatLng(23.148059, 113.329632);
        MapStatus mMapStatus = new MapStatus.Builder().target(position).zoom(16).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        MarkerOptions ooGPS = new MarkerOptions().position(position).icon(bd).zIndex(9).draggable(true);
        mMarkerGPS = (Marker) (mBaiduMap.addOverlay(ooGPS));
        MarkerOptions ooGSM = new MarkerOptions().position(position).icon(bd).zIndex(9).draggable(true);
        mMarkerGSM = (Marker) mBaiduMap.addOverlay(ooGSM);
        locationHandler = new Handler(this);

    }

    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        bd.recycle();
    }

    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Gps_Location:
                gpslng = msg.getData().getDouble("longitude");
                gpslat = msg.getData().getDouble("latitude");
                position = new LatLng(gpslat, gpslng);
                Log.d("00385", "" + position.latitude + ":" + position.longitude);
                MapStatus mMapStatusGPS = new MapStatus.Builder().target(position).zoom(18).build();
                MapStatusUpdate mMapStatusUpdateGPS = MapStatusUpdateFactory.newMapStatus(mMapStatusGPS);
                mBaiduMap.animateMapStatus(mMapStatusUpdateGPS);
                mMarkerGPS = null;
                MarkerOptions ooGPS = new MarkerOptions().position(position).icon(bd).zIndex(9).draggable(true);
                mMarkerGPS = (Marker) (mBaiduMap.addOverlay(ooGPS));
                Toast.makeText(this, "GPS定位成功", Toast.LENGTH_SHORT).show();
                break;

            case Gsm_Location:
                gpslng = msg.getData().getDouble("longitude");
                gpslat = msg.getData().getDouble("latitude");
                position = new LatLng(gpslat, gpslng);
                MapStatus mMapStatusGSM = new MapStatus.Builder().target(position).zoom(18).build();
                MapStatusUpdate mMapStatusUpdateGSM = MapStatusUpdateFactory.newMapStatus(mMapStatusGSM);
                mBaiduMap.animateMapStatus(mMapStatusUpdateGSM);
                mMarkerGSM = null;
                MarkerOptions ooGSM = new MarkerOptions().position(position).icon(bd).zIndex(9).draggable(true);
                mMarkerGSM = (Marker) (mBaiduMap.addOverlay(ooGSM));
                Toast.makeText(this, "GPS定位成功", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return false;
    }
    //创建菜单
    public boolean onCreateOptionMenu(Menu menu){
        menu.add(0,MENU_GPS,0,"GPS定位");
        menu.add(0,MENU_GSM,0,"基站定位");
        menu.add(0,MENU_DIS,0,"误差计算");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_GPS) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "请开启GPS", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
                return false;
            }
            gpsLocation(locationManager);
            return true;
        } else if (item.getItemId() == MENU_GSM) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        } else if (item.getItemId() == MENU_DIS) {
            if (gpslng != 0.0 && gsmlng != 0.0) {
                double result = getDistance(gpslat,gpslng,gsmlat,gsmlng);
                DecimalFormat df =new DecimalFormat(",##");
                Toast.makeText(this,"误差为："+df.format(result)+"米",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"请先使用GPS和基站定位后才能计算误差",Toast.LENGTH_SHORT).show();
            }
        }else  if (item.getItemId() == 4){
            position = new LatLng(23.148059, 113.329632);
            MapStatus mMapStatus=new MapStatus.Builder().target(position).zoom(16).build();
            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            mBaiduMap.setMapStatus(mMapStatusUpdate);
        }
        return super.onOptionsItemSelected(item);
    }

    public double getDistance(double lat1,double lon1,double lat2,double lon2) {
        float[] results =new float[1];
        Location.distanceBetween(lat1,lon1,lat2,lon2,results);
        return results[0];
    }

    public void gsmLocation(LocationManager tm){
        Location location = tm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location ==null){
            location = tm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (location != null) {
            Bundle bundle = new Bundle();
            bundle.putDouble("Longitude", location.getLongitude());
            bundle.putDouble("Latitude", location.getLatitude());
            Message gsm_Msg = Message.obtain(locationHandler, Gsm_Location);
            gsm_Msg.setData(bundle);
            gsm_Msg.sendToTarget();
        }else {
            Toast.makeText(GPSActivity.this,"基站获取失败，"+"请确保AGPS，GPS已被打开",Toast.LENGTH_SHORT).show();
        }
    }

    public void gpsLocation(LocationManager locMan) {
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 10,GPS_locationListener);
    }
    private LocationListener GPS_locationListener =new LocationListener(){
        public void onLocationChanged(Location location){
            Bundle bundle = new Bundle();
            bundle.putDouble("Longitude", location.getLongitude());
            bundle.putDouble("Latitude", location.getLatitude());
            Message gps_Msg = Message.obtain(locationHandler, Gps_Location);
            gps_Msg.setData(bundle);
            gps_Msg.sendToTarget();
        }
        @Override
        public void onProviderDisabled(String provider) {

        }
        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };
    protected boolean isRouteDisplayd(){
        return true;
    }

}
