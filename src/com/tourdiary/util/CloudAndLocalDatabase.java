package com.tourdiary.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class CloudAndLocalDatabase implements Database {

	private LocalSqliteDatabase local;
	private CloudBmobDatabase cloud;
	private Context ctx;

	public CloudAndLocalDatabase(Context ctx) {
		local = new LocalSqliteDatabase(ctx);
		cloud = new CloudBmobDatabase(ctx);
		this.ctx = ctx;
	}

	public void synchronize(final SynchronizeListener listener) {
		// 本地数据库与云端同步数据

		// 同步的内容包括:用户,好友关系,日记,地点,图片路径

		// 先同步用户
		final SynchronizeListener comments_listener=new SynchronizeListener(){
			@Override
			public void finished() {
				// 通知同步完成
				listener.finished();
			}
		};
		final SynchronizeListener likes_listener=new SynchronizeListener(){
			@Override
			public void finished() {
				synchronizeComments(comments_listener);	//同步评论至本地
			}
			
		};
		final SynchronizeListener moment_listener=new SynchronizeListener(){
			@Override
			public void finished() {
				synchronizeLikes(likes_listener);
			}

		};
		
		final SynchronizeListener photo_listener=new SynchronizeListener(){
			@Override
			public void finished() {
				synchronizeMoments(moment_listener);	//同步动态
			}

			
		};

		final SynchronizeListener location_listener = new SynchronizeListener() {

			@Override
			public void finished() {
				synchronizePhoto(photo_listener);
			}


		};
		final SynchronizeListener diary_listener = new SynchronizeListener() {

			@Override
			public void finished() {
				// TODO Auto-generated method stub
				synchronizeLocation(location_listener);
			}

		};
		final SynchronizeListener friendship_listener = new SynchronizeListener() {

			@Override
			public void finished() {
				// TODO Auto-generated method stub
				synchronizeDiary(diary_listener);
			}

		};
		SynchronizeListener user_listener = new SynchronizeListener() {

			@Override
			public void finished() {
				// TODO Auto-generated method stub
				// 再同步好友关系
				synchronizeFriendship(friendship_listener);
			}

		};
		synchronizeUser(user_listener);
	}

	void synchronizeUser(final SynchronizeListener user_finished) {
		BmobQuery<User> query = new BmobQuery<User>();
		// 执行查询方法
		query.findObjects(new FindListener<User>() {
			@Override
			public void done(List<User> users, BmobException arg1) {
				Log.d("debug", "开始同步Users ");

				for (int i = 0; i != users.size(); ++i) {
					Log.d("debug", "本地 插入一条用户记录 " + users.get(i).name);
					User u = users.get(i);
					local.insertUser(u.id, u.name, u.password, u.portrait);
				}
				user_finished.finished();
			}
		});
	}

	void synchronizeFriendship(final SynchronizeListener friend_finished) {
		BmobQuery<FriendShip> query2 = new BmobQuery<FriendShip>();
		query2.findObjects(new FindListener<FriendShip>() {
			@Override
			public void done(List<FriendShip> friendships, BmobException arg1) {
				Log.d("debug", "开始同步Friendship ");
				for (int i = 0; i != friendships.size(); ++i) {
					Log.d("debug", "本地 插入一条好友记录 " + friendships.get(i).user1
							+ "," + friendships.get(i).user2);
					FriendShip f = friendships.get(i);
					local.insertFriendShip(f.id, f.user1, f.user2);
				}
				friend_finished.finished();
			}
		});
	}

	void synchronizeDiary(final SynchronizeListener diary_listener) {
		BmobQuery<Diary> query = new BmobQuery<Diary>();
		query.findObjects(new FindListener<Diary>() {

			@Override
			public void done(List<Diary> list, BmobException arg1) {
				// TODO Auto-generated method stub

				Log.d("debug", "开始同步Diary ");
				for (int i = 0; i != list.size(); ++i) {
					Diary diary = list.get(i);
					local.insertDiary(diary.id, diary, diary.userid);
					Log.d("debug", "本地 插入一条日记记录 " + diary.name);
				}
				Log.d("debug", "diary size:" + local.getDiary(1).size());
				diary_listener.finished();
			}
		});
	}

	void synchronizeLocation(final SynchronizeListener location_listener) {
		BmobQuery<Location> query = new BmobQuery<Location>();
		query.setLimit(1000);
		query.findObjects(new FindListener<Location>() {

			@Override
			public void done(List<Location> list, BmobException arg1) {
				Log.d("debug", "开始同步Location ");
				for (int i = 0; i != list.size(); ++i) {
					Location l = list.get(i);
					local.insertLocation(l.id, l, l.diary_id);
					Log.d("debug", "本地 插入一条位置记录 location_id="+l.id+", diary_id="+l.diary_id);
				}

				location_listener.finished();
			}
		});
	}
	

	void synchronizePhoto(final SynchronizeListener photo_listener) {
		BmobQuery<Photo> query = new BmobQuery<Photo>();
		query.findObjects(new FindListener<Photo>() {

			@Override
			public void done(List<Photo> list, BmobException arg1) {
				Log.d("debug", "开始同步Photo ");
				for (int i = 0; i != list.size(); ++i) {
					Photo p = list.get(i);
					local.insertPhoto( p.path, p.location_id);
					BmobFile bmobfile=new BmobFile(p.path,"",p.url);
					File savefile=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+p.path);
					if(savefile.exists()){}
					else{
						bmobfile.download(savefile,new DownloadFileListener(){
							@Override
							public void onProgress(Integer arg0, long arg1) {
								
							}

							@Override
							public void done(String path, BmobException arg1) {
								Log.d("debug","下载图片成功:"+path);
							}
							
						});
					}
					Log.d("debug", "本地插入一条照片记录: "+p.path);
				}
				photo_listener.finished();
			}
		});
	}
	private void synchronizeMoments(final SynchronizeListener moment_listener) {
		BmobQuery<Moment> query = new BmobQuery<Moment>();
		query.findObjects(new FindListener<Moment>() {
			@Override
			public void done(List<Moment> list, BmobException arg1) {
				Log.d("debug", "开始同步Photo ");
				for (int i = 0; i != list.size(); ++i) {
					Moment m=list.get(i);
					local.insertMoments(m.id, m, m.userid, m.diary_id);
					Log.d("debug", "本地插入一条动态: "+m.content);
				}
				moment_listener.finished();
			}
		});
	}

	private void synchronizeLikes(final SynchronizeListener likes_listener) {
		BmobQuery<Like> query = new BmobQuery<Like>();
		query.findObjects(new FindListener<Like>() {
			@Override
			public void done(List<Like> list, BmobException arg1) {
				Log.d("debug", "开始同步点赞");
				for (int i = 0; i != list.size(); ++i) {
					Like like=list.get(i);
					local.insertLike(like.id,like);
					Log.d("debug", "本地插入一条点赞");
				}
				likes_listener.finished();
			}
		});
	}
	
	private void synchronizeComments(final SynchronizeListener comments_listener){
		BmobQuery<Comment> query = new BmobQuery<Comment>();
		query.findObjects(new FindListener<Comment>() {
			@Override
			public void done(List<Comment> list, BmobException arg1) {
				Log.d("debug", "开始同步动态的评论");
				for (int i = 0; i != list.size(); ++i) {
					Comment c=list.get(i);
					local.insertComment(c.id, c, c.userid, c.moment_id);
					Log.d("debug", "本地插入一条评论");
				}
				comments_listener.finished();
			}
		});
	}
	
	@Override
	public void open() {
		// TODO Auto-generated method stub
		local.open();
		cloud.open();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		local.close();
		cloud.close();
	}

	@Override
	public void insertUser(User user) {
		// TODO Auto-generated method stub
		local.insertUser(user);
		ArrayList<User> users = local.getUser(user.name);
		Log.d("debug", "得到刚刚在本地插入的user id");
		if (users.size() != 0) {
			Log.d("debug", "准备向云 传入user数据");
			cloud.insertUser(users.get(0));
		}
	}

	@Override
	public long insertFriendShip(int user_id1, int user_id2) {
		// TODO Auto-generated method stub
		long id = local.insertFriendShip(user_id1, user_id2);
		FriendShip f = new FriendShip();
		f.id = (int) id;
		f.user1 = user_id1;
		f.user2 = user_id2;
		f.save(new SaveListener<String>() {

			@Override
			public void done(String arg0, BmobException arg1) {
				// TODO Auto-generated method stub
				Log.d("debug", "云端存储 friendship 成功");
			}

		});
		return 1;
	}

	@Override
	public long insertDiary(Diary diary, int userid) {
		// TODO Auto-generated method stub
		long id = local.insertDiary(diary, userid);
		diary.id = (int) id;
		cloud.insertDiary(diary, userid);
		return id;
	}

	@Override
	public long insertLocation(Location location, int diary_id) {
		// TODO Auto-generated method stub
		long location_id = local.insertLocation(location, diary_id);
		location.id = (int) location_id;
		cloud.insertLocation(location, diary_id);
		return location_id;
	}

	@Override
	public void insertPhoto(final String photopath, final int location_id) {
		//photopath是相对路径,Environment.getExternalStorageDirectory().getAbsolutePath()+photopath就是绝对路径
		// 先判断本地有没有重复的,如果有,那么就不在云端插入了
		if(local.existPhoto(photopath)){
			return;
		}
		local.insertPhoto(photopath, location_id);
		
		//上传本地照片至服务器
		String absPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+photopath;
		final BmobFile bmobFile = new BmobFile(new File(absPath));
		bmobFile.uploadblock(new UploadFileListener() {
		    @Override
		    public void done(BmobException e) {
		        if(e==null){
		            Log.d("debug","上传文件成功:" + bmobFile.getFileUrl());
		            Photo p = new Photo();
		    		p.location_id = location_id;
		    		p.path = photopath;
		    		p.url=bmobFile.getFileUrl();
		    		p.save(new SaveListener<String>() {
		    			@Override
		    			public void done(String arg0, BmobException arg1) {
		    			}
		    		});
		        }else{
		            Log.d("debug","上传文件失败：" + e.getMessage());
		        }
		    }

		    @Override
		    public void onProgress(Integer value) {
		    }
		});
		
		
	}
	@Override
	public long insertMoments(Moment moment, int userid, int diary_id) {
		long id= local.insertMoments(moment, userid, diary_id);
		moment.id=(int) id;
		cloud.insertMoments(moment, userid, diary_id);
		return id;
	}

	@Override
	public long insertLike(Like like) {
		long id=local.insertLike(like);
		cloud.insertLike(like);
		return id;
	}
	@Override
	public ArrayList<User> getUser(String name) {
		// TODO Auto-generated method stub
		return local.getUser(name);
	}

	@Override
	public ArrayList<User> getUser(int id) {
		// TODO Auto-generated method stub
		return local.getUser(id);
	}

	@Override
	public ArrayList<FriendShip> getFriendship(int user_id1, int user_id2) {
		// TODO Auto-generated method stub
		return local.getFriendship(user_id1, user_id2);
	}

	@Override
	public ArrayList<FriendShip> getFriendship(int userid) {
		// TODO Auto-generated method stub
		return local.getFriendship(userid);
	}

	@Override
	public ArrayList<Diary> getDiary(int userid) {
		// TODO Auto-generated method stub
		return local.getDiary(userid);
	}

	@Override
	public ArrayList<Location> getLocations(int diary_id) {
		// TODO Auto-generated method stub
		return local.getLocations(diary_id);
	}

	@Override
	public ArrayList<String> getPhotos(int location_id) {
		// TODO Auto-generated method stub
		return local.getPhotos(location_id);
	}

	@Override
	public ArrayList<Moment> getMoments() {
		return local.getMoments();
	}

	@Override
	public ArrayList<Moment> getMoments(int userid) {
		return local.getMoments(userid);
	}
	
	@Override
	public void deleteUser(int userid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteDiary(int diary_id) {
		local.deleteDiary(diary_id);
		cloud.deleteDiary(diary_id);
	}

	@Override
	public void deleteLocation(int location_id) {
		local.deleteLocation(location_id);
		cloud.deleteLocation(location_id);
	}

	@Override
	public void deletePhoto(String photo_path) {
		local.deletePhoto(photo_path);
		cloud.deletePhoto(photo_path);
	}

	@Override
	public void updateUser(int userid, User user) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateDiary(int diary_id, Diary diary) {
		local.updateDiary(diary_id, diary);
		cloud.updateDiary(diary_id, diary);
	}

	@Override
	public void updateLocation(int location_id, Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updatePhoto(String photopath, String new_phothopath) {
		local.updatePhoto(photopath, new_phothopath);
		cloud.updatePhoto(photopath, new_phothopath);
	}

	@Override
	public boolean existUser(String name) {
		// TODO Auto-generated method stub
		return local.existUser(name);
	}

	@Override
	public boolean existPhoto(String photopath) {
		// TODO Auto-generated method stub
		return local.existPhoto(photopath);
	}

	@Override
	public ArrayList<Like> getLikes(int moment_id) {
		// TODO Auto-generated method stub
		return local.getLikes(moment_id);
	}

	@Override
	public long insertComment(Comment comment, int userid, int moment_id) {
		long id=local.insertComment(comment, userid, moment_id);
		comment.id=(int) id;
		cloud.insertComment(comment, userid, moment_id);
		return id;
	}

	

}
