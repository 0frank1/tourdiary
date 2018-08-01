package com.tourdiary.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.tourdiary.util.CloudAndLocalDatabase;
import com.tourdiary.util.Diary;
import com.tourdiary.util.LocalSqliteDatabase;
import com.tourdiary.util.Moment;
import com.zh.tourdiary.R;

public class AddMomentActivity extends Activity{
	int userid;
	int diary_id;	//spinner_diary选中的日记id
	boolean validation;	//是否可以发表
	
	TextView report;
	TextView cancel;
	EditText textview_content;
	Spinner spinner_diary;

	ArrayList<Diary> diarys;
	ArrayList<String> diary_names;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent=getIntent();
		userid=intent.getIntExtra("userid", -1);
		if(userid==-1){
			this.finish();
			return;
		}
		
		setContentView(R.layout.activity_add_new_moment);
		report=(TextView) findViewById(R.id.report);
//		report.setEnabled(false);
		cancel=(TextView) findViewById(R.id.cancel);
		textview_content=(EditText) findViewById(R.id.new_moment_content);
		spinner_diary=(Spinner) findViewById(R.id.diary_spinner);
		
		loadEvents();
		loadDiarySpinner();
	}
	private void loadEvents() {
		report.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//能点击,说明能发表
				Moment moment=new Moment();
				moment.userid=userid;
				moment.diary_id=diary_id;
				moment.content=textview_content.getText().toString();
				moment.time=new SimpleDateFormat("MM月dd日 HH:mm").format(new Date());
				moment.view_times=0;
				CloudAndLocalDatabase db=new CloudAndLocalDatabase(AddMomentActivity.this);
				db.open();
				db.insertMoments(moment, userid, diary_id);
				db.close();
				AddMomentActivity.this.finish();
			}
		});
		cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				AddMomentActivity.this.finish();
			}});
	}
	private void loadDiarySpinner() {
		LocalSqliteDatabase db=new LocalSqliteDatabase(this);
		db.open();
		diarys = db.getDiary(userid);
		diary_names=new ArrayList<String>();
		for(int i=0;i!=diarys.size();++i){
			diary_names.add(diarys.get(i).name);
		}
		String [] arr=(String [])diary_names.toArray(new String[diary_names.size()]);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arr);
		spinner_diary.setAdapter(adapter);
		spinner_diary.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				diary_id= diarys.get(arg2).id;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				validation=false;	//不能发表
			}});
		db.close();
	}
	
	//检查是否可以发表动态
	boolean checkValidation(){
		return false;
	}
}
