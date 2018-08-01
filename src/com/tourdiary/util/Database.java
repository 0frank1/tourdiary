package com.tourdiary.util;
import java.util.ArrayList;

import cn.bmob.v3.listener.FindListener;



public interface Database {
	public void open();
	public void close();
	public void insertUser(User user);
	public long insertFriendShip(int user_id1,int user_id2);
	public long insertDiary(Diary diary,int userid);
	public long insertLocation(Location location,int diary_id);
	public void insertPhoto(String photopath,int location_id);
	public long insertMoments(Moment moment,int userid,int diary_id);
	public long insertLike(Like like);
	public long insertComment(Comment comment, int userid, int moment_id);
	
	public ArrayList<User> getUser(int id);
	public ArrayList<User> getUser(String name);
	public ArrayList<FriendShip> getFriendship(int user_id1,int user_id2);
	public ArrayList<FriendShip> getFriendship(int userid);
	public ArrayList<Diary> getDiary(int userid);
	public ArrayList<Location> getLocations(int diary_id);
	public ArrayList<String> getPhotos(int location_id);
	public ArrayList<Moment> getMoments();
	public ArrayList<Moment> getMoments(int userid);
	public ArrayList<Like> getLikes(int moment_id);
	
	public void deleteUser(int userid);
	public void deleteDiary(int diary_id);
	public void deleteLocation(int location_id);
	public void deletePhoto(String photo_path);
	
	public void updateUser(int userid,User user);
	public void updateDiary(int diary_id,Diary diary);
	public void updateLocation(int location_id,Location location);
	public void updatePhoto(String photopath,String new_phothopath);
	
	public boolean existUser(String name);
	public boolean existPhoto(String photopath);
}
