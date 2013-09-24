package net.steveperkins.fitnessjiffy.domain;

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

	private int id;
	private Gender gender;
	private int age;
	private float heightInInches;
	private ActivityLevel activityLevel;
	private String username;
	private String password;
	private String firstName;
	private String lastName;
	
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
	
	public float getCurrentWeight() {
		return 300f;
	}
	
	public float getBmi() {
		return 40f;
	}
	
	public int getMaintenanceCalories() {
		return 2000;
	}
	
	public int getDailyPoints() {
		return 48;
	}
	
}
