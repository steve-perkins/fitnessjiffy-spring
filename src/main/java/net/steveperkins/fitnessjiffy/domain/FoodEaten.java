package net.steveperkins.fitnessjiffy.domain;

import java.util.Date;

import net.steveperkins.fitnessjiffy.domain.Food.ServingType;

public class FoodEaten {

	private int id;
	private int userId;
	private Food food;
	private Date date;
	private ServingType servingType;
	private float servingQty;
	
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
	public Food getFood() {
		return food;
	}
	public void setFood(Food food) {
		this.food = food;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public ServingType getServingType() {
		return servingType;
	}
	public void setServingType(ServingType servingType) {
		this.servingType = servingType;
	}
	public float getServingQty() {
		return servingQty;
	}
	public void setServingQty(float servingQty) {
		this.servingQty = servingQty;
	}
	
}