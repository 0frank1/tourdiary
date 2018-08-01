package com.tourdiary.ui;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import com.tourdiary.util.CloudAndLocalDatabase;
import com.tourdiary.util.CloudBmobDatabase;
import com.tourdiary.util.LocalSqliteDatabase;
import com.tourdiary.util.Message;
import com.tourdiary.util.User;
import com.zh.tourdiary.R;

public class AddFriendActivity extends Activity {

	Button search_btn;
	EditText username;
	EditText content;
	TextView user_info_name;
	TextView user_info_id;
	Button add_btn;
	LinearLayout detail;
	
	int userid;
	int to_add_userid;	//这个是准备添加的好友的id
	int userid_search_result;	//这个是搜索好友,得到的结果
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friend);
		
		findViews();
		
		//得到当前登录的userid		
		Intent intent=getIntent();
		userid=intent.getIntExtra("userid",-1);
		if(userid==-1){
			Log.d("debug","error,没有登录,userid不正确");
			this.finish();
		}
	}
	
	void findViews(){
		search_btn=(Button)findViewById(R.id.add_search_button);
		setSearchClickListener();
		
		username=(EditText)findViewById(R.id.add_username_edit);
		
		content=(EditText)findViewById(R.id.add_friend_content);
		
		user_info_name=(TextView)findViewById(R.id.add_user_name);
		
		user_info_id=(TextView)findViewById(R.id.add_user_id);
		
		add_btn=(Button)findViewById(R.id.add_friend_btn);
		setAddBtnClickListener();
		
		detail=(LinearLayout)findViewById(R.id.user_detail);
	}
	
	void setSearchClickListener(){
		search_btn.setOnClickListener(new OnClickListener (){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				search_btn.setText("正在搜索...");
				search_btn.setEnabled(false);
				add_btn.setEnabled(false);
				CloudAndLocalDatabase db=new CloudAndLocalDatabase(AddFriendActivity.this);
				db.open();
				List<User> users=db.getUser(username.getText().toString());
				if(users.size()!=0){
					User u=users.get(0);
					if(userid==u.id){
						//搜索到的人是本人
					}else{
						if(db.getFriendship(userid, u.id).size()==0){
							add_btn.setText("添加好友");
						}else{
							add_btn.setText("删除好友");
						}
						detail.setVisibility(View.VISIBLE);
						
						String myname=db.getUser(userid).get(0).name;
						content.setText("我是 "+myname);
						user_info_name.setText(u.name);
						user_info_id.setText("用户id: "+u.id);
						to_add_userid=u.id;
						add_btn.setEnabled(true);
					}
				}else{
					detail.setVisibility(View.INVISIBLE);
				}
				search_btn.setText("搜索");
				search_btn.setEnabled(true);
				db.close();
			}
			
		});
	}

	void setAddBtnClickListener(){
		add_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(add_btn.getText().toString()=="添加好友"){
					//发送添加好友的消息
					CloudBmobDatabase bmob=new CloudBmobDatabase(AddFriendActivity.this);
					bmob.open();
					Message m=new Message();
					m.type=Message.Type.AddFriend;
					m.from=userid;
					m.to=to_add_userid;
					m.content=content.getText().toString();
					bmob.sendMessage(m);
					bmob.close();
				}else{
					
				}
				AddFriendActivity.this.finish();
			}
			
		});
	}
}
