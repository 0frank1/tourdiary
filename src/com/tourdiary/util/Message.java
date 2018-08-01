package com.tourdiary.util;

import cn.bmob.v3.BmobObject;

public class Message extends BmobObject{
	public Integer from;
	public Integer to;
	public enum Type {
        Unkown,AddFriend, NormalText,ShareDiary;    
    }
	public Type type;
	public String content;
	public String time;
	public Integer diary;
}
