package com.tourdiary.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.zh.tourdiary.R;

public class FunctionActivity extends Activity implements OnClickListener {

	private TextView title;
	private SharedPreferences sp;
	private Button bt_trace,bt_camera,bt_movie;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_function);
		title=(TextView) findViewById(R.id.tv_functiontitle);
		bt_trace=(Button) findViewById(R.id.bt_trace);
		bt_camera=(Button) findViewById(R.id.bt_camera);
		bt_movie=(Button) findViewById(R.id.bt_movie);
//		sp = getSharedPreferences("config",Context.MODE_APPEND); 
//		title.setText(sp.getString("curname", "日记标题"));
		Intent intent=getIntent();
		title.setText(intent.getStringExtra("cur_diary_name"));
		sp = getSharedPreferences("config",Context.MODE_APPEND);
		Editor editor = sp.edit();
		// 存入当前选中的 diary name
		editor.putString("cur_diary_name", title.getText().toString());
		editor.commit();
		bt_trace.setOnClickListener(this);
		bt_camera.setOnClickListener(this);
		bt_movie.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.bt_trace:
			Intent intent=new Intent();
			intent.setClass(FunctionActivity.this, TraceActivity.class);
			startActivity(intent);
			break;
		case R.id.bt_camera:
			Intent intent1=new Intent(FunctionActivity.this,CameraActivity.class);
			startActivity(intent1);
			break;
		case R.id.bt_movie:
			Intent intent2=new Intent(FunctionActivity.this,VideoActivity.class);
			startActivity(intent2);
			break;
		default:
				break;
	}
		
	}
    
		


}
