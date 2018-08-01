package com.tourdiary.util;

import cn.bmob.v3.BmobObject;

public class Photo extends BmobObject{
	public String path;
	public Integer location_id;
	public String url;	//这是服务器中存储这张图片的url
}
