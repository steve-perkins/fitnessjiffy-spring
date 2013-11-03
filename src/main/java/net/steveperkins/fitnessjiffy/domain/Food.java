package net.steveperkins.fitnessjiffy.domain;

public class Food {

	public enum ServingType {
		ounce(1f), cup(8f), pound(16f), pint(16f), tablespoon(0.5f), teaspoon(0.1667f), gram(0.03527f), CUSTOM(0);
		private float value;
		private ServingType(float value) {
			this.value = value;
		}
		public static ServingType fromValue(float value) {
			for(ServingType servingType : ServingType.values()) {
				if(servingType.getValue() == value) {
					return servingType;
				}
			}
			return null;
		}
		public static ServingType fromString(String s) {
			for(ServingType servingType : ServingType.values()) {
				if(servingType.toString().equalsIgnoreCase(s)) {
					return servingType;
				}
			}
			return null;
		}
		public float getValue() {
			return this.value;
		}
		@Override
		public String toString() {
			StringBuilder s = new StringBuilder(super.toString().toLowerCase().replace('_', ' '));
			for(int index = 0; index < s.length(); index++) {
				if(index == 0 || s.charAt(index - 1) == ' ') {
                    String currentCharAsString = s.charAt(index) + "";
					s.replace(index, index + 1, currentCharAsString.toUpperCase());
				}
			}
			return s.toString();
		}
	}
	
	private int id;
	private int userId;
	private String name;
	private ServingType defaultServingType;
	private float servingTypeQty;
	private int calories;
	private float fat;
	private float saturatedFat;
	private float carbs;
	private float fiber;
	private float sugar;
	private float protein;
	private float sodium;
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ServingType getDefaultServingType() {
		return defaultServingType;
	}
	public void setDefaultServingType(ServingType defaultServingType) {
		this.defaultServingType = defaultServingType;
	}
	public float getServingTypeQty() {
		return servingTypeQty;
	}
	public void setServingTypeQty(float servingTypeQty) {
		this.servingTypeQty = servingTypeQty;
	}
	public int getCalories() {
		return calories;
	}
	public void setCalories(int calories) {
		this.calories = calories;
	}
	public float getFat() {
		return fat;
	}
	public void setFat(float fat) {
		this.fat = fat;
	}
	public float getSaturatedFat() {
		return saturatedFat;
	}
	public void setSaturatedFat(float saturatedFat) {
		this.saturatedFat = saturatedFat;
	}
	public float getCarbs() {
		return carbs;
	}
	public void setCarbs(float carbs) {
		this.carbs = carbs;
	}
	public float getFiber() {
		return fiber;
	}
	public void setFiber(float fiber) {
		this.fiber = fiber;
	}
	public float getSugar() {
		return sugar;
	}
	public void setSugar(float sugar) {
		this.sugar = sugar;
	}
	public float getProtein() {
		return protein;
	}
	public void setProtein(float protein) {
		this.protein = protein;
	}
	public float getSodium() {
		return sodium;
	}
	public void setSodium(float sodium) {
		this.sodium = sodium;
	}
    public float getPoints() {
        float fiber = (this.fiber <= 4) ? this.fiber : 4;
        float points = (calories / 50) + (fat / 12) - (fiber / 5);
        return (points > 0) ? points : 0;
    }
	
}
