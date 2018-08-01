package com.tourdiary.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.listener.FindListener;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;



public class LocalSqliteDatabase implements Database{

	static final String TABLE_USER_CREATE = "create table user ( id integer primary key autoincrement ,name varchar(255) unique not null, password text not null ,portrait text);";
	
	static final String TABLE_DIARY_CREATE = "create table diary ( id integer primary key autoincrement ,name tinytext not null , create_date text  , userid integer,  foreign key (id) references user(id));";
	
	static final String TABLE_LOCATION_CREATE = "create table location (id integer primary key autoincrement ,diary_id int ,description text  , latitude double not null , longitude double not null , foreign key (diary_id) references diary(id) );";
	
	static final String TABLE_FRIENDSHIP_CREATE = "create table friendship (id integer primary key autoincrement ,user1 int ,user2 int , foreign key  (user1) references user(id), foreign key  (user2) references user(id) );";
	
	static final String TABLE_PHOTO_CREATE = "create table photo (path varchar(255) primary key , location_id int , foreign key (location_id) references location(id) );";
	
	static final String TABLE_MOMENTS_CREATE="create table moments( id integer primary key autoincrement, userid integer, diary_id integer, content text, time text, view_times integer, foreign key (userid) references user(id), foreign key (diary_id) references diary(id));";
	
	static final String TABLE_LIKES_CREATE="create table likes( id integer primary key autoincrement, userid integer, moment_id integer, foreign key (userid) references user(id), foreign key (moment_id) references moments(id));";
	
	static final String TABLE_COMMENTS_CREATE="create table comments( id integer primary key autoincrement, userid integer, moment_id integer, content text);";
	
	static final String DATABASE_NAME="datas";
	private static final int DATABASE_VERSION = 1;
	private String diary_path = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator + "zheng";
	static final String TAG = "DBAdapter";  
	
	final Context context; 

	DatabaseHelper DBHelper;
	SQLiteDatabase db;
	
	public LocalSqliteDatabase(Context cxt){
		this.context=cxt;
		DBHelper=new DatabaseHelper(cxt);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			try {
				db.execSQL(TABLE_USER_CREATE);
				db.execSQL(TABLE_DIARY_CREATE);
				db.execSQL(TABLE_LOCATION_CREATE);
				db.execSQL(TABLE_FRIENDSHIP_CREATE);
				db.execSQL(TABLE_PHOTO_CREATE);
				db.execSQL(TABLE_MOMENTS_CREATE);
				db.execSQL(TABLE_LIKES_CREATE);
				db.execSQL(TABLE_COMMENTS_CREATE);
				Log.d("debug","create database success");
			} catch (SQLException e) {
				Log.d("debug","create database error");
				Log.d("debug",e.getMessage());
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			Log.wtf(TAG, "Upgrading database from version " + oldVersion
					+ "to " + newVersion + ", which will destroy all old data");
			db.execSQL("drop table if exists comments,likes,moments,photo,friendship,location,diary,user;");
			onCreate(db);
		}
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub
		db = DBHelper.getWritableDatabase();  
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		DBHelper.close();  
	}

	public void insertUser(User user) {
		// TODO Auto-generated method stub
		ContentValues initialValues = new ContentValues();  
		initialValues.put("name", user.name);  
		initialValues.put("password", user.password);
		initialValues.put("portrait", user.portrait);
		db.insert("user", null, initialValues);
	}
	
	public void insertUser(int id,String name,String password,String portrait){
		ContentValues initialValues = new ContentValues();  
		initialValues.put("id", id);  
		initialValues.put("name", name);
		initialValues.put("password", password);
		initialValues.put("portrait", portrait);
		db.insert("user", null, initialValues);
	}

	@Override
	public long insertFriendShip(int user_id1, int user_id2) {
		// TODO Auto-generated method stub
		ContentValues initialValues = new ContentValues();  
		initialValues.put("user1", user_id1);  
		initialValues.put("user2", user_id2);
		return db.insert("friendship", null, initialValues);
	}
	
	public void insertFriendShip(int id,int user_id1, int user_id2) {
		// TODO Auto-generated method stub
		ContentValues initialValues = new ContentValues();  
		initialValues.put("user1", user_id1);  
		initialValues.put("user2", user_id2);
		initialValues.put("id",id);
		db.insert("friendship", null, initialValues);
	}

	@Override
	public long insertDiary(Diary diary,int userid) {
		// TODO Auto-generated method stub
		ContentValues initialValues = new ContentValues();  
		initialValues.put("name", diary.name);  
		initialValues.put("create_date", diary.create_date);
		initialValues.put("userid", userid);
		
		diary.userid=userid;
		return db.insert("diary", null, initialValues);
	}
	
	public long insertDiary(int id,Diary diary,int userid) {
		// TODO Auto-generated method stub
		ContentValues initialValues = new ContentValues();  
		initialValues.put("id",id);
		initialValues.put("name", diary.name);  
		initialValues.put("create_date", diary.create_date);
		initialValues.put("userid", userid);
		
		diary.userid=userid;
		File f = new File(diary_path + File.separator
				+ diary.name);
		if(f.exists()==false)
			f.mkdir();
		return db.insert("diary", null, initialValues);
	}

	@Override
	public long insertLocation(Location location,int diary_id) {
		// TODO Auto-generated method stub
		ContentValues initialValues = new ContentValues();  
		initialValues.put("diary_id", diary_id);  
		initialValues.put("description", location.description);
		initialValues.put("latitude", location.latitude);
		initialValues.put("longitude", location.longitude);
		
		location.diary_id=diary_id;
		return db.insert("location", null, initialValues);
	}
	
	public long insertLocation(int id,Location location,int diary_id) {
		// TODO Auto-generated method stub
		ContentValues initialValues = new ContentValues();  
		initialValues.put("id", id);
		initialValues.put("diary_id", diary_id);  
		initialValues.put("description", location.description);
		initialValues.put("latitude", location.latitude);
		initialValues.put("longitude", location.longitude);
		
		location.diary_id=diary_id;
		return db.insert("location", null, initialValues);
	}

	@Override
	public void insertPhoto(String photopath,int location_id) {
		// TODO Auto-generated method stub
		ContentValues initialValues = new ContentValues();  
		initialValues.put("path", photopath); 
		initialValues.put("location_id", location_id );
		db.insert("photo", null, initialValues);
	}

	@Override
	public long insertMoments(Moment moment, int userid, int diary_id) {
		moment.userid=userid;
		moment.diary_id=diary_id;
		ContentValues initialValues = new ContentValues();  
		initialValues.put("userid", userid); 
		initialValues.put("diary_id", diary_id );
		initialValues.put("content", moment.content );
		initialValues.put("time", moment.time );
		initialValues.put("view_times", moment.view_times );
		return db.insert("moments", null, initialValues);
	}
	public long insertMoments(int id,Moment moment, int userid, int diary_id) {
		moment.userid=userid;
		moment.diary_id=diary_id;
		ContentValues initialValues = new ContentValues();  
		initialValues.put("id", id);
		initialValues.put("userid", userid); 
		initialValues.put("diary_id", diary_id );
		initialValues.put("content", moment.content );
		initialValues.put("time", moment.time );
		initialValues.put("view_times", moment.view_times );
		return db.insert("moments", null, initialValues);
	}

	@Override
	public long insertLike(Like like) {
		ContentValues initialValues = new ContentValues();  
		initialValues.put("userid", like.userid); 
		initialValues.put("moment_id", like.moment_id );
		
		long id=db.insert("likes", null, initialValues);
		like.id=(int) id;
		return id;
	}
	public long insertLike(int id,Like like) {
		ContentValues initialValues = new ContentValues();  
		initialValues.put("id", like.id); 
		initialValues.put("userid", like.userid); 
		initialValues.put("moment_id", like.moment_id );
		
		return db.insert("likes", null, initialValues);
	}

	@Override
	public long insertComment(Comment comment, int userid, int moment_id) {
		ContentValues initialValues = new ContentValues();  
		initialValues.put("userid", userid); 
		initialValues.put("moment_id", moment_id );
		initialValues.put("content", comment.content );
		
		return db.insert("comments", null, initialValues);
	}
	
	public long insertComment(int id,Comment comment, int userid, int moment_id) {
		ContentValues initialValues = new ContentValues();   
		initialValues.put("id", id); 
		initialValues.put("userid", userid); 
		initialValues.put("moment_id", moment_id );
		initialValues.put("content", comment.content );
		
		return db.insert("comments", null, initialValues);
	}
	
	@Override
	public ArrayList<User> getUser(int id) {
		// TODO Auto-generated method stub
		ArrayList<User> re=new ArrayList<User>();
		User user = new User();
		Cursor cursor=db.query("user", new String[]{"id","name","password","portrait"}, "id="+id, null, null, null, null);
		if(cursor.moveToFirst()){
			user.id=cursor.getInt(cursor.getColumnIndex("id"));
			user.name=cursor.getString(cursor.getColumnIndex("name"));
			user.password=cursor.getString(cursor.getColumnIndex("password"));
			user.portrait=cursor.getString(cursor.getColumnIndex("portrait"));
			re.add(user);
		}else
		{}
		cursor.close();
		return re;
	}
	
	@Override
	public ArrayList<User> getUser(String name) {
		// TODO Auto-generated method stub
		ArrayList<User> re=new ArrayList<User>();
		User user = new User();
		Cursor cursor=db.query("user", new String[]{"id","name","password","portrait"}, "name='"+name+"'", null, null, null, null);
		if(cursor.moveToLast()){
			user.id=cursor.getInt(cursor.getColumnIndex("id"));
			user.name=cursor.getString(cursor.getColumnIndex("name"));
			user.password=cursor.getString(cursor.getColumnIndex("password"));
			user.portrait=cursor.getString(cursor.getColumnIndex("portrait"));
			re.add(user);
		}else
		{}
		cursor.close();
		return re;
	}

	@Override
	public ArrayList<FriendShip> getFriendship(int user_id1, int user_id2) {
		// TODO Auto-generated method stub
		ArrayList<FriendShip> re=new ArrayList<FriendShip>();
		FriendShip friendship = new FriendShip();
		Cursor cursor=db.query("friendship", 
				new String[]{"id","user1","user2"}, 
				"user1="+String.valueOf(user_id1)+" and user2="+String.valueOf(user_id2),null,null, null, null);
		if(cursor.moveToLast()){
			friendship.id=cursor.getInt(cursor.getColumnIndex("id"));
			friendship.user1=cursor.getInt(cursor.getColumnIndex("user1"));
			friendship.user2=cursor.getInt(cursor.getColumnIndex("user2"));
			re.add(friendship);
		}else{
			Cursor cursor2=db.query("friendship", 
					new String[]{"id","user1","user2"}, 
					"user1="+String.valueOf(user_id2)+" and user2="+String.valueOf(user_id1),null, null, null, null);
			if(cursor2.moveToFirst()){
				friendship.id=cursor2.getInt(cursor2.getColumnIndex("id"));
				friendship.user1=cursor2.getInt(cursor2.getColumnIndex("user2"));
				friendship.user2=cursor2.getInt(cursor2.getColumnIndex("user1"));
				re.add(friendship);
			}else{
				
			}
			cursor2.close();
		}
		cursor.close();
			
		return re;
	}
	
	@Override
	public ArrayList<FriendShip> getFriendship(int userid) {
		// TODO Auto-generated method stub
		ArrayList<FriendShip> re=new ArrayList<FriendShip>();
		FriendShip friendship;
		Cursor cursor=db.rawQuery("select * from friendship where user1=?", new String []{String.valueOf(userid)});
		while(cursor.moveToNext()){
			friendship=  new FriendShip();
			friendship.id=cursor.getInt(cursor.getColumnIndex("id"));
			friendship.user1=cursor.getInt(cursor.getColumnIndex("user1"));
			friendship.user2=cursor.getInt(cursor.getColumnIndex("user2"));
			re.add(friendship);
		}
		Cursor cursor2=db.rawQuery("select * from friendship where user2=?",new String []{String.valueOf(userid)});
		while(cursor2.moveToNext()){
			friendship=  new FriendShip();
			friendship.id=cursor2.getInt(cursor2.getColumnIndex("id"));
			friendship.user1=cursor2.getInt(cursor2.getColumnIndex("user2"));
			friendship.user2=cursor2.getInt(cursor2.getColumnIndex("user1"));
			re.add(friendship);
		}
		cursor2.close();
		cursor.close();
			
		return re;
	}

	@Override
	public ArrayList<Diary> getDiary(int userid) {
		// TODO Auto-generated method stub
		ArrayList<Diary> diarys = new ArrayList<Diary>();  
        Cursor c = db.rawQuery("select * from diary where userid="+String.valueOf(userid), null) ;
        while (c.moveToNext()) {  
        	Diary diary=new Diary();
        	diary.id=c.getInt(c.getColumnIndex("id"));
        	diary.name=c.getString(c.getColumnIndex("name"));
        	diary.create_date=c.getString(c.getColumnIndex("create_date"));
        	diarys.add(diary);  
        }  
        c.close();  
        return diarys;
	}
	
	public Diary getDiaryById(int diary_id) {
		// TODO Auto-generated method stub
		Diary diary = new Diary();  
        Cursor c = db.rawQuery("select * from diary where id="+String.valueOf(diary_id), null) ;
        if (c.moveToNext()) {  
        	diary.id=c.getInt(c.getColumnIndex("id"));
        	diary.name=c.getString(c.getColumnIndex("name"));
        	diary.create_date=c.getString(c.getColumnIndex("create_date"));
        }else{
        	diary.id=-1;
        }
        c.close();  
        return diary;
	}

	@Override
	public ArrayList<Location> getLocations(int diary_id) {
		// TODO Auto-generated method stub
		ArrayList<Location> locations=new ArrayList<Location>();
		Cursor c = db.rawQuery("select * from location where diary_id="+String.valueOf(diary_id), null) ;
        while (c.moveToNext()) {  
        	Location lo=new Location();
        	lo.id=c.getInt(c.getColumnIndex("id"));
        	lo.description=c.getString(c.getColumnIndex("description"));
        	lo.latitude=c.getDouble(c.getColumnIndex("latitude"));
        	lo.longitude=c.getDouble(c.getColumnIndex("longitude"));
        	locations.add(lo);
        }  
        c.close();  
		return locations;
	}

	public Location getLocationById(int id) {
		// TODO Auto-generated method stub
		Cursor c = db.rawQuery("select latitude,longitude from location where id="+String.valueOf(id), null) ;
		c.moveToNext(); 
        Location lo=new Location();
        lo.latitude=c.getDouble(c.getColumnIndex("latitude"));
        lo.longitude=c.getDouble(c.getColumnIndex("longitude")); 
        c.close();  
		return lo;
	}
	
	public Integer getIdByDiaryName(String name) {
		// TODO Auto-generated method stub
		Cursor c = db.rawQuery("select id from diary where name='"+name+"'"
				, null) ;
		Integer id=-1;
		while (c.moveToNext()) {  
		    id=c.getInt(c.getColumnIndex("id"));
		}
        c.close();  
		return id;
	}
	
	public int getIdByLocation(double latitude,double longitude) {
		// TODO Auto-generated method stub
		Cursor c = db.rawQuery("select id from location where latitude="+String.valueOf(latitude)+
				"and longitude="+String.valueOf(longitude), null) ;
		c.moveToNext(); 
        int id=c.getInt(c.getColumnIndex("id"));
        c.close();  
		return id;
	}
	
	public int getMaxIdFromLocation() {
		// TODO Auto-generated method stub
		int id = 0;
		Cursor c = db.rawQuery("select id from location order by id desc", null) ; 
		c.moveToNext(); 
        id=c.getInt(0);
        c.close();  
		return id;
	}
	
	public ArrayList<Integer> getAllLocationIdFromPhoto() {
		// TODO Auto-generated method stub
		ArrayList<Integer> idlist=new ArrayList<Integer>();
		Cursor c = db.rawQuery("select location_id from photo", null) ;
        while (c.moveToNext()) { 
        	int l=c.getInt(c.getColumnIndex("location_id"));
        	idlist.add(l);
        }  
        c.close();  
		return idlist;
	}
	
	public boolean isLocationIdFromDiary(int location_id,int diaryId){
		int diary_id=-1;
		Cursor c = db.rawQuery("select diary_id from location where id="+location_id, null) ;
        while (c.moveToNext()) { 
        	diary_id=c.getInt(c.getColumnIndex("diary_id"));
        }  
        c.close();
        if(diary_id==diaryId)
		return true;
        return false;
	}
	
	@Override
	public ArrayList<String> getPhotos(int location_id) {
		ArrayList<String> paths=new ArrayList<String>();
		Cursor c = db.rawQuery("select * from photo where location_id="+String.valueOf(location_id), null) ;
        while (c.moveToNext()) {  
        	String p=new String();
        	p=c.getString(c.getColumnIndex("path"));
        	paths.add(p);
        }  
        c.close();  
		return paths;
	}
	
	@Override
	public ArrayList<Moment> getMoments() {
		ArrayList<Moment> results=new ArrayList<Moment>();
		Cursor c=db.rawQuery("select * from moments", null);
		while(c.moveToNext()){
			Moment m=new Moment();
			m.id=c.getInt(c.getColumnIndex("id"));
			m.userid=c.getInt(c.getColumnIndex("userid"));
			m.diary_id=c.getInt(c.getColumnIndex("diary_id"));
			m.content=c.getString(c.getColumnIndex("content"));
			m.time=c.getString(c.getColumnIndex("time"));
			m.view_times=c.getInt(c.getColumnIndex("view_times"));
			results.add(m);
		}
		return results;
	}

	@Override
	public ArrayList<Moment> getMoments(int userid) {
		ArrayList<Moment> results=new ArrayList<Moment>();
		Cursor c=db.rawQuery("select * from moments where userid=?",new String[]{String.valueOf(userid)});
		while(c.moveToNext()){
			Moment m=new Moment();
			m.id=c.getInt(c.getColumnIndex("id"));
			m.userid=c.getInt(c.getColumnIndex("userid"));
			m.diary_id=c.getInt(c.getColumnIndex("diary_id"));
			m.content=c.getString(c.getColumnIndex("content"));
			m.time=c.getString(c.getColumnIndex("time"));
			m.view_times=c.getInt(c.getColumnIndex("view_times"));
			results.add(m);
		}
		return results;
	}
	
	public ArrayList<Comment> getComment(int moment_id) {
		ArrayList<Comment> results=new ArrayList<Comment>();
		Cursor c=db.rawQuery("select * from comments where moment_id=?",new String[]{String.valueOf(moment_id)});
		while(c.moveToNext()){
			Comment comment=new Comment();
			comment.id=c.getInt(c.getColumnIndex("id"));
			comment.userid=c.getInt(c.getColumnIndex("userid"));
			comment.moment_id=moment_id;
			comment.content=c.getString(c.getColumnIndex("content"));
			results.add(comment);
		}
		return results;
	}

	@Override
	public void deleteUser(int userid) {
		// TODO Auto-generated method stub
		db.execSQL("delete from user where id="+String.valueOf(userid));
	}

	@Override
	public void deleteDiary(int diary_id) {
		//删除本地文件夹
		Cursor c=db.rawQuery("select name from diary where id=?", new String[]{String.valueOf(diary_id)});
		String diary_name;
		if(c.moveToNext()){
			diary_name=c.getString(0);
			File diary_dir=new File(Environment.getExternalStorageDirectory().toString()+"/zheng/"+diary_name);
			File [] files=diary_dir.listFiles();
			for(int i=0;i!=files.length;++i){
				File file=files[i];
				file.delete();
			}
			diary_dir.delete();
		}
		
		//删除属于这个diary的location
		c=db.rawQuery("select id from location where diary_id=?", new String[] {String.valueOf(diary_id)});
		while(c.moveToNext()){
			int location_id=c.getInt(0);
			this.deleteLocation(location_id);
		}
		//再删除数据库diary记录
		db.execSQL("delete from diary where id="+String.valueOf(diary_id));
	}

	@Override
	public void deleteLocation(int location_id) {
		//先删除属于这个location的photo
		Cursor c=db.rawQuery("select path from photo where location_id=?", new String [] {String.valueOf(location_id)});
		while(c.moveToNext()){
			String photo_path=c.getString(0);
			this.deletePhoto(photo_path);
		}
		//再删除location
		db.execSQL("delete from location where id="+String.valueOf(location_id));
	}

	@Override
	public void deletePhoto(String photo_path) {
		// TODO Auto-generated method stub
		db.execSQL("delete from photo where path='"+photo_path+"'");
		
		//本地照片不删除,由deleteDiary负责把整个文件夹删除
		
//		File file=new File(Environment.getExternalStorageDirectory().toString()+"/"+photo_path);
//		//删除本地照片
//		file.delete();
	}

	@Override
	public void updateUser(int userid, User user) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();  
		values.put("name", user.name);  
		values.put("password", user.password);
		values.put("portrait", user.portrait);
		db.update("user", values, "id="+String.valueOf(userid), null);
	}

	@Override
	public void updateDiary(int diary_id, Diary diary) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();  
		values.put("name", diary.name);  
		values.put("create_date", diary.create_date);
		db.update("diary", values, "id="+String.valueOf(diary_id), null);
	}

	@Override
	public void updateLocation(int location_id, Location location) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();  
		values.put("description", location.description);  
		values.put("latitude", location.latitude);
		values.put("longitude", location.longitude);
		db.update("location", values, "id="+String.valueOf(location_id), null);
	}

	@Override
	public void updatePhoto(String photopath, String new_phothopath) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();  
		values.put("path", new_phothopath);  
		db.update("photo", values, "path='"+photopath+"'", null);
	}



	@Override
	public boolean existUser(String name) {
		// TODO Auto-generated method stub
		Cursor c=db.rawQuery("select * from user where name='"+name+"'", null);
		if(c.moveToFirst()){
			return true;
		}
		else
			return false;
	}

	@Override
	public boolean existPhoto(String photopath) {
		Cursor c=db.rawQuery("select * from photo where path='"+photopath+"'", null);
		if(c.moveToFirst()){
			return true;
		}
		return false;
	}

	public boolean existLike(int userid,int momentid){
		Cursor c=db.rawQuery("select * from likes where userid=? and moment_id= ?", new String [] {String.valueOf(userid),String.valueOf(momentid)});
		if(c.moveToNext())
			return true;
		return false;
		
	}

	@Override
	public ArrayList<Like> getLikes(int moment_id) {
		ArrayList<Like> likes=new ArrayList<Like>();
		Cursor c=db.rawQuery("select * from likes where moment_id = ?", new String [] {String.valueOf(moment_id)});
		while(c.moveToNext()){
			Like like=new Like();
			like.id=c.getInt(c.getColumnIndex("id"));
			like.userid=c.getInt(c.getColumnIndex("userid"));
			like.moment_id=c.getInt(c.getColumnIndex("moment_id"));
			likes.add(like);
		}
		return likes;
	}

	

}
