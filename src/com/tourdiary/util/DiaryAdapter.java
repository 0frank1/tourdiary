package com.tourdiary.util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.zh.tourdiary.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DiaryAdapter extends BaseAdapter {

	//创建view时必须要提供context
		public Activity activity;	
		//提供数据源，日记 列表
		public List<Diary> list=new ArrayList<Diary>();	
		//当前列表路径
		public String currPath;	
		private Bitmap bmp_folder;
		
		public int getCount() {

			return list.size();
		}

		public Object getItem(int position) {

			return list.get(position);
		}
		
		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View view, ViewGroup arg2) {
			View v_return;
			//申明一个视图装载listView条目
			v_return=View.inflate(activity, R.layout.diary_item, null);
			TextView textPath=(TextView) v_return.findViewById(R.id.text_path);
			ImageView img=(ImageView) v_return.findViewById(R.id.img_filefolder);
			TextView create_date=(TextView)v_return.findViewById(R.id.create_date);
			//获取当前位置
			Diary diary=list.get(position);
			//获取文件名
			textPath.setText(diary.name);
			//显示出文件夹的图片
			img.setImageBitmap(bmp_folder);
			create_date.setText("创建于:"+diary.create_date);
			
			
			//肉眼视图
			return v_return;
		}


		
		public DiaryAdapter(Activity activity){
			
			this.activity=activity;
			//绑定显示文件图标
			bmp_folder= BitmapFactory.decodeResource(activity.getResources(),R.drawable.bmp_floder);
		}

}
