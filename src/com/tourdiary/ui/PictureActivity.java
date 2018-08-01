package com.tourdiary.ui;

import java.io.File;

import com.zh.tourdiary.R;
import com.tourdiary.util.DiaryAdapter;
import com.tourdiary.util.ImageAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class PictureActivity extends Activity {
    private GridView gridView;
    private String str1=Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "zheng";//¹ì¼£ÎÄ¼þ¸ùÄ¿Â¼
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		gridView=(GridView) findViewById(R.id.gv_piclist);
		//新建一个适配器。用来适配ListView，显示文件列表
		ImageAdapter imgAdapter= new ImageAdapter(this);
		//获取当前日记文件夹
		String diaryname=getDiaryName();
		//显示默认的文件目录
		imgAdapter.scanFiles(str1+File.separator+diaryname);
		//设置适配器，绑定适配器
		gridView.setAdapter(imgAdapter);
		
		//listView中按键响应
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				//声明一个适配器 
		        ImageAdapter fileNext=(ImageAdapter) gridView.getAdapter();
		        //
		        File f=fileNext.list.get(arg2); 
		       //获取图片文件路径
		        String imgStr=fileNext.currPath;
		        String imgName=f.getName();
		        //打开图片
		        File file = new File(imgStr+File.separator+imgName);
		        if( file != null && file.isFile() == true){
		         Intent intent = new Intent();
		         intent.setAction(Intent.ACTION_VIEW);
		         intent.setDataAndType(Uri.fromFile(file), "image/*");
		         startActivity(intent);  
		         }    
			}
		});
	}

	public String getDiaryName(){
    	SharedPreferences sp;
    	sp = getSharedPreferences("config",Context.MODE_APPEND);
		String diaryname=sp.getString("cur_diary_name", ""); 
    	return diaryname;
    }
	
}
