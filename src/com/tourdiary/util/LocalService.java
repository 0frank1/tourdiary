package com.tourdiary.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.zh.tourdiary.R;
import com.tourdiary.ui.TraceActivity;
import com.tourdiary.ui.TraceActivity.MyLocationListenner;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;

public class LocalService extends Service {
	//定位对象
	public static LocationClient mLocationClient = null;
	static private ArrayList<LatLng> ptList1=new ArrayList<LatLng>();
	private LatLng pt1;
	private SQLiteDatabase db;
	private CloudAndLocalDatabase dba;
	private SharedPreferences sp1;
	private SharedPreferences sp2;
	private WakeLock mWakeLock;// 电源锁
	private String str1=Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "zh";//轨迹文件根目录
	/**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
             //map view 销毁后不在处理新接收的位置
            if (location == null ) {
                return;
            }
//            pt1=new LatLng(location.getLatitude(), location.getLongitude());
//           ptList1.add(pt1);

        	Location lo=new Location();
        	lo.latitude=location.getLatitude();
        	lo.longitude=location.getLongitude();
            
            String diaryname=sp1.getString("cur_diary_name", "");
            LocalSqliteDatabase dba=new LocalSqliteDatabase(getApplicationContext());
    		dba.open();
            int diary_id=dba.getIdByDiaryName(diaryname);
            
//            String diaryname=dba._diaryName;
//            int diarynum=dba._diaryNameList.indexOf(diaryname);
            
          //打开或创建test.db数据库  
//            db.execSQL("INSERT INTO diary0(_id,Latitude,Longitude) VALUES (NULL, 11.2, 11.2)");
            
            if(diary_id!=-1){
            	//插入数据  
            	CloudAndLocalDatabase db=new CloudAndLocalDatabase(getApplicationContext());
            	db.open();
            	db.insertLocation(lo, diary_id);
            	db.close();
            }
            
        }

		@Override
		public void onConnectHotSpotMessage(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
    }

	private DownLoadBinder downLoadBinder=new DownLoadBinder();
    /**
     * 回调
     */
    private Callback callback;
    /**
     * Timer实时更新数据的
     */
    private Timer mTimer=new Timer();
    /**
     *
     */
     
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        System.out.println("=====onBind=====");
        return downLoadBinder;
    }
 
    /**
     * 内部类继承Binder
     * @author lenovo
     *
     */
    public class DownLoadBinder extends Binder{
        /**
         * 声明方法返回值是MyService本身
         * @return
         */
        public LocalService getService() {
            return LocalService.this;
        }
    }
    /**
     * 服务创建的时候调用
     */
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
//        dbHelper=new DatabaseHelper(getApplicationContext(), "location","diary0");
//        db=dbHelper.getWritableDatabase();
        
        /*
         * 执行Timer 2000毫秒后执行，5000毫秒执行一次
         * 
         */
//        mMapView = (MapView) findViewById(R.id.bmapview);
//      	mMapView.showZoomControls(false);
//      	mBaiduMap=mMapView.getMap();// 获取地图控制
//      	mBaiduMap.setMyLocationEnabled(true);
        acquireWakeLock();
      	mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类    
      	setLocationOption(0,mLocationClient);
      	BDLocationListener myListener = new MyLocationListenner();
      	mLocationClient.registerLocationListener( myListener );    //注册监听函数
      	mLocationClient.start();
        mTimer.schedule(task, 0, 20*1000);
        dba=new CloudAndLocalDatabase(getApplicationContext());
        dba.open();
        sp1 = getSharedPreferences("config",Context.MODE_APPEND);
//        sp2=getSharedPreferences("diaryNum",Context.MODE_APPEND);
    }
    
    /**
     * 设置定位参数
     */
    private void setLocationOption(int span,LocationClient lc) {
    	 LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
         option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
         
         option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
         option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
         option.setOpenGps(true);//可选，默认false,设置是否使用gps
         option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
         option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
         option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
         option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死  
         option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
         option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
         lc.setLocOption(option);
    }
     
    /**
     * 提供接口回调方法
     * @param callback
     */
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
     
    /**
     *
     */
    private TimerTask task = new TimerTask(){
 
        @Override
        public void run() {
            // TODO Auto-generated method stub
//        	mLocationClient.start();
        	mLocationClient.requestLocation();
//        	new Thread(){
//            	@Override
//            	public void run(){
//            	   // Looper.prepare();
//            	   new Handler(Looper.getMainLooper()).post(new Runnable() {
//            	       
//            	        @Override
//            	       public void run() {
//            	           Toast.makeText(getApplicationContext(),"0",Toast.LENGTH_SHORT).show();
//            	       }
//            	   });
//            	}
//            	}.start();
//            if(callback!=null){
//                /*
//                 * 得到最新数据
//                 */
//                callback.getNum(pt1);
//            }
             
        }
         
    };
     
    /**
     * 回调接口
     *
     * @author lenovo
     *
     */
    public static interface Callback {
        /**
         * 得到实时更新的数据
         *
         * @return
         */
        void getNum(LatLng pt1);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	// TODO Auto-generated method stub
    	 return START_STICKY;
    }
    /**
     * 服务销毁的时候调用
     */
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        System.out.println("=========onDestroy======");
        /**
         * 停止Timer
         */
        task.cancel();
        mTimer.cancel();
        dba.close();
//        mMapView.onDestroy();
//		mMapView = null;
        super.onDestroy();
        releaseWakeLock();
    }
    /**
     * onCreate时,申请设备电源锁
     */
	private void acquireWakeLock() {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "myService");
			if (null != mWakeLock) {
				mWakeLock.acquire();
			}
		}
	}

    /**
     * onDestroy时，释放设备电源锁
     */
	private void releaseWakeLock() {
		if (null != mWakeLock) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}
}
