package net.steveperkins.fitnessjiffy.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "FITNESSJIFFY_USER")
public class User {

    public enum Gender {

        MALE, FEMALE;

        @Nullable
        public static Gender fromString(@Nonnull String s) {
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

        @Nullable
        public static ActivityLevel fromValue(double value) {
            for(ActivityLevel activityLevel : ActivityLevel.values()) {
                if(activityLevel.getValue() == value) {
                    return activityLevel;
                }
            }
            return null;
        }

        @Nullable
        public static ActivityLevel fromString(@Nonnull String s) {
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
            @Nullable UUID id,
            @Nonnull Gender gender,
            @Nonnull Date birthdate,
            double heightInInches,
            @Nonnull ActivityLevel activityLevel,
            @Nonnull String email,
            @Nullable byte[] passwordHash,
            @Nullable byte[] passwordSalt,
            @Nonnull String firstName,
            @Nonnull String lastName,
            @Nonnull Timestamp createdTime,
            @Nonnull Timestamp lastUpdatedTime
    ) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.gender = gender.toString();
        this.birthdate = (Date) birthdate.clone();
        this.heightInInches = heightInInches;
        this.activityLevel = activityLevel.getValue();
        this.email = email;
        this.passwordHash = (passwordHash != null) ? passwordHash.clone() : null;
        this.passwordSalt = (passwordSalt != null) ? passwordSalt.clone() : null;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdTime = (Timestamp) createdTime.clone();
        this.lastUpdatedTime = (Timestamp) lastUpdatedTime.clone();
    }

    public User() {
    }

    @Nonnull
    public UUID getId() {
        return id;
    }

    public void setId(@Nonnull UUID id) {
        this.id = id;
    }

    @Nonnull
    public Gender getGender() {
        return Gender.fromString(gender);
    }

    public void setGender(@Nonnull Gender gender) {
        this.gender = gender.toString();
    }

    @Nonnull
    public Date getBirthdate() {
        return (Date) birthdate.clone();
    }

    public void setBirthdate(@Nonnull Date birthdate) {
        this.birthdate = (Date) birthdate.clone();
    }

    public double getHeightInInches() {
        return heightInInches;
    }

    public void setHeightInInches(double heightInInches) {
        this.heightInInches = heightInInches;
    }

    @Nonnull
    public ActivityLevel getActivityLevel() {
        return ActivityLevel.fromValue(activityLevel);
    }

    public void setActivityLevel(@Nonnull ActivityLevel activityLevel) {
        this.activityLevel = activityLevel.getValue();
    }

    @Nonnull
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nonnull String email) {
        this.email = email;
    }

    @Nullable
    public byte[] getPasswordHash() {
        return (passwordHash != null) ? passwordHash.clone() : null;
    }

    public void setPasswordHash(@Nullable byte[] passwordHash) {
        this.passwordHash = (passwordHash != null) ? passwordHash.clone() : null;
    }

    @Nullable
    public byte[] getPasswordSalt() {
        return (passwordSalt != null) ? passwordSalt.clone() : null;
    }

    public void setPasswordSalt(@Nullable byte[] passwordSalt) {
        this.passwordSalt = (passwordSalt != null) ? passwordSalt.clone() : null;
    }

    @Nonnull
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Nonnull String firstName) {
        this.firstName = firstName;
    }

    @Nonnull
    public String getLastName() {
        return lastName;
    }

    public void setLastName(@Nonnull String lastName) {
        this.lastName = lastName;
    }

    @Nonnull
    public Timestamp getCreatedTime() {
        return (Timestamp) createdTime.clone();
    }

    public void setCreatedTime(@Nonnull Timestamp createdTime) {
        this.createdTime = (Timestamp) createdTime.clone();
    }

    @Nonnull
    public Timestamp getLastUpdatedTime() {
        return (Timestamp) lastUpdatedTime.clone();
    }

    public void setLastUpdatedTime(@Nonnull Timestamp lastUpdatedTime) {
        this.lastUpdatedTime = (Timestamp) lastUpdatedTime.clone();
    }

    @Nonnull
    public Set<Weight> getWeights() {
        return weights;
    }

    public void setWeights(@Nonnull Set<Weight> weights) {
        this.weights = weights;
    }

    @Nonnull
    public Set<Food> getFoods() {
        return foods;
    }

    public void setFoods(@Nonnull Set<Food> foods) {
        this.foods = foods;
    }

    @Nonnull
    public Set<FoodEaten> getFoodsEaten() {
        return foodsEaten;
    }

    public void setFoodsEaten(@Nonnull Set<FoodEaten> foodsEaten) {
        this.foodsEaten = foodsEaten;
    }

    @Nonnull
    public Set<ExercisePerformed> getExercisesPerformed() {
        return exercisesPerformed;
    }

    public void setExercisesPerformed(@Nonnull Set<ExercisePerformed> exercisesPerformed) {
        this.exercisesPerformed = exercisesPerformed;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null || !(other instanceof User)) {
            return false;
        }
        User that = (User) other;
        return this.getId().equals(that.getId())
                && this.getGender().equals(that.getGender())
                && this.getBirthdate().toString().equals(that.getBirthdate().toString())
                && this.getHeightInInches() == that.getHeightInInches()
                && this.getActivityLevel() == that.getActivityLevel()
                && this.getEmail().equals(that.getEmail())
                && ((this.getPasswordHash() == null && that.getPasswordHash() == null) || Arrays.equals(this.getPasswordHash(), that.getPasswordHash()))
                && ((this.getPasswordSalt() == null && that.getPasswordSalt() == null) || Arrays.equals(this.getPasswordSalt(), that.getPasswordSalt()))
                && this.getFirstName().equals(that.getFirstName())
                && this.getLastName().equals(that.getLastName())
                && this.getCreatedTime().equals(that.getCreatedTime())
                && this.getLastUpdatedTime().equals(that.getLastUpdatedTime());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode()
                + this.getGender().hashCode()
                + this.getBirthdate().hashCode()
                + this.getActivityLevel().hashCode()
                + this.getEmail().hashCode()
                + (this.getPasswordHash() != null ? Arrays.hashCode(this.getPasswordHash()) : 0)
                + (this.getPasswordSalt() != null ? Arrays.hashCode(this.getPasswordSalt()) : 0)
                + this.getFirstName().hashCode()
                + this.getLastName().hashCode()
                + this.getCreatedTime().hashCode()
                + this.getLastUpdatedTime().hashCode();
    }
	
}
