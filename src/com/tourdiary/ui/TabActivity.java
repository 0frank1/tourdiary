package com.tourdiary.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.tourdiary.util.CloudAndLocalDatabase;
import com.tourdiary.util.CloudBmobDatabase;
import com.tourdiary.util.Database;
import com.tourdiary.util.Diary;
import com.tourdiary.util.DiaryAdapter;
import com.tourdiary.util.FriendShip;
import com.tourdiary.util.LocalSqliteDatabase;
import com.tourdiary.util.Location;
import com.tourdiary.util.Message;
import com.tourdiary.util.MessageItemAdapter;
import com.tourdiary.util.Moment;
import com.tourdiary.util.MomentsItemAdapter;
import com.tourdiary.util.User;
import com.zh.tourdiary.R;

public class TabActivity extends Activity {

	int userid;
	private String diary_path = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator + "zheng";//
	private String photo_save_path = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator + "zh";//
	// cur_diary_dir ，当点击，长点击时，保存这个
	protected String clicked_diary_dir;
	protected int diary_clicked_index;
	private SharedPreferences sp;

	List<String> friends = new ArrayList<String>();
	List<Message> messages = new ArrayList<Message>();
	List<Moment> moments = new ArrayList<Moment>();
	MessageItemAdapter message_adapter;
	DiaryAdapter diary_adapter;

	ViewPager pager;
	List<View> views = new ArrayList<View>();
	View diary_view;
	View friend_view;
	View message_view;
	View moments_view;

	LinearLayout diary_tab;
	LinearLayout friend_tab;
	LinearLayout message_tab;
	LinearLayout moments_tab;

	ImageView diary_img;
	ImageView friend_img;
	ImageView message_img;
	ImageView moments_img;

	ListView diary_listview;
	ListView friend_listview;
	ListView message_listview;
	ListView moments_listview;

	TextView message_count; // 这个用来显示未读信息的数量

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_tab);
		sp = getSharedPreferences("config", Context.MODE_APPEND);

		// Intent intent = getIntent();
		// userid = intent.getIntExtra("userid", -1);
		userid = sp.getInt("userId", -1);
		if (userid == -1) {
			this.finish();
			return;
		}

		initViews();
		loadDiarys();
		loadFriends();
		loadMoments(); // 加载动态
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// 监听菜单按钮
		case R.id.action_settings2:
			logoutUser();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// 注销账户
	public void logoutUser() {
		sp = getSharedPreferences("config", Context.MODE_APPEND);
		Editor editor = sp.edit();
		editor.putBoolean("isAutomatic", false);
		editor.putBoolean("isRemember", false);
		editor.putString("passWord", "");
		editor.putString("userName", "");
		editor.commit();
		Intent intent = new Intent();
		intent.setClass(TabActivity.this, MainActivity.class);
		startActivity(intent);
		TabActivity.this.finish();
	}

	private void loadFriends() {
		friend_listview = (ListView) friend_view.findViewById(R.id.my_friends);
		friend_listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				SimpleAdapter adapter = (SimpleAdapter) friend_listview
						.getAdapter();
				Map<String, Object> item = (Map<String, Object>) adapter
						.getItem(arg2);
				String username = item.get("friend_name").toString();
				LocalSqliteDatabase local = new LocalSqliteDatabase(
						TabActivity.this);
				local.open();

				ArrayList<User> users = local.getUser(username);

				final List<String> diary_names = getDiaryNames();

				if (users.size() > 0) {
					final User u = users.get(0);
					AlertDialog.Builder builder = new AlertDialog.Builder(
							TabActivity.this);
					builder.setTitle("选择要分享的日记");
					builder.setItems(diary_names
							.toArray(new CharSequence[diary_names.size()]),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub
									List<Integer> diary_ids = getDiaryIds();
									Log.d("debug",
											"分享日记:"
													+ String.valueOf(diary_ids
															.get(arg1)) + " 给 "
													+ u.name);
									Message m = new Message();
									m.type = Message.Type.ShareDiary;
									m.from = userid;
									m.to = u.id;
									m.content = "给你分享了一篇日记:"
											+ diary_names.get(arg1);
									m.diary = diary_ids.get(arg1);
									m.time = new SimpleDateFormat(
											"yyyy年MM月dd日 HH:mm")
											.format(new Date());
									m.save(new SaveListener<String>() {

										@Override
										public void done(String arg0,
												BmobException arg1) {
											// TODO Auto-generated method stub

										}
									});
								}
							});
					builder.show();
				}
				local.close();
			}
		});

		Button add_btn = (Button) friend_view.findViewById(R.id.add_freinds);
		add_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent();
				// 当前登录的用户
				i.putExtra("userid", userid);
				i.setClass(TabActivity.this, AddFriendActivity.class);
				startActivity(i);
			}

		});
	}

	private void refreshFriends() {
		friends.clear();
		// 从本地加载好友,这里默认本地与云端同步完成
		final LocalSqliteDatabase local = new LocalSqliteDatabase(
				TabActivity.this);
		local.open();

		// 将本用户的所有日记 读取出来
		DiaryAdapter diary_adapter=(DiaryAdapter) diary_listview.getAdapter();
		List<Diary> diarys = diary_adapter.list;
		diarys = local.getDiary(userid);

		ArrayList<FriendShip> db_friends = local.getFriendship(userid);
		for (int i = 0; i < db_friends.size(); ++i) {
			FriendShip f = db_friends.get(i);
			ArrayList<User> users = local.getUser(f.user2);
			if (users.size() != 0) {
				friends.add(users.get(0).name);
			}
		}
		local.close();

		int[] portraits = new int[] { R.drawable.portrait_1,
				R.drawable.portrait_2, R.drawable.portrait_3,
				R.drawable.portrait_4, R.drawable.portrait_5,
				R.drawable.portrait_6, R.drawable.portrait_7,
				R.drawable.portrait_8 };
		List<Map<String, Object>> list_items = new ArrayList<Map<String, Object>>();
		for (int i = 0; i != friends.size(); ++i) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("friend_name", friends.get(i));
			int portrait_index = (int) (Math.random() * 8);
			Log.d("debug", "portrait index : " + portrait_index);
			item.put("friend_portrait", portraits[portrait_index]);
			list_items.add(item);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, list_items,
				R.layout.friend_item, new String[] { "friend_name",
						"friend_portrait" }, new int[] { R.id.friend_name,
						R.id.friend_portrait });

		friend_listview.setAdapter(adapter);
	}

	private void loadMoments() {
		MomentsItemAdapter adapter = new MomentsItemAdapter(this, moments,userid);
		moments_listview = (ListView) moments_view
				.findViewById(R.id.moments_listview);
		moments_listview.setAdapter(adapter);
		moments_listview.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(arg1.getId()==R.id.reply_btn){
					Log.d("debug","reply clicked");
				}else if(arg1.getId()==R.id.reply_edit){
					Log.d("debug","reply edit clicked");
				}
				Moment m=moments.get(arg2);
				Intent intent = new Intent(TabActivity.this,
						ShareTraceActivity.class);
				intent.putExtra("diary_id", m.diary_id);
				startActivity(intent);
			}});

		ImageView add_new = (ImageView) moments_view.findViewById(R.id.bt_new);
		add_new.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.putExtra("userid", userid);
				intent.setClass(TabActivity.this, AddMomentActivity.class);
				startActivity(intent);
			}
		});
	}

	private void refreshMoments() {
		moments.clear();
		Log.d("debug","moments size:"+moments.size());
		moments.removeAll(moments);
		Log.d("debug","moments size:"+moments.size());
		LocalSqliteDatabase db = new LocalSqliteDatabase(this);
		db.open();
		ArrayList<Moment> list = db.getMoments();
		for (int i = 0; i != list.size(); ++i) {
			moments.add(list.get(i));
			Moment m= list.get(i);
			Log.d("debug","动态id:"+m.id+" 动态内容:"+m.content);
		}
		MomentsItemAdapter adapter = (MomentsItemAdapter) moments_listview
				.getAdapter();
		adapter.notifyDataSetChanged();
		db.close();

	}
	
	List<String> getDiaryNames() {
		List<String> diary_names = new ArrayList<String>();
		DiaryAdapter adapter=(DiaryAdapter) diary_listview.getAdapter();
		List<Diary> diarys = adapter.list;
		for (int i = 0; i != diarys.size(); ++i) {
			diary_names.add(diarys.get(i).name);
		}
		return diary_names;
	}

	List<Integer> getDiaryIds() {
		List<Integer> diary_ids = new ArrayList<Integer>();
		DiaryAdapter adapter=(DiaryAdapter) diary_listview.getAdapter();
		List<Diary> diarys = adapter.list;
		for (int i = 0; i != diarys.size(); ++i) {
			diary_ids.add(diarys.get(i).id);
		}
		return diary_ids;
	}

	private void loadDiarys() {
		diary_listview = (ListView) diary_view.findViewById(R.id.lv_filelist);
		diary_listview.setOnItemLongClickListener(new MyLongClick());
		ImageView imgbt_new = (ImageView) diary_view.findViewById(R.id.bt_new);
		imgbt_new.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				createNewDiary();
			}
		});
		// listView中按键响应
		diary_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// 声明一个适配器
				DiaryAdapter diary_list = (DiaryAdapter) diary_listview
						.getAdapter();
				//
				Diary diary = diary_list.list.get(arg2);

				Intent intent = new Intent();
				intent.setClass(TabActivity.this, FunctionActivity.class);
				intent.putExtra("cur_diary_name", diary.name);
				startActivity(intent);
			}
		});
		diary_adapter=new DiaryAdapter(this);
		diary_listview.setAdapter(diary_adapter);
		showDiarysInCurUser();
	}

	// 新建日记
	public void createNewDiary() {
		checkFilePath(diary_path);
		showCreateDiaryDialog();
	}

	// 检查照片储存地址是否存在
	public void checkFilePath(String strImgPath) {
		File path = new File(strImgPath);
		if (!path.exists()) {
			path.mkdir();
		}
	}

	// 新建日记的弹出框
	private void showCreateDiaryDialog() {
		/*
		 * @setView 装入一个EditView
		 */
		final EditText editText = new EditText(TabActivity.this);
		AlertDialog.Builder inputDialog = new AlertDialog.Builder(
				TabActivity.this);
		inputDialog.setTitle("日记名称").setView(editText);
		inputDialog.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Diary diary = new Diary();
						diary.name = editText.getText().toString().trim();
						if (diary.name.isEmpty())
							return;
						diary.create_date = new SimpleDateFormat(
								"yyyyMMdd_HHmmss").format(new Date());

						// 得到当前用户id
						int userid = sp.getInt("userId", -1);
						if (userid == -1) {
							return;
						}

						// 在本地建立日记文件夹
						File riji = new File(diary_path + File.separator
								+ diary.name);
						if (!riji.exists()) {
							riji.mkdir();
						}
						// 把diary记录进数据库
						Database db = new CloudAndLocalDatabase(
								TabActivity.this);
						db.open();
						int diary_id = (int) db.insertDiary(diary, userid);
						db.close();

						Editor editor = sp.edit();
						editor.putInt(diary.name, diary_id);
						editor.commit();

						showDiarysInCurUser();
						onCreate(null);
						// finish();
						// Intent intent = new Intent(TabActivity.this,
						// TabActivity.class);
						// startActivity(intent);
					}
				}).show();
	}

	private void showDiarysInCurUser() {
		Database db = new CloudAndLocalDatabase(this);
		db.open();
		ArrayList<Diary> diarys = db.getDiary(userid);
		Log.d("debug","显示日记,共"+diarys.size()+"篇日记");
		diary_adapter.list.clear();
		for(int i=0;i!=diarys.size();++i){
			Log.d("debug","日记 id:"+diarys.get(i).id+ "名:"+diarys.get(i).name);
			diary_adapter.list.add(diarys.get(i));
		}
		diary_listview.setAdapter(diary_adapter);
		diary_adapter.notifyDataSetChanged();
		diary_adapter.currPath = diary_path;
		db.close();
	}

	private void initViews() {
		pager = (ViewPager) findViewById(R.id.viewpager);
		LayoutInflater inflater = LayoutInflater.from(this);
		diary_view = inflater.inflate(R.layout.activity_user, null);
		friend_view = inflater.inflate(R.layout.friends, null);
		message_view = inflater.inflate(R.layout.messages, null);
		moments_view = inflater.inflate(R.layout.moments, null);
		views.add(diary_view);
		views.add(friend_view);
		views.add(message_view);
		views.add(moments_view);

		diary_img = (ImageView) findViewById(R.id.diary_img);
		friend_img = (ImageView) findViewById(R.id.friend_img);
		message_img = (ImageView) findViewById(R.id.message_img);
		moments_img = (ImageView) findViewById(R.id.moments_img);

		diary_tab = (LinearLayout) findViewById(R.id.diary_tab);
		friend_tab = (LinearLayout) findViewById(R.id.friend_tab);
		message_tab = (LinearLayout) findViewById(R.id.message_tab);
		moments_tab = (LinearLayout) findViewById(R.id.moments_tab);

		message_count = (TextView) findViewById(R.id.my_message_count);
		message_listview = (ListView) message_view
				.findViewById(R.id.message_list);
		message_adapter = new MessageItemAdapter(this, messages) {
		};
		message_listview.setAdapter(message_adapter);
		message_listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Message m = messages.get(arg2);
				LocalSqliteDatabase db = new LocalSqliteDatabase(
						TabActivity.this);
				db.open();
				if (m.type == Message.Type.AddFriend) {
					String name = db.getUser(m.from).get(0).name;
					Intent intent = new Intent(TabActivity.this,
							NotificationFriendActivity.class);
					intent.putExtra("from_username", name);
					intent.putExtra("from", m.from);
					intent.putExtra("to", m.to);
					intent.putExtra("content", m.content);
					intent.putExtra("message_obj_id", m.getObjectId());
					startActivity(intent);
				} else if (m.type == Message.Type.ShareDiary) {
					int diary_id = m.diary; // 这个是分享给你的日记id
					Intent intent = new Intent(TabActivity.this,
							ShareTraceActivity.class);
					intent.putExtra("diary_id", diary_id);

					startActivity(intent);
					m.delete(new UpdateListener() {
						@Override
						public void done(BmobException arg0) {
						}
					});
				}
				db.close();
			}
		});

		diary_tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pager.setCurrentItem(0);
			}
		});
		friend_tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pager.setCurrentItem(1);
			}
		});
		message_tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pager.setCurrentItem(2);
			}
		});
		moments_tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pager.setCurrentItem(3);
			}
		});

		PagerAdapter adapter = new PagerAdapter() {

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return views.size();
			}

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				// TODO Auto-generated method stub
				return arg0 == arg1;
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				// TODO Auto-generated method stub
				container.removeView(views.get(position));
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				View view = views.get(position);
				container.addView(view);
				return view;
			}
		};

		pager.setAdapter(adapter);

		pager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				resetImg();
				int index = pager.getCurrentItem();
				switch (index) {
				case 0:
					diary_img.setImageResource(R.drawable.diary_pressed);
					break;
				case 1:
					friend_img.setImageResource(R.drawable.friend_pressed);
					break;
				case 2:
					message_img.setImageResource(R.drawable.message_pressed);
					break;
				case 3:
					moments_img.setImageResource(R.drawable.moments_pressed);
				}
			}

		});
	}

	private void resetImg() {
		diary_img.setImageResource(R.drawable.diary_normal);
		friend_img.setImageResource(R.drawable.friend_normal);
		message_img.setImageResource(R.drawable.message_normal);
		moments_img.setImageResource(R.drawable.moments_normal);
	}

	public void refreshMessages() {
		messages.clear();
		CloudBmobDatabase db = new CloudBmobDatabase(this);
		db.open();
		db.getMessages(userid, new FindListener<Message>() {
			@Override
			public void done(List<Message> message_list, BmobException arg1) {
				messages.clear();
				for (int i = 0; i != message_list.size(); ++i) {
					messages.add(message_list.get(i));
				}
				message_adapter.notifyDataSetChanged();
				updateMessagesCount();
			}

		});
		db.close();
	}

	// 检查通知,如果通知不为零,则头像上显示通知数量,否则不显示
	void updateMessagesCount() {
		if (messages.size() == 0) {
			message_count.setVisibility(View.INVISIBLE);
		} else {
			message_count.setVisibility(View.VISIBLE);
			message_count.setWidth(Math.max(message_count.getWidth(),
					message_count.getHeight()));
			// setText传入一个int,编译不出错,但是运行出错!!!1
			message_count.setText(String.valueOf(messages.size()));
		}
	}

	private class MyLongClick implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			DiaryAdapter fileNext = (DiaryAdapter) diary_listview.getAdapter();
			//
			Diary diary = fileNext.list.get(arg2);
			clicked_diary_dir = diary.name;
			diary_clicked_index=arg2;
			showPopupMenu(arg1);
			return true;
		}
	}

	private void showPopupMenu(View view) {
		// View当前PopupMenu显示的相对View的位置
		PopupMenu popupMenu = new PopupMenu(this, view);
		// menu布局
		popupMenu.getMenuInflater().inflate(R.menu.user, popupMenu.getMenu());
		// menu的item点击事件
		popupMenu
				.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (item.getItemId() == R.id.set_delete) {
							showDeleteDialog();
						}
						if (item.getItemId() == R.id.set_rename) {
							showRenameDialog();
						}
						return false;
					}
				});
		popupMenu.show();
	}

	// 删除日记的弹出框
	private void showDeleteDialog() {
		// final TextView textView = new TextView(UserActivity.this);
		// textView.setText("");
		AlertDialog.Builder deleteDialog = new AlertDialog.Builder(
				TabActivity.this);
		deleteDialog.setTitle("是否删除文件");
		deleteDialog
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						CloudAndLocalDatabase db=new CloudAndLocalDatabase(TabActivity.this);
						db.open();
						DiaryAdapter adapter=(DiaryAdapter) diary_listview.getAdapter();
						List<Diary> diarys = adapter.list;
						Log.d("debug","删除日记:"+diarys.get(diary_clicked_index).name);
						db.deleteDiary(diarys.get(diary_clicked_index).id);
						db.close();
						diarys.remove(diary_clicked_index);

						showDiarysInCurUser();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// return;
					}
				}).show();
	}

	// 重命名日记的弹出框
	private void showRenameDialog() {
		final EditText editText = new EditText(TabActivity.this);
		AlertDialog.Builder deleteDialog = new AlertDialog.Builder(
				TabActivity.this);
		deleteDialog.setTitle("修改文件").setView(editText);
		deleteDialog
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String diaryName;
						diaryName = editText.getText().toString().trim();
						// 想命名的原文件夹的路径
						File file1 = new File(diary_path + File.separator
								+ clicked_diary_dir);
						// 将原文件夹更改为A，其中路径是必要的。注意
						file1.renameTo(new File(diary_path + File.separator
								+ diaryName));
						// Editor editor = sp.edit();
						// int diaryNum = sp.getInt(clicked_diary_dir, -1);
						// editor.putInt(diaryName, diaryNum);
						// editor.commit();
						LocalSqliteDatabase dba = new LocalSqliteDatabase(
								TabActivity.this);
						dba.open();
						int diary_id = dba.getIdByDiaryName(clicked_diary_dir);
						Database db = new CloudAndLocalDatabase(
								TabActivity.this);
						db.open();
						Diary diary = new Diary();
						diary.name = diaryName;
						diary.create_date = new SimpleDateFormat(
								"yyyyMMdd_HHmmss").format(new Date());
						db.updateDiary(diary_id, diary);
						
						ArrayList<Integer> idlist=dba.getAllLocationIdFromPhoto();
						for (Integer id : idlist) {
							ArrayList<String> img=db.getPhotos(id);
							String photopath=img.get(0);
							String new_phothopath=photopath.replace(clicked_diary_dir, diaryName);
							db.updatePhoto(photopath, new_phothopath);
						}
						db.close();
						// onCreate(null);
						// finish();
						// Intent intent = new Intent(TabActivity.this,
						// TabActivity.class);
						// startActivity(intent);
						DiaryAdapter fileNext = (DiaryAdapter) diary_listview
								.getAdapter();

						showDiarysInCurUser();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// return;
					}
				}).show();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		refreshMessages();
		refreshMoments();
		refreshFriends();
	}

}
