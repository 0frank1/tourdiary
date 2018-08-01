package com.tourdiary.ui;

import java.io.Serializable;

public class MarkerInfoUtil implements Serializable {
	private String imgStr;//图片

	public String getImgStr() {
		return imgStr;
	}
	public void setImgStr(String imgStr) {
		this.imgStr = imgStr;
	}
	//构造方法
    public MarkerInfoUtil() {}
    public MarkerInfoUtil(String imgStr) {
        super();
        this.imgStr = imgStr;
    }

}
