package com.tourdiary.util;

public class Entity {

	private String imageStr;
	private boolean isSelected;
	
	public Entity(String imageUri,boolean isSelected) {
		this.imageStr = imageUri;
		this.isSelected = isSelected;
	}
	
	public String getImageUri() {
		return imageStr;
	}
	public void setImageUri(String imageUri) {
		this.imageStr = imageUri;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	
}
