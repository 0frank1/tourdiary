package com.tourdiary.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tourdiary.ui.ShareTraceActivity;
import com.tourdiary.ui.TabActivity;
import com.zh.tourdiary.R;

public class MomentsItemAdapter extends BaseAdapter {

	Context ctx;
	List<Moment> moments;
	int userid; // 这个是当前查看这些动态的用户id

	ImageView portrait;
	ImageView like_btn;
	ImageView comment_btn;
	TextView username;
	TextView moment_time;
	TextView content;
	TextView view_times;
	TextView like_textview;
	TextView comment_textview;

	public MomentsItemAdapter(Context ctx, List<Moment> moments, int userid) {
		this.ctx = ctx;
		this.moments = moments;
		this.userid = userid;
	}

	@Override
	public int getCount() {
		return moments.size();
	}

	@Override
	public Object getItem(int arg0) {
		return moments.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View convert, ViewGroup arg2) {
		View view;
		view = LayoutInflater.from(ctx).inflate(R.layout.moments_item,
				arg2, false);
		portrait = (ImageView) view.findViewById(R.id.portrait);
		like_btn = (ImageView) view.findViewById(R.id.like_btn);

		comment_btn = (ImageView) view.findViewById(R.id.comment_btn);
		username = (TextView) view.findViewById(R.id.username);
		moment_time = (TextView) view.findViewById(R.id.moment_time);
		content = (TextView) view.findViewById(R.id.content);
		view_times = (TextView) view.findViewById(R.id.view_times);
		like_textview = (TextView) view.findViewById(R.id.like_textview);
		comment_textview = (TextView) view
				.findViewById(R.id.comment_textview);
		Button reply_btn = (Button) view.findViewById(R.id.reply_btn);
		final EditText reply_edit = (EditText) view
				.findViewById(R.id.reply_edit);

		LocalSqliteDatabase local = new LocalSqliteDatabase(ctx);
		local.open();
		// 设置控件的内容
		final Moment m = moments.get(arg0);
		final Diary diary = local.getDiaryById(m.diary_id);
		User user = local.getUser(m.userid).get(0);
		local.close();
		content.setText(m.content);
		if (diary.id != -1) {
			content.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(ctx,
							ShareTraceActivity.class);
					intent.putExtra("diary_id", m.diary_id);
					ctx.startActivity(intent);
				}
			});
			view_times.setText("日记:" +diary.name
					+ "  浏览" + String.valueOf(m.view_times) + "次");
		}else{
			view_times.setText("(已删除日记)" 
					+ "  浏览" + String.valueOf(m.view_times) + "次");
		}

		moment_time.setText(m.time);
		
		username.setText(user.name);
		refreshLikes(m.id);
		refreshComments(m.id);
		// 设置事件
		like_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LocalSqliteDatabase local = new LocalSqliteDatabase(ctx);
				local.open();
				if (local.existLike(userid, m.id)) {
				} else {
					Like like = new Like();
					like.userid = userid;
					like.moment_id = m.id;
					local.insertLike(like);
					CloudBmobDatabase cloud = new CloudBmobDatabase(ctx);
					cloud.open();
					cloud.insertLike(like);
				}
				local.close();
				refreshLikes(m.id);
			}
		});
		reply_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (reply_edit.getText().toString().isEmpty())
					return;
				CloudAndLocalDatabase db = new CloudAndLocalDatabase(ctx);
				db.open();
				Comment comment = new Comment();
				comment.content = reply_edit.getText().toString();
				db.insertComment(comment, userid, m.id);
				db.close();
				reply_edit.setText("");
				refreshComments(m.id);
			}
		});
		return view;
	}

	private void refreshComments(int moment_id) {
		// 刷新这个动态的评论
		comment_textview.setText("");
		LocalSqliteDatabase local = new LocalSqliteDatabase(ctx);
		local.open();
		ArrayList<Comment> comments = local.getComment(moment_id);
		if (comments.size() == 0) {
			comment_textview.setText("");
		} else {
			for (int i = 0; i != comments.size(); ++i) {
				Comment comment = comments.get(i);
				String username = local.getUser(comment.userid).get(0).name;
				comment_textview.append(username + ": " + comment.content);
				if (i != (comments.size() - 1))
					comment_textview.append("\n");
			}
		}
		local.close();
	}

	void refreshLikes(int moment_id) {
		// 刷新这个动态的赞的人
		LocalSqliteDatabase local = new LocalSqliteDatabase(ctx);
		local.open();
		ArrayList<Like> likes = local.getLikes(moment_id);
		if (likes.size() == 0) {
			like_textview.setVisibility(View.INVISIBLE);
		} else {
			String likestr = new String();
			like_textview.setVisibility(View.VISIBLE);
			for (int i = 0; i != likes.size(); ++i) {
				Like like = likes.get(i);
				String username = local.getUser(like.userid).get(0).name;
				likestr += username;
				if (i != (likes.size() - 1))
					likestr += ",";
			}
			likestr += " 觉得很赞";
			like_textview.setText(likestr);
		}
		if (local.existLike(userid, moment_id)) {
			like_btn.setImageResource(R.drawable.like_pressed);
		} else {
			like_btn.setImageResource(R.drawable.like_normal);
		}
		local.close();
	}

}
