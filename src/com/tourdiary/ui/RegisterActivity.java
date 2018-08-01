package com.tourdiary.ui;


import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import com.tourdiary.util.CloudAndLocalDatabase;
import com.tourdiary.util.Database;
import com.tourdiary.util.SynchronizeListener;
import com.tourdiary.util.User;
import com.zh.tourdiary.R;




public class RegisterActivity extends Activity {

	private Button bt_register;// 
	private EditText et_phoneNumber;// 
	private EditText et_password;// 
	private EditText et_repassword;// 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		setContentView(R.layout.activity_register);


		et_phoneNumber = (EditText) findViewById(R.id.et_phonenumber);
		et_password = (EditText) findViewById(R.id.et_password);
		et_repassword = (EditText) findViewById(R.id.et_repassword);
		bt_register = (Button) findViewById(R.id.bt_register);

		//假设在MainActivity中已经同步完成,所以这里不同步了
		
//		SQLiteDatabase mSQLiteDatabase=this.openOrCreateDatabase("Test",MODE_PRIVATE,null);
		
		// 密码长度检验
		et_password.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub

				if (!hasFocus) {
					if (et_password.getText().toString().trim().length() < 6) {
						Toast.makeText(RegisterActivity.this, "密码的长度不能小于6！",
								Toast.LENGTH_SHORT).show();
						et_password.setText("");
					}

				}
			}

		});
		//两次密码对比检验
		et_repassword.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub

				if (!hasFocus) {
					String password = et_password.getText().toString().trim();
					String repassword = et_repassword.getText().toString()
							.trim();

					if (password.equals(repassword) == false) {
						et_repassword.setText("");
						Toast.makeText(RegisterActivity.this,
								"两次输入的密码不一致", Toast.LENGTH_SHORT)
								.show();
						
					}

				}
			}

		});
		// 注册按钮事件
		bt_register.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				User user=new User();
				user.name=et_phoneNumber.getText().toString().trim();
				user.password=et_password.getText().toString().trim();
				if(user.name.isEmpty())
					return;
				
				CloudAndLocalDatabase db = new CloudAndLocalDatabase(RegisterActivity.this); 
				db.open();				
				
				if(db.existUser(user.name)){
					Toast.makeText(RegisterActivity.this,
							"该账号已经注册", Toast.LENGTH_SHORT)
							.show();
				}
				else{
					db.insertUser(user);
					
					//SharedPreferences操作
					SharedPreferences preferences=getSharedPreferences("config",Context.MODE_APPEND);
					Editor editor=preferences.edit();	
					editor.putString("userName", user.name);
					editor.putString("passWord", user.password);
					editor.commit();

//					editor.putInt("usertype", userType);
					Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
			        startActivity(intent);
			        RegisterActivity.this.finish();
				}
				db.close();
			}
		});

		
	}

}
