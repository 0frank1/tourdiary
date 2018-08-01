package com.tourdiary.ui;

import com.tourdiary.util.CloudAndLocalDatabase;
import com.tourdiary.util.SynchronizeListener;
import com.zh.tourdiary.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class GuideActivity extends Activity {
	TextView tv_join;
	TextView tv_turn;
	int time = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		tv_join = (TextView) findViewById(R.id.tv_join);
		tv_turn = (TextView) findViewById(R.id.tv_turn);

		tv_turn.setText("请稍等,正在同步...");

		// 判断网络是否可用
		if (isNetworkAvailable(this) == false) {
			tv_turn.setText("网络不可用");
			return;
		}
		
		final CloudAndLocalDatabase db = new CloudAndLocalDatabase(this);
		db.open();
		db.synchronize(new SynchronizeListener() {

			@Override
			public void finished() {
				db.close();
				Intent intent = new Intent(GuideActivity.this,
						MainActivity.class);
				startActivity(intent);
				GuideActivity.this.finish();	//从主界面返回时,不再回到此界面
			}

		});
	}

	public boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				// 当前网络是连接的
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					// 当前所连接的网络可用
					return true;
				}
			}
		}
		return false;
	}
}
