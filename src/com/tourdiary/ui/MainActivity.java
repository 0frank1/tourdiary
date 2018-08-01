package com.tourdiary.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.tourdiary.util.CloudAndLocalDatabase;
import com.tourdiary.util.Database;
import com.tourdiary.util.SynchronizeListener;
import com.tourdiary.util.User;
import com.zh.tourdiary.R;

public class MainActivity extends Activity {

	private Button bt_login;
	private Button bt_register;
	private EditText et_userName;
	private EditText et_password;
	private CheckBox cb_remember;
	private CheckBox cb_automatic;
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sp = getSharedPreferences("config", Context.MODE_APPEND);
		findViewById();

		// 注册好按钮事件
		setUpListener();


		Boolean isAutomatic = sp.getBoolean("isAutomatic", false);
		if (isAutomatic) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, TabActivity.class);
			startActivity(intent);
			MainActivity.this.finish();
			return;
		}
		//
		String username_s = sp.getString("userName", "");
		if (!username_s.equals("")) {
			et_userName.setText(username_s);
		}
		if (sp.getBoolean("isRemember", false)) {
			cb_remember.setChecked(true);
			String userpassword = sp.getString("passWord", "");
			et_password.setText(userpassword);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	protected void findViewById() {
		bt_login = (Button) findViewById(R.id.bt_login);
		et_userName = (EditText) findViewById(R.id.loginname);
		et_password = (EditText) findViewById(R.id.loginpass);
		bt_register = (Button) findViewById(R.id.bt_register);
		cb_remember = (CheckBox) findViewById(R.id.cb_remember);
		cb_automatic = (CheckBox) findViewById(R.id.cb_automatic);
	}

	void setUpListener() {
		// 注册按钮
		bt_register.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, RegisterActivity.class);
				startActivity(intent);
				MainActivity.this.finish();

			}
		});
		// 登录按钮
		bt_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String userName = et_userName.getText().toString().trim();
				String passWord = et_password.getText().toString().trim();
				if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(passWord)) {

					Toast.makeText(MainActivity.this, "用户名或者密码为空！",
							Toast.LENGTH_SHORT).show();
					return;

				} else {
					Database db = new CloudAndLocalDatabase(MainActivity.this);
					db.open();
					ArrayList<User> users = db.getUser(userName);
					if (users.size() != 0
							&& users.get(0).password.equals(passWord)) {
						User user = users.get(0);
						Editor editor = sp.edit();
						// 是否选择自动登录
						if (cb_automatic.isChecked()) {
							editor.putBoolean("isAutomatic", true);
						} else {
							editor.putBoolean("isAutomatic", false);
						}
						// 是否记住密码
						if (cb_remember.isChecked()) {
							editor.putBoolean("isRemember", true);
						} else {
							editor.putBoolean("isRemember", false);
						}
						editor.putBoolean("isLogin", true);
						editor.putString("userName", userName);
						editor.putString("passWord", passWord);
						editor.putInt("userId", user.id);

                        editor.commit();
                        db.close();
                        
                        Intent intent=new Intent();
//                        intent.putExtra("userid", user.id);
            			intent.setClass(MainActivity.this, TabActivity.class);

						startActivity(intent);
						MainActivity.this.finish();

					} else {
						Toast.makeText(MainActivity.this, "用户名或者密码错误！",
								Toast.LENGTH_SHORT).show();
						db.close();
					}

				}

			}
		});
	}
}
