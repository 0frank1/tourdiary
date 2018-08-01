package com.tourdiary.util;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zh.tourdiary.R;

public class MessageItemAdapter extends BaseAdapter{

	Context ctx;
	List<Message> messages;
	LocalSqliteDatabase db;
	public MessageItemAdapter(Context ctx,List<Message> messages){
		this.ctx=ctx;
		this.messages=messages;
		db=new LocalSqliteDatabase(ctx);
	}
	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		View view;
		if(arg1!=null){
			view=arg1;
		}else{
			view=LayoutInflater.from(ctx).inflate(R.layout.message_item, arg2, false);
			Message m=messages.get(arg0);
			LinearLayout add_friend_layout=(LinearLayout) view.findViewById(R.id.message_add_friend);
			LinearLayout share_layout=(LinearLayout) view.findViewById(R.id.message_share);
			
			TextView add_friend_content=(TextView) view.findViewById(R.id.add_friend_content);
			TextView add_friend_user_name=(TextView) view.findViewById(R.id.add_friend_user_name);
			TextView share_username=(TextView) view.findViewById(R.id.share_username);
			TextView share_content=(TextView) view.findViewById(R.id.share_content);
			
			db.open();
			String username=db.getUser(m.from).get(0).name;
			if(m.type==Message.Type.AddFriend){
				add_friend_layout.setVisibility(View.VISIBLE);
				share_layout.setVisibility(View.INVISIBLE);
				
				add_friend_user_name.setText(username+" 申请添加好友");
				add_friend_content.setText("附加信息: "+m.content);
			}else if (m.type==Message.Type.ShareDiary){
				add_friend_layout.setVisibility(View.INVISIBLE);
				share_layout.setVisibility(View.VISIBLE);
				
				share_username.setText(username);
				share_content.setText(m.content);
			}
			db.close();
		}
		
		return view;
	}
	
}
