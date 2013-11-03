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
    public int getCalories() {
        return (int) (food.getCalories() * getRatio());
    }
    public int getFat() {
        return (int) (food.getFat() * getRatio());
    }
    public int getSaturatedFat() {
        return (int) (food.getSaturatedFat() * getRatio());
    }
    public int getSodium() {
        return (int) (food.getSodium() * getRatio());
    }
    public int getCarbs() {
        return (int) (food.getCarbs() * getRatio());
    }
    public int getFiber() {
        return (int) (food.getFiber() * getRatio());
    }
    public int getSugar() {
        return (int) (food.getSugar() * getRatio());
    }
    public int getProtein() {
        return (int) (food.getProtein() * getRatio());
    }
    public int getPoints() {
        return (int) (food.getPoints() * getRatio());
    }

	private float getRatio() {
        if(servingType.equals(food.getDefaultServingType())) {
            // Default serving type was used
            return servingQty / food.getServingTypeQty();
        } else {
            // Serving type needs conversion
            float ouncesInThisServingType = servingType.getValue();
            float ouncesInDefaultServingType = food.getDefaultServingType().getValue();
            return (ouncesInDefaultServingType * food.getServingTypeQty() != 0) ? (ouncesInThisServingType * servingQty) / (ouncesInDefaultServingType * food.getServingTypeQty()) : 0;
        }
    }
}
