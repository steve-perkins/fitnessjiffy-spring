package net.steveperkins.fitnessjiffy.domain;

import java.util.List;

public class User {
	
	public enum Gender {
		MALE, FEMALE;
		public static Gender fromString(String s) {
			for(Gender gender : Gender.values()) {
				if(gender.toString().equalsIgnoreCase(s)) {
					return gender;
				}
			}
			return null;
		}
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}
	
	public enum ActivityLevel {
		SEDENTARY(1.25f), LIGHTLY_ACTIVE(1.3f), MODERATELY_ACTIVE(1.5f), VERY_ACTIVE(1.7f), EXTREMELY_ACTIVE(2.0f);
		private float value;
		private ActivityLevel(float value) {
			this.value = value;
		}
		public static ActivityLevel fromValue(float value) {
			for(ActivityLevel activityLevel : ActivityLevel.values()) {
				if(activityLevel.getValue() == value) {
					return activityLevel;
				}
			}
			return null;
		}
		public static ActivityLevel fromString(String s) {
			for(ActivityLevel activityLevel : ActivityLevel.values()) {
				if(activityLevel.toString().equalsIgnoreCase(s)) {
					return activityLevel;
				}
			}
			return null;
		}
		public float getValue() {
			return this.value;
		}
		@Override
		public String toString() {
			StringBuffer s = new StringBuffer(super.toString().toLowerCase().replace('_', ' '));
			for(int index = 0; index < s.length(); index++) {
				if(index == 0 || s.charAt(index - 1) == ' ') {
					s.replace(index, index + 1, new String(s.charAt(index) + "").toUpperCase());
				}
			}
			return s.toString();
		}
	}
	
	public enum YesNo {
		YES('Y'), NO('N');
		private char value;
		private YesNo(char value) {
			this.value = value;
		}
		public static YesNo fromValue(char value) {
			for(YesNo yesNo : YesNo.values()) {
				if(yesNo.getValue() == value) {
					return yesNo;
				}
			}
			return null;
		}
		public static YesNo fromString(String s) {
			for(YesNo yesNo : YesNo.values()) {
				if(yesNo.toString().equalsIgnoreCase(s)) {
					return yesNo;
				}
			}
			return null;
		}
		public char getValue() {
			return this.value;
		}
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	private int id;
	private Gender gender;
	private int age;
	private float heightInInches;
	private ActivityLevel activityLevel;
	private String username;
	private String password;
	private String firstName;
	private String lastName;
	private YesNo active;
	private List<Weight> weights;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public void setGender(String gender) {
		this.gender = Gender.fromString(gender);
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public float getHeightInInches() {
		return heightInInches;
	}
	public void setHeightInInches(float heightInInches) {
		this.heightInInches = heightInInches;
	}
	public ActivityLevel getActivityLevel() {
		return activityLevel;
	}
	public void setActivityLevel(ActivityLevel activityLevel) {
		this.activityLevel = activityLevel;
	}
	public void setActivityLevel(String activityLevel) {
		this.activityLevel = ActivityLevel.fromString(activityLevel);
	}
	public void setActivityLevel(float activityLevel) {
		this.activityLevel = ActivityLevel.fromValue(activityLevel);
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public YesNo getActive() {
		return active;
	}
	public void setActive(YesNo active) {
		this.active = active;
	}
	public List<Weight> getWeights() {
		return weights;
	}
	public void setWeights(List<Weight> weights) {
		this.weights = weights;
	}
	
	public float getCurrentWeight() {
		return (this.weights != null && this.weights.size() > 0) ? this.weights.get(0).getPounds() : 0;
	}
	
	public float getBmi() {
		return (getCurrentWeight() == 0 || getHeightInInches() == 0) ? 0 : (getCurrentWeight() * 703) / (getHeightInInches() * getHeightInInches());
	}
	
	public int getMaintenanceCalories() {
		if(getGender() == null || getCurrentWeight() == 0 || getHeightInInches() == 0 || getAge() == 0 || getActivityLevel() == null) {
			return 0;
		} else {
			float centimeters = getHeightInInches() * 2.54f;
			float kilograms   = getCurrentWeight() / 2.2f;
			float adjustedWeight = getGender().equals(Gender.FEMALE) ? 655f + (9.6f * kilograms) : 66f + (13.7f * kilograms);
			float adjustedHeight = getGender().equals(Gender.FEMALE) ? 1.7f * centimeters : 5f * centimeters;
			float adjustedAge = getGender().equals(Gender.FEMALE) ? 4.7f * getAge() : 6.8f * getAge();
			
			return (int) ((adjustedWeight + adjustedHeight - adjustedAge) * getActivityLevel().value);
		}
	}
	
	public int getDailyPoints() {
		if(getGender() == null || getAge() == 0 || getCurrentWeight() == 0 || getHeightInInches() == 0 || getActivityLevel() == null) {
			return 0;
		} else {
			// Factor in gender
			int returnValue = getGender().equals(Gender.FEMALE) ? 2 : 8;
			// Factor in age
			if(getAge() <= 26) {
				returnValue += 4;
			} else if(getAge() <= 37) {
				returnValue += 3;
			} else if(getAge() <= 47) {
				returnValue += 2;
			} else if(getAge() <= 58) {
				returnValue += 1;
			}
			// Factor in weight
			returnValue += (getCurrentWeight() / 10f);
			// Factor in height
			if(getHeightInInches() >= 61 && getHeightInInches() <= 70) {
				returnValue += 1;
			} else if(getHeightInInches() > 70) {
				returnValue += 2;
			}
			// Factor in activity level
			if(getActivityLevel().equals(ActivityLevel.EXTREMELY_ACTIVE) || getActivityLevel().equals(ActivityLevel.VERY_ACTIVE)) {
				returnValue += 6;
			} else if(getActivityLevel().equals(ActivityLevel.MODERATELY_ACTIVE)) {
				returnValue += 4;
			} else if(getActivityLevel().equals(ActivityLevel.LIGHTLY_ACTIVE)) {
				returnValue += 2;
			}
			// Factor in daily "flex" points quota 
			returnValue += 5;
			
			return returnValue;
		}
	}
	
}
