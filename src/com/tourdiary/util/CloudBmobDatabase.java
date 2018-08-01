package com.tourdiary.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class CloudBmobDatabase implements Database {

	private Context ctx;

	public CloudBmobDatabase(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub
		Bmob.initialize(ctx, "e5fb69be86fdd690a92ab95d0b4ce5f4");
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertUser(User user) {
		// TODO Auto-generated method stub
		Log.d("debug", "开始向云 传入user数据");
		user.save(new SaveListener<String>() {
			@Override
			public void done(String objectId, BmobException e) {
				if (e == null) {
					Log.d("debug", "添加数据成功，返回objectId为：" + objectId);
				} else {
					Log.d("debug", "添加数据失败 " + e.getMessage());
				}
			}
		});
	}

	@Override
	public long insertFriendShip(int user_id1, int user_id2) {
		// TODO Auto-generated method stub
		FriendShip ship = new FriendShip();
		ship.user1 = user_id1;
		ship.user2 = user_id2;
		ship.save(new SaveListener<String>() {
			@Override
			public void done(String objectId, BmobException e) {
				if (e == null) {
					Log.d("debug", "创建数据成功：" + objectId);
				} else {
					Log.i("bmob",
							"失败：" + e.getMessage() + "," + e.getErrorCode());
				}
			}
		});
		return 1;
	}

	@Override
	public long insertDiary(Diary diary, int userid) {
		// TODO Auto-generated method stub
		diary.userid = userid;
		diary.save(new SaveListener<String>() {
			@Override
			public void done(String arg0, BmobException arg1) {
				// TODO Auto-generated method stub

			}
		});
		return -1;
	}

	@Override
	public long insertLocation(Location location, int diary_id) {
		location.diary_id = diary_id;
		location.save(new SaveListener<String>() {

			@Override
			public void done(String arg0, BmobException arg1) {
				// TODO Auto-generated method stub

			}
		});
		return -1;
	}

	@Override
	public void insertPhoto(String photopath, int location_id) {
		// TODO Auto-generated method stub

	}

	@Override
	public long insertMoments(Moment moment, int userid, int diary_id) {
		moment.save(new SaveListener<String>() {
			@Override
			public void done(String arg0, BmobException arg1) {

			}
		});
		return 0;
	}

	@Override
	public long insertLike(Like like) {
		like.save(new SaveListener<String>() {
			@Override
			public void done(String arg0, BmobException arg1) {
				Log.d("debug", "点赞成功");
			}
		});
		return 0;
	}

	@Override
	public ArrayList<User> getUser(int id) {
		// TODO Auto-generated method stub
		return new ArrayList<User>();
	}

	@Override
	public ArrayList<User> getUser(String name) {
		// TODO Auto-generated method stub
		ArrayList<User> re = new ArrayList<User>();
		BmobQuery<User> query = new BmobQuery<User>();
		query.addWhereEqualTo("name", name);
		query.getObject("a203eba875", new GetUserListener(re));
		return re;
	}

	private class GetUserListener extends QueryListener<User> {

		public ArrayList<User> result;

		GetUserListener(ArrayList<User> re) {
			this.result = re;
		}

		@Override
		public void done(User object, BmobException e) {
			// TODO Auto-generated method stub
			if (e == null) {
				result.add(object);
			} else {
			}
		}

	}

	@Override
	public ArrayList<FriendShip> getFriendship(int user_id1, int user_id2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<FriendShip> getFriendship(int userid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Diary> getDiary(int userid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Location> getLocations(int diary_id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getPhotos(int location_id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteUser(int userid) {
		BmobQuery<User> query = new BmobQuery<User>();
		query.addWhereEqualTo("id", userid);
		query.findObjects(new FindListener<User>() {
			@Override
			public void done(List<User> users, BmobException arg1) {
				users.get(0).delete(new UpdateListener() {
					@Override
					public void done(BmobException arg0) {
						Log.d("debug", "从服务器删除用户成功");
					}
				});
			}

		});
	}

	@Override
	public void deleteDiary(int diary_id) {
		//删除属于这个diary的location
		BmobQuery<Location> location_query = new BmobQuery<Location>();
		location_query.addWhereEqualTo("diary_id", diary_id);
		location_query.findObjects(new FindListener<Location>() {
			@Override
			public void done(List<Location> list, BmobException arg1) {
				for (int i = 0; i != list.size(); ++i) {
					Location l = list.get(i);
					deleteLocation(l.id);
				}
			}
		});
		//再删除数据库diary记录
		BmobQuery<Diary> diary_query = new BmobQuery<Diary>();
		diary_query.addWhereEqualTo("id", diary_id);
		diary_query.findObjects(new FindListener<Diary>() {
			@Override
			public void done(List<Diary> diarys, BmobException arg1) {
				diarys.get(0).delete(new UpdateListener() {
					@Override
					public void done(BmobException arg0) {
						if(arg0!=null){
							Log.d("debug",arg0.getMessage());
						}else
							Log.d("debug", "从服务器删除日记成功");
					}
				});
			}

		});
	}

	@Override
	public void deleteLocation(int location_id) {
		// 先删除属于这个location的photo
		BmobQuery<Photo> photo_query = new BmobQuery<Photo>();
		photo_query.addWhereEqualTo("location_id", location_id);
		photo_query.findObjects(new FindListener<Photo>() {
			@Override
			public void done(List<Photo> list, BmobException arg1) {
				for (int i = 0; i != list.size(); ++i) {
					Photo p = list.get(i);
					BmobFile bmobfile = new BmobFile();
					bmobfile.setUrl(p.url);
					bmobfile.delete(new UpdateListener() {
						@Override
						public void done(BmobException arg0) {
							if(arg0!=null){
								Log.d("debug",arg0.getMessage());
							}
						}

					});
					p.delete(new UpdateListener() {
						@Override
						public void done(BmobException arg0) {
							if(arg0!=null){
								Log.d("debug",arg0.getMessage());
							}
						}
					});
				}
			}
		});
		// 再删除location
		BmobQuery<Location> location_query = new BmobQuery<Location>();
		location_query.addWhereEqualTo("id", location_id);
		location_query.findObjects(new FindListener<Location>() {
			@Override
			public void done(List<Location> locations, BmobException arg1) {
				locations.get(0).delete(new UpdateListener() {
					@Override
					public void done(BmobException arg0) {
						Log.d("debug", "从服务器删除位置成功");
						if(arg0!=null){
							Log.d("debug",arg0.getMessage());
						}
					}
				});
			}
		});
	}

	@Override
	public void deletePhoto(String photo_path) {
		BmobQuery<Photo> query = new BmobQuery<Photo>();
		query.addWhereEqualTo("path", photo_path);
		query.findObjects(new FindListener<Photo>() {
			@Override
			public void done(List<Photo> list, BmobException arg1) {
				Photo p = list.get(0);
				BmobFile bmobfile = new BmobFile();
				bmobfile.setUrl(p.url);
				bmobfile.delete(new UpdateListener() {
					@Override
					public void done(BmobException arg0) {
					}

				});
				p.delete(new UpdateListener() {
					@Override
					public void done(BmobException arg0) {
					}
				});
			}
		});
	}

	@Override
	public void updateUser(int userid, User user) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateDiary(int diary_id, Diary diary) {
		BmobQuery<Diary> query = new BmobQuery<Diary>();
		diary.id = diary_id;
		final Diary to_update = diary;
		query.addWhereEqualTo("id", diary_id);
		query.findObjects(new FindListener<Diary>() {
			@Override
			public void done(List<Diary> diarys, BmobException arg1) {
				Diary diary = diarys.get(0);
				diary.name = to_update.name;
				diary.userid = to_update.userid;
				diary.create_date = to_update.create_date;
				diary.update(new UpdateListener() {
					@Override
					public void done(BmobException arg0) {
						Log.d("debug", "更新日记成功");
					}
				});
			}

		});
	}

	@Override
	public void updateLocation(int location_id, Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updatePhoto(String photopath, String new_phothopath) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean existUser(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public void getMessages(int userid, FindListener<Message> listener) {
		BmobQuery<Message> query = new BmobQuery<Message>();
		query.addWhereEqualTo("to", userid);
		query.findObjects(listener);
	}

	public void sendMessage(Message m) {
		m.save(new SaveListener<String>() {

			@Override
			public void done(String arg0, BmobException arg1) {
				// TODO Auto-generated method stub

			}

		});
	}

	public void getUser(String username, FindListener<User> listener) {
		BmobQuery<User> query = new BmobQuery<User>();
		query.addWhereEqualTo("name", username);
		query.findObjects(listener);
	}

	@Override
	public boolean existPhoto(String photopath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<Moment> getMoments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Moment> getMoments(int userid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Like> getLikes(int moment_id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long insertComment(Comment comment, int userid, int moment_id) {
		comment.userid = userid;
		comment.moment_id = moment_id;
		comment.save(new SaveListener<String>() {
			@Override
			public void done(String arg0, BmobException arg1) {
			}
		});
		return 0;
	}

}
