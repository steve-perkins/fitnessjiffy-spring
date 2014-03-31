package net.steveperkins.fitnessjiffy.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "FITNESSJIFFY_USER")
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

    @Column(name = "BIRTHDATE", nullable = false)
    private Date birthdate;

    @Column(name = "HEIGHT_IN_INCHES", nullable = false)
    private double heightInInches;

    @Column(name = "ACTIVITY_LEVEL", nullable = false)
    private double activityLevel;

    @Column(name = "EMAIL", length = 100, nullable = false)
    private String email;

    @Column(name = "PASSWORD_HASH", length = 64, nullable = true)
    private byte[] passwordHash;

    @Column(name = "PASSWORD_SALT", length = 64, nullable = true)
    private byte[] passwordSalt;

    @Column(name = "FIRST_NAME", length = 20, nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", length = 20, nullable = false)
    private String lastName;

    @Column(name = "CREATED_TIME", nullable = false)
    private Timestamp createdTime;

    @Column(name = "LAST_UPDATED_TIME", nullable = false)
    private Timestamp lastUpdatedTime;

    @OneToMany(mappedBy = "user")
    private Set<Weight> weights = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<Food> foods = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<FoodEaten> foodsEaten = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<ExercisePerformed> exercisesPerformed = new HashSet<>();

    public User(
            UUID id,
            Gender gender,
            Date birthdate,
            double heightInInches,
            ActivityLevel activityLevel,
            String email,
            byte[] passwordHash,
            byte[] passwordSalt,
            String firstName,
            String lastName,
            Timestamp createdTime,
            Timestamp lastUpdatedTime
    ) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.gender = gender.toString();
        this.birthdate = birthdate;
        this.heightInInches = heightInInches;
        this.activityLevel = activityLevel.getValue();
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdTime = createdTime;
        this.lastUpdatedTime = lastUpdatedTime;
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

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    public byte[] getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(byte[] passwordSalt) {
        this.passwordSalt = passwordSalt;
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

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public Timestamp getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Timestamp lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
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

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof User)) {
            return false;
        }
        User that = (User) other;
        return this.getId().equals(that.getId())
                && this.getGender().equals(that.getGender())
                && this.getBirthdate().toString().equals(that.getBirthdate().toString())
                && this.getHeightInInches() == that.getHeightInInches()
                && this.getActivityLevel() == that.getActivityLevel()
                && this.getEmail().equals(that.getEmail())
                && ((this.getPasswordHash() == null && that.getPasswordHash() == null) || this.getPasswordHash().equals(that.getPasswordHash()))
                && ((this.getPasswordSalt() == null && that.getPasswordSalt() == null) || this.getPasswordSalt().equals(that.getPasswordSalt()))
                && this.getFirstName().equals(that.getFirstName())
                && this.getLastName().equals(that.getLastName())
                && this.getCreatedTime().equals(that.getCreatedTime())
                && this.getLastUpdatedTime().equals(that.getLastUpdatedTime());
    }
	
}
