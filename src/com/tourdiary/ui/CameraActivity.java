package com.tourdiary.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.tourdiary.util.CloudAndLocalDatabase;
import com.tourdiary.util.LocalSqliteDatabase;
import com.tourdiary.util.Location;
import com.zh.tourdiary.R;

public class CameraActivity extends Activity {
	private final int TAKE_PICTURE = 1;
	private String filename;
	private Button camera, picture;
	private final String strImgPath = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator + "zheng";// 照片地址

	// 定位对象
	public static LocationClient mLocationClient = null;
	static private ArrayList<LatLng> ptList1 = new ArrayList<LatLng>();
	private LatLng pt1;
	private SharedPreferences sp1;
	private int location_id = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		camera = (Button) findViewById(R.id.bt_opencamera);
		picture = (Button) findViewById(R.id.bt_openpicture);
		camera.setOnClickListener(onclick);
		picture.setOnClickListener(onclick);

		sp1 = getSharedPreferences("config", Context.MODE_APPEND);
		mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
		setLocationOption(0, mLocationClient);
		BDLocationListener myListener = new MyLocationListenner();
		mLocationClient.registerLocationListener(myListener); // 注册监听函数

	}

	/**
	 * 设置定位参数
	 */
	private void setLocationOption(int span, LocationClient lc) {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
		option.setCoorType("bd09ll");// 可选，默认gcj02，设置返回的定位结果坐标系

		option.setScanSpan(span);// 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
		option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
		option.setOpenGps(true);// 可选，默认false,设置是否使用gps
		option.setLocationNotify(true);// 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
		option.setIsNeedLocationDescribe(true);// 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
		option.setIsNeedLocationPoiList(true);// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
		option.setIgnoreKillProcess(false);// 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
		option.SetIgnoreCacheException(false);// 可选，默认false，设置是否收集CRASH信息，默认收集
		option.setEnableSimulateGps(false);// 可选，默认false，设置是否需要过滤gps仿真结果，默认需要
		lc.setLocOption(option);
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null) {
				return;
			}
			// pt1=new LatLng(location.getLatitude(), location.getLongitude());
			// ptList1.add(pt1);

			Location lo = new Location();
			lo.latitude = location.getLatitude();
			lo.longitude = location.getLongitude();

			String diaryname = sp1.getString("cur_diary_name", "");
			LocalSqliteDatabase dba = new LocalSqliteDatabase(
					CameraActivity.this);
			dba.open();
			int diary_id = dba.getIdByDiaryName(diaryname);
			dba.close();

			if (diary_id != -1) {
				// 插入数据
				CloudAndLocalDatabase db = new CloudAndLocalDatabase(
						CameraActivity.this);
				db.open();
				location_id=(int) db.insertLocation(lo, diary_id);
				// Toast.makeText(getApplicationContext(),"paizhao",Toast.LENGTH_SHORT).show();
//				location_id = dba.getMaxIdFromLocation();
				if (location_id != -1) {
					db.insertPhoto(filename, location_id);
				}
			}
		}

		@Override
		public void onConnectHotSpotMessage(String arg0, int arg1) {
			// TODO Auto-generated method stub

		}
	}

	private OnClickListener onclick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_opencamera:
				// 打开照相机
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				checkFilePath(strImgPath);
				String time = new SimpleDateFormat("yyyyMMdd_HHmmss")
						.format(new Date());
				String diaryname = getDiaryName();
				File file = new File(strImgPath + File.separator + diaryname
						+ File.separator + time + ".jpg");
				filename = "zheng" + File.separator + diaryname
						+ File.separator + time + ".jpg";
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
				startActivityForResult(intent, TAKE_PICTURE);
				// mLocationClient.start();
				break;
			case R.id.bt_openpicture:
				// 查看图片
				Intent intent1 = new Intent(CameraActivity.this,
						PictureActivity.class);
				startActivity(intent1);
				break;
			default:
				break;
			}

		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			mLocationClient.start();
			finish();
			Intent intent1 = new Intent(CameraActivity.this,
					CameraActivity.class);
			startActivity(intent1);
			
		}
	};

	// 检查照片储存地址是否存在
	public void checkFilePath(String strImgPath) {
		File path = new File(strImgPath);
		if (!path.exists()) {
			path.mkdir();
		}
	}

	public String getDiaryName() {
		SharedPreferences sp;
		sp = getSharedPreferences("config", Context.MODE_APPEND);
		String diaryname = sp.getString("cur_diary_name", "");
		return diaryname;
	}
}
