package com.tourdiary.ui;

import java.io.File;
import java.util.ArrayList;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.model.LatLng;
import com.tourdiary.util.CloudAndLocalDatabase;
import com.tourdiary.util.Database;
import com.tourdiary.util.LocalService;
import com.tourdiary.util.LocalSqliteDatabase;
import com.tourdiary.util.Location;
import com.tourdiary.util.LocalService.DownLoadBinder;
import com.zh.tourdiary.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ShareTraceActivity extends Activity {
	
	int diary_id;
	// 百度地图控件
		private static MapView mMapView = null;
		// 百度地图对象
		private static BaiduMap mBaiduMap;
		//定位对象
		public LocationClient mLocationClient = null;
		//定位的坐标集合
//		private ArrayList<LatLng> ptList=new ArrayList<LatLng>();
		//定位的坐标点
		private LatLng pt1;
		//按钮
		private Button bt1;
		
//		private View v;
//		private ImageView im_photo;
		//设备锁
		private WakeLock wakeLock1 = null;
		private LocalService.DownLoadBinder downLoadBinder;
		//线程间隔时间
		private  int locationTime=10*1000; 
		
		private int location_id;
		private Handler handler;
		private Runnable runnable=new Runnable() {
			
			@Override
			public void run() {
				mLocationClient.requestLocation();
				handler.postDelayed(this, locationTime);
			}
		};
		 
	   /**
	     * 定位SDK监听函数
	     */
	    public class MyLocationListenner implements BDLocationListener {

	        private boolean isFirstLoc = true;//是否第一次定位

	        @Override
	        public void onReceiveLocation(BDLocation location) {
	              //map view 销毁后不在处理新接收的位置
	            if (location == null || mMapView == null) {
	                return;
	            }
	            
	            //获取定位信息        
	            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
	           // 此处设置开发者获取到的方向信息，顺时针0-360
	                    .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
	           //定位
	            mBaiduMap.setMyLocationData(locData);
//	            pt1=new LatLng(location.getLatitude(), location.getLongitude());
//	            ptList.add(pt1);
	            if (isFirstLoc) {
	                isFirstLoc = false;
	                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
	                MapStatus.Builder builder = new MapStatus.Builder();
	                builder.target(ll).zoom(16.0f);
	                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
	            }
	        }

			@Override
			public void onConnectHotSpotMessage(String arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
	    }

	    
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			//启动设备锁
//			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//			wakeLock1 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TraceActivity.class.getName());
//			wakeLock1.acquire();
			//
			 // 在使用SDK各组件之前初始化context信息，传入ApplicationContext
	        // 注意该方法要再setContentView方法之前实现
			/***
	         * 初始化定位sdk，建议在Application中创建
	         */
			
			Intent intent=getIntent();
			diary_id=intent.getIntExtra("diary_id", -1);
			if(diary_id==-1){
				Log.d("debug","没有设置 diary_id");
				this.finish();
			}
	        
	        SDKInitializer.initialize(getApplicationContext()); 
	        setContentView(R.layout.activity_share_trace);
	         
	     //获取地图控件引用  		
	        mMapView = (MapView) findViewById(R.id.mybmapview);
	      	mMapView.showZoomControls(false);
	      	mBaiduMap=mMapView.getMap();// 获取地图控制
	      	mBaiduMap.setMyLocationEnabled(true);
	      	mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类    
	      	setLocationOption(0,mLocationClient);
	      	BDLocationListener myListener = new MyLocationListenner();
	      	mLocationClient.registerLocationListener( myListener );    //注册监听函数
	      	mLocationClient.start();	
	      	
	    
	      	
	      	
	      	bt1=(Button) findViewById(R.id.bt_show);
	      	//显示轨迹
	      	bt1.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					drawLine();
				}
			});
		}
	      	

		private ServiceConnection connection=new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				downLoadBinder = (DownLoadBinder) service;
				LocalService service2 = downLoadBinder.getService();
	             /**
	             * 实现回调，得到实时刷新的数据
	             */
		
			}
		};	
		
		//将view转成bitmap，用于自定义百度图标
		 private Bitmap getViewBitmap(View addViewContent) {

		        addViewContent.setDrawingCacheEnabled(true);

		        addViewContent.measure(
		                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
		                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		        addViewContent.layout(0, 0,
		                addViewContent.getMeasuredWidth(),
		                addViewContent.getMeasuredHeight());

		        addViewContent.buildDrawingCache();
		        Bitmap cacheBitmap = addViewContent.getDrawingCache();
		        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

		        return bitmap;
		    }
		
		/**
		 * 画轨迹
		 */
		private void drawLine() {
			// 构造折线点坐标
//	      	List<LatLng> points = new ArrayList<LatLng>();
//	      	Toast tst=Toast.makeText(MainActivity.this, pt1.latitude+"", Toast.LENGTH_SHORT);
//	      	tst.show();
//	      	points.add(pt1);
//	      	points.add(new LatLng(pt1.latitude,pt1.longitude));
//	      	points.add(new LatLng(pt1.latitude,pt1.longitude+0.001));
//	      	points.add(new LatLng(pt1.latitude+0.001,pt1.longitude+0.002));
//	      	points.add(new LatLng(pt1.latitude+0.002,pt1.longitude+0.003));
	      	
	      	 
	      	//构建分段颜色索引数组
//	      	List<Integer> colors = new ArrayList<Integer>();
//	      	colors.add(Integer.valueOf(Color.BLUE));
//	      	colors.add(Integer.valueOf(Color.RED));
//	      	colors.add(Integer.valueOf(Color.YELLOW));
//	      	colors.add(Integer.valueOf(Color.GREEN));

			Database db=new CloudAndLocalDatabase(ShareTraceActivity.this);
			db.open();
			
	        
	      //获取轨迹点数据
			ArrayList<Location> locations= db.getLocations(diary_id); 
			ArrayList<LatLng> gjList=new ArrayList<LatLng>();
			for(int i=0;i<locations.size();++i){
				Location l=locations.get(i);
	            LatLng lt=new LatLng(l.latitude, l.longitude);
	            gjList.add(lt);
			}
			
			//获取图片位置数据
			LocalSqliteDatabase dba=new LocalSqliteDatabase(ShareTraceActivity.this);
			dba.open();
			ArrayList<Integer> idlist=dba.getAllLocationIdFromPhoto();
			ArrayList<Integer> location_idlist=new ArrayList<Integer>();
			ArrayList<LatLng> tplist=new ArrayList<LatLng>();
			for (Integer id : idlist) {
				if(dba.isLocationIdFromDiary(id, diary_id))
				{
					location_idlist.add(id);
					Location l=dba.getLocationById(id);
			        LatLng lt=new LatLng(l.latitude, l.longitude);
			        tplist.add(lt);
				}
			}
	        
	      	db.close();
	      	
	      	if(gjList.size()>=2){
	      		//起点图标
	      	    //定义Maker坐标点  
	      		LatLng point = new LatLng(gjList.get(0).latitude, gjList.get(0).longitude);  
	      		//构建Marker图标  
	      		BitmapDescriptor bitmap = BitmapDescriptorFactory  
	      		    .fromResource(R.drawable.icon_star);  
	      		//构建MarkerOption，用于在地图上添加Marker  
	      		OverlayOptions option = new MarkerOptions()  
	      		    .position(point)  
	      		    .icon(bitmap);  
	      		//在地图上添加Marker，并显示  
	      		mBaiduMap.addOverlay(option);
	      		
	      	    //终点图标
	      	    //定义Maker坐标点  
	      		point = new LatLng(gjList.get(gjList.size()-1).latitude, gjList.get(gjList.size()-1).longitude);  
	      		//构建Marker图标  
	      		bitmap = BitmapDescriptorFactory  
	      		    .fromResource(R.drawable.icon_end);  
	      		//构建MarkerOption，用于在地图上添加Marker  
	      		option = new MarkerOptions()  
	      		    .position(point)  
	      		    .icon(bitmap);  
	      		//在地图上添加Marker，并显示  
	      		mBaiduMap.addOverlay(option);
	      		
	      		//
	      		MapStatus.Builder builder = new MapStatus.Builder();
	            builder.target(gjList.get(0)).zoom(16.0f);
	            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
//	      		Toast.makeText(this, "轨迹"+gjList.size(), Toast.LENGTH_LONG).show();
	      		OverlayOptions ooPolyline = new PolylineOptions().width(10)
	      				.points(gjList).color(Integer.valueOf(Color.RED));
	      		//添加在地图中
	      		Polyline  mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
	      	}
	      	
	      //绘制照片图标
	      	if(tplist.size()>=1){
//	      		Toast.makeText(this, "相片"+tplist.size(), Toast.LENGTH_LONG).show();

	      		for(int i=0;i<tplist.size();i++){
	      			LatLng lt=tplist.get(i);
	      			location_id=location_idlist.get(i);
	      		//构建Marker图标  
	      			Marker marker;
	      			View v;
	      			ImageView im_photo;
	      			v=this.getLayoutInflater().inflate(R.layout.photo_marker, null);
	    	      	im_photo=(ImageView) v.findViewById(R.id.im_photo);

	      			LocalSqliteDatabase ldb=new LocalSqliteDatabase(ShareTraceActivity.this);
	  				ldb.open();
	  			    ArrayList<String> imageStr=dba.getPhotos(location_id);
	  			  MarkerInfoUtil info=new MarkerInfoUtil(Environment.getExternalStorageDirectory()
	                      .getAbsolutePath() +File.separator+imageStr.get(0));
	                Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()
	                        .getAbsolutePath() +File.separator+imageStr.get(0)));
	                im_photo.setImageURI(uri);
	      		//设置成功后把View转换成Bitmap
	      			Bitmap viewBitmap = getViewBitmap(v);  
	      		//调用百度地图提供的api获取刚转换的Bitmap
	      			BitmapDescriptor bitmap=BitmapDescriptorFactory.fromBitmap(viewBitmap);
	          		//构建MarkerOption，用于在地图上添加Marker  
	          		OverlayOptions option = new MarkerOptions()  
	          		    .position(lt)  
	          		    .icon(bitmap);  
	          		//在地图上添加Marker，并显示  

	          		marker=(Marker) mBaiduMap.addOverlay(option);
	          		
	          	    //使用marker携带info信息，当点击事件的时候可以通过marker获得info信息
	                Bundle bundle = new Bundle();
	                //info必须实现序列化接口
	                bundle.putSerializable("info", info);
	                marker.setExtraInfo(bundle);
	          	    
	      		} 	
//	      	//添加marker点击事件的监听
	      		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
	      		    @Override
	      		    public boolean onMarkerClick(Marker marker) {
	      		    	
	      		    //打开图片
//	      		    	LocalSqliteDatabase dba=new LocalSqliteDatabase(TraceActivity.this);
//	      				dba.open();
//	      			    ArrayList<String> imageStr=dba.getPhotos(location_id);
	      		    //从marker中获取info信息
	      		        Bundle bundle = marker.getExtraInfo();
	      		        MarkerInfoUtil infoUtil = (MarkerInfoUtil) bundle.getSerializable("info");
	    		        File file = new File(infoUtil.getImgStr());
	    		        if( file != null && file.isFile() == true){
	    		         Intent intent = new Intent();
	    		         intent.setAction(Intent.ACTION_VIEW);
	    		         intent.setDataAndType(Uri.fromFile(file), "image/*");
	    		         startActivity(intent);  
	    		         }
//	      		    	Toast.makeText(TraceActivity.this, "显示图片", Toast.LENGTH_SHORT).show();
	      		        return true;
	      		    }
	      		});

	      	}
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
		
		@Override
		protected void onStop() {
			
			//停止定位
//			mBaiduMap.setMyLocationEnabled(false);
//	        mLocationClient.stop();
//	        myOrientationListener.stop();
	        super.onStop();
		}
		
		@Override
		protected void onResume() {
			
			mMapView.onResume();
			super.onResume();
		}
		
		@Override
		protected void onPause() {
			
			mMapView.onPause();
			super.onPause();
		}
		
		@Override
		protected void onDestroy() {
			//退出时销毁定位
//	        mLocationClient.stop();
//	        mBaiduMap.setMyLocationEnabled(false);
//			mMapView.onDestroy();
//			mMapView = null;
			//注销设备锁
//			if (wakeLock1 != null) {
//				wakeLock1.release();
//				wakeLock1 = null;
//			}
			super.onDestroy();
		}
}
