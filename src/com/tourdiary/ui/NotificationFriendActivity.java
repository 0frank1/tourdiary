package com.tourdiary.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import com.tourdiary.util.CloudAndLocalDatabase;
import com.tourdiary.util.Message;
import com.zh.tourdiary.R;

public class NotificationFriendActivity  extends Activity {

	int from;
	int to;
	String message_obj_id;
	
	TextView content; 
	TextView from_name;
	TextView from_id;
	
	Button accept_btn,reject_btn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification_friend);
		
		Intent intent=getIntent();
		
		from_id=(TextView)findViewById(R.id.notification_userid);
		from=intent.getIntExtra("from", -1);
		from_id.setText("用户id:"+String.valueOf(from));
		
		message_obj_id=intent.getStringExtra("message_obj_id");
		
		to=intent.getIntExtra("to", -1);
		
		from_name=(TextView)findViewById(R.id.notification_username);
		from_name.setText(intent.getStringExtra("from_username")+" 申请添加好友");
		
		content=(TextView) findViewById(R.id.notification_content);
		content.setText("附加消息: "+intent.getStringExtra("content"));
		
		accept_btn=(Button)findViewById(R.id.notification_accept);
		accept_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				CloudAndLocalDatabase db=new CloudAndLocalDatabase(NotificationFriendActivity.this);
				db.open();
				db.insertFriendShip(from, to);
				db.close();
				//从数据库删除这个消息
				delMessage();
				NotificationFriendActivity.this.finish();
			}
			
		});
		reject_btn=(Button)findViewById(R.id.notification_reject);
		reject_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//从数据库删除这个消息
				delMessage();
				NotificationFriendActivity.this.finish();
			}
			
		});
	}
	
	void delMessage(){
		//从云数据库删除这个消息
		Message m=new Message();
		m.setObjectId(message_obj_id);
		m.delete(new UpdateListener(){

			@Override
			public void done(BmobException arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
}
