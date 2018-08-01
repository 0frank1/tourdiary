package com.tourdiary.util;

import cn.bmob.v3.BmobObject;

public class Moment extends BmobObject{
	public Integer id;
	public Integer userid;
	public Integer diary_id;
	public String content;		//内容,文字
	public String time;		//发表日期
	public Integer view_times;	//浏览次数
}
