package net.steveperkins.fitnessjiffy.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "USER")
public class User {

    public enum Gender {
        MALE, FEMALE;
        public static Gender fromString(String s) {
            for(Gender gender : Gender.values()) {
                if(s != null && gender.toString().equalsIgnoreCase(s)) {
                    return gender;
                }
            }
            return null;
        }
        @Override
        public String toString() {
            return super.toString();
        }
    }

    public enum ActivityLevel {
        SEDENTARY(1.25), LIGHTLY_ACTIVE(1.3), MODERATELY_ACTIVE(1.5), VERY_ACTIVE(1.7), EXTREMELY_ACTIVE(2.0);
        private double value;
        private ActivityLevel(double value) {
            this.value = value;
        }
        public static ActivityLevel fromValue(double value) {
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
        public double getValue() {
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

    @Id
    @Column(columnDefinition = "BYTEA", length = 16)
    private UUID id;

    @Column(name = "GENDER", length = 6, nullable = false)
    private String gender;

    @Column(name = "AGE", nullable = false)
    private int age;

    @Column(name = "HEIGHT_IN_INCHES", nullable = false)
    private double heightInInches;

    @Column(name = "ACTIVITY_LEVEL", nullable = false)
    private double activityLevel;

    @Column(name = "USERNAME", length = 50, nullable = false)
    private String username;

    @Column(name = "PASSWORD", length = 50, nullable = false)
    private String password;

    @Column(name = "FIRST_NAME", length = 20, nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", length = 20, nullable = false)
    private String lastName;

    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "user")
    private Set<Weight> weights = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<Food> foods = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<FoodEaten> foodsEaten = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<ExercisePerformed> exercisesPerformed = new HashSet<>();

    public User(UUID id, Gender gender, int age, double heightInInches, ActivityLevel activityLevel,
                String username, String password, String firstName, String lastName, boolean isActive) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.gender = gender.toString();
        this.age = age;
        this.heightInInches = heightInInches;
        this.activityLevel = activityLevel.getValue();
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
    }

    public User() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Gender getGender() {
        return Gender.fromString(gender);
    }

    public void setGender(Gender gender) {
        this.gender = gender.toString();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getHeightInInches() {
        return heightInInches;
    }

    public void setHeightInInches(double heightInInches) {
        this.heightInInches = heightInInches;
    }

    public ActivityLevel getActivityLevel() {
        return ActivityLevel.fromValue(activityLevel);
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel.getValue();
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Set<Weight> getWeights() {
        return weights;
    }

    public void setWeights(Set<Weight> weights) {
        this.weights = weights;
    }

    public Set<Food> getFoods() {
        return foods;
    }

    public void setFoods(Set<Food> foods) {
        this.foods = foods;
    }

    public Set<FoodEaten> getFoodsEaten() {
        return foodsEaten;
    }

    public void setFoodsEaten(Set<FoodEaten> foodsEaten) {
        this.foodsEaten = foodsEaten;
    }

    public Set<ExercisePerformed> getExercisesPerformed() {
        return exercisesPerformed;
    }

    public void setExercisesPerformed(Set<ExercisePerformed> exercisesPerformed) {
        this.exercisesPerformed = exercisesPerformed;
    }

	
	public double getCurrentWeight() {
		return (this.weights != null && this.weights.size() > 0) ? this.weights.iterator().next().getPounds() : 0;
	}
	
	public double getBmi() {
		return (getCurrentWeight() == 0 || getHeightInInches() == 0) ? 0 : (getCurrentWeight() * 703) / (getHeightInInches() * getHeightInInches());
	}
	
	public int getMaintenanceCalories() {
		if(getGender() == null || getCurrentWeight() == 0 || getHeightInInches() == 0 || getAge() == 0 || getActivityLevel() == null) {
			return 0;
		} else {
			double centimeters = getHeightInInches() * 2.54f;
			double kilograms   = getCurrentWeight() / 2.2f;
			double adjustedWeight = getGender().equals(Gender.FEMALE) ? 655f + (9.6f * kilograms) : 66f + (13.7f * kilograms);
			double adjustedHeight = getGender().equals(Gender.FEMALE) ? 1.7f * centimeters : 5f * centimeters;
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

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof User)) {
            return false;
        }
        User that = (User) other;
        return this.getId().equals(that.getId())
                && this.getGender().equals(that.getGender())
                && this.getAge() == that.getAge()
                && this.getHeightInInches() == that.getHeightInInches()
                && this.getActivityLevel().equals(that.getActivityLevel())
                && this.getUsername().equals(that.getUsername())
                && this.getPassword().equals(that.getPassword())
                && this.getFirstName().equals(that.getFirstName())
                && this.getLastName().equals(that.getLastName())
                && this.isActive() == that.isActive();
    }
	
}
