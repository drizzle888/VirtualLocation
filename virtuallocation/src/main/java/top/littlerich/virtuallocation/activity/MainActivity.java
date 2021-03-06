package top.littlerich.virtuallocation.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;

import top.littlerich.virtuallocation.R;
import top.littlerich.virtuallocation.base.BaseActivity;
import top.littlerich.virtuallocation.common.AppApplication;
import top.littlerich.virtuallocation.listener.AsyncLocationResultListener;
import top.littlerich.virtuallocation.listener.GeoCoderListener;
import top.littlerich.virtuallocation.listener.MapClickListener;
import top.littlerich.virtuallocation.listener.MarkerDragListener;
import top.littlerich.virtuallocation.util.LocationUtil;
import top.littlerich.virtuallocation.view.TopBanner;

import static top.littlerich.virtuallocation.util.LocationUtil.hasAddTestProvider;

/**
 * Created by xuqingfu on 2017/4/15.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener{

    private String mMockProviderName = LocationManager.GPS_PROVIDER;
    private Button bt_Ok;
    private LocationManager locationManager;
    public static double latitude = 25.2358842413, longitude = 119.2035484314;

    private Thread thread;// ??????????????????????????????
    private Boolean RUN = true;
    private TextView tv_location;
    boolean isFirstLoc = true;// ??????????????????
    // ??????????????????
    private LocationClient mLocClient;
    private MyLocationConfiguration.LocationMode mCurrentMode;// ????????????
    private BitmapDescriptor mCurrentMarker;// ????????????
    private MapView mMapView;
    private static BaiduMap mBaiduMap;
    // ??????????????? bitmap ???????????????????????? recycle
    private BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.mipmap.icon_gcoding);
    private static Marker mMarker;
    private static LatLng curLatlng;
    private static GeoCoder mSearch;
    public static double myGpslatitude, myGpslongitude;
    DrawerLayout mDrawerLayout;
    TopBanner mTopbanner;
    private TextView mAboutAuthor;
    private ImageView mCurrentLocation;
    private Animation mOperatingAnim;
    private TextView mPreciseLocation;
    private TextView mAddProcess;
    private ImageView mStopMock;

    @Override
    protected Object getContentViewId() {
        return R.layout.layout_schema;
    }

    @Override
    protected void IniView() {
        bt_Ok = (Button) findViewById(R.id.bt_Ok);
        tv_location = (TextView) findViewById(R.id.tv_location);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_left);
        mTopbanner = (TopBanner) findViewById(R.id.topbanner);
        mAboutAuthor = (TextView) findViewById(R.id.tv_about_me);
        mCurrentLocation = (ImageView)findViewById(R.id.iv_location);
        mStopMock = (ImageView)findViewById(R.id.iv_stop_location);
        mPreciseLocation = (TextView)findViewById(R.id.tv_precise);
        mAddProcess = (TextView) findViewById(R.id.tv_add_app);
        //??????????????????
        mOperatingAnim = AnimationUtils.loadAnimation(this, R.anim.spinloaing);
        LinearInterpolator lin = new LinearInterpolator();

        mOperatingAnim.setInterpolator(lin);
        // ???????????????
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // ??????????????????
        mBaiduMap.setMyLocationEnabled(true);
        //?????????????????????
        mMapView.showScaleControl(false);
        //????????????????????????
        mMapView.showZoomControls(false);
        mMapView.removeViewAt(1);
        // ???????????????
        mLocClient = new LocationClient(this);
    }

    @Override
    protected void IniLister() {
        bt_Ok.setOnClickListener(this);
        mLocClient.registerLocationListener(new AsyncLocationResultListener(mMapView, isFirstLoc));
        mBaiduMap.setOnMapClickListener(new MapClickListener(bt_Ok));
        mBaiduMap.setOnMarkerDragListener(new MarkerDragListener());

        // ??????????????????????????????????????????
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(new GeoCoderListener(MainActivity.this, tv_location));
        mTopbanner.setTopBannerListener(new TopBanner.OnTopBannerListener() {
            @Override
            public void leftClick(View v) {
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
            }

            @Override
            public void rightClick(View v) {

            }
        });
        mAboutAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.openActivity(MainActivity.this);
            }
        });
        mCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng ll = new LatLng(myGpslatitude, myGpslongitude);
                setCurrentMapLatLng(ll);
            }
        });
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                new Handler().postDelayed(new Runnable(){

                    public void run() {
                        mCurrentLocation.clearAnimation();
                    }

                }, 1000 * 6);
            }
        });
        mPreciseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreciseLocationActivity.openActivity(MainActivity.this);
            }
        });
        mAddProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppsActivity.openActivity(MainActivity.this);
            }
        });
        mStopMock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationUtil.stopMockLocation();
                Toast.makeText(MainActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                bt_Ok.setText("????????????");
            }
        });

    }

    @Override
    protected void IniData() {
        inilocation();
        iniMap();

        if (mOperatingAnim != null) {
            mCurrentLocation.startAnimation(mOperatingAnim);
        }

    }

    /**
     * inilocation ????????? ????????????
     */
    private void inilocation() {
        try {
            if (LocationUtil.initLocation(MainActivity.this)) {
                LocationUtil.initLocationManager();
            } else {//?????????ADB????????????????????????????????????HOOK??????

            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "???????????????????????????!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * iniMap ???????????????
     */
    private void iniMap() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// ??????gps
        option.setCoorType("bd09ll"); // ??????????????????
        option.setScanSpan(3000);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;

        // ??????
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
        mBaiduMap.setMapStatus(msu);

        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
        mLocClient.setLocOption(option);
        mLocClient.start();
        initOverlay();

        // ???????????????????????????GPS??????
        LocationUtil.startLocaton();
    }

    /**
     * initOverlay ????????????????????????????????????????????????
     */
    private void initOverlay() {
        LatLng ll = new LatLng(AppApplication.mMockGps.mLatitude, AppApplication.mMockGps.mLongitude);
        OverlayOptions oo = new MarkerOptions().position(ll).icon(bd).zIndex(9)
                .draggable(true);
        mMarker = (Marker) (mBaiduMap.addOverlay(oo));
    }

    /**
     * setCurrentMapLatLng ??????????????????
     */
    public static void setCurrentMapLatLng(LatLng arg0) {
        curLatlng = arg0;
        mMarker.setPosition(arg0);

        // ????????????????????????????????????
        LatLng ll = new LatLng(arg0.latitude, arg0.longitude);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        mBaiduMap.animateMapStatus(u);

        // ????????????????????? ?????????????????????????????????onGetReverseGeoCodeResult???????????????
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(arg0));
    }

    @Override
    protected void thisFinish() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle("??????");
        build.setMessage("????????????????????????????????????????????????????????????");
        build.setPositiveButton("??????", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        build.setNeutralButton("?????????", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
            }
        });
        build.setNegativeButton("??????", null);
        build.show();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        thisFinish();
    }

    @Override
    protected void onDestroy() {
        RUN = false;
        thread = null;
        //????????????????????????
        mCurrentLocation.clearAnimation();

        // ?????????????????????
        mLocClient.stop();
        // ??????????????????
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        bd.recycle();
        mSearch.destroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_Ok:
                latitude = curLatlng.latitude;
                longitude = curLatlng.longitude;
                try {
                    if (!hasAddTestProvider){
                        LocationUtil.initLocation(MainActivity.this);
                        LocationUtil.initLocationManager();
                    }
                    LocationUtil.setLongitudeAndLatitude(curLatlng.longitude, curLatlng.latitude);
                    AppApplication.mMockGps.mLatitude = curLatlng.latitude;
                    AppApplication.mMockGps.mLongitude = curLatlng.longitude;
                    bt_Ok.setText("????????????");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}