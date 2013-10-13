package net.steveperkins.fitnessjiffy.domain;

import java.util.Date;

public class Weight {

	private int id;
	private int userId;
	private Date date;
	private float pounds;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public float getPounds() {
		return pounds;
	}
	public void setPounds(float pounds) {
		this.pounds = pounds;
	}
	
}
