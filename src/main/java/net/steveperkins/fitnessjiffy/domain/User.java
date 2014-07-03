package net.steveperkins.fitnessjiffy.domain;

import com.google.common.base.Optional;

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
        public static Gender fromString(@Nonnull final String s) {
            Gender match = null;
            for (final Gender gender : Gender.values()) {
                if (gender.toString().equalsIgnoreCase(s)) {
                    match = gender;
                }
            }
            return match;
        }

        @Override
        public String toString() {
            return super.toString();
        }

    }

    public enum ActivityLevel {

        SEDENTARY(1.25), LIGHTLY_ACTIVE(1.3), MODERATELY_ACTIVE(1.5), VERY_ACTIVE(1.7), EXTREMELY_ACTIVE(2.0);

        private double value;

        private ActivityLevel(final double value) {
            this.value = value;
        }

        @Nullable
        public static ActivityLevel fromValue(final double value) {
            ActivityLevel match = null;
            for (final ActivityLevel activityLevel : ActivityLevel.values()) {
                if (activityLevel.getValue() == value) {
                    match = activityLevel;
                }
            }
            return match;
        }

        @Nullable
        public static ActivityLevel fromString(@Nonnull final String s) {
            ActivityLevel match = null;
            for (final ActivityLevel activityLevel : ActivityLevel.values()) {
                if (activityLevel.toString().equalsIgnoreCase(s)) {
                    match = activityLevel;
                }
            }
            return match;
        }

        public double getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            final StringBuilder s = new StringBuilder(super.toString().toLowerCase().replace('_', ' '));
            for (int index = 0; index < s.length(); index++) {
                if (index == 0 || s.charAt(index - 1) == ' ') {
                    final String currentCharAsString = Character.toString(s.charAt(index));
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
            @Nullable final UUID id,
            @Nonnull final Gender gender,
            @Nonnull final Date birthdate,
            final double heightInInches,
            @Nonnull final ActivityLevel activityLevel,
            @Nonnull final String email,
            @Nullable final byte[] passwordHash,
            @Nullable final byte[] passwordSalt,
            @Nonnull final String firstName,
            @Nonnull final String lastName,
            @Nonnull final Timestamp createdTime,
            @Nonnull final Timestamp lastUpdatedTime
    ) {
        this.id = Optional.fromNullable(id).or(UUID.randomUUID());
        this.gender = gender.toString();
        this.birthdate = (Date) birthdate.clone();
        this.heightInInches = heightInInches;
        this.activityLevel = activityLevel.getValue();
        this.email = email;
        this.passwordHash = (passwordHash == null) ? null : passwordHash.clone();
        this.passwordSalt = (passwordSalt == null) ? null : passwordSalt.clone();
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

    public void setId(@Nonnull final UUID id) {
        this.id = id;
    }

    @Nonnull
    public Gender getGender() {
        return Gender.fromString(gender);
    }

    public void setGender(@Nonnull final Gender gender) {
        this.gender = gender.toString();
    }

    @Nonnull
    public Date getBirthdate() {
        return (Date) birthdate.clone();
    }

    public void setBirthdate(@Nonnull final Date birthdate) {
        this.birthdate = (Date) birthdate.clone();
    }

    public double getHeightInInches() {
        return heightInInches;
    }

    public void setHeightInInches(final double heightInInches) {
        this.heightInInches = heightInInches;
    }

    @Nonnull
    public ActivityLevel getActivityLevel() {
        return ActivityLevel.fromValue(activityLevel);
    }

    public void setActivityLevel(@Nonnull final ActivityLevel activityLevel) {
        this.activityLevel = activityLevel.getValue();
    }

    @Nonnull
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nonnull final String email) {
        this.email = email;
    }

    @Nullable
    public byte[] getPasswordHash() {
        return (passwordHash == null) ? null : Arrays.copyOf(passwordHash, passwordHash.length);
    }

    public void setPasswordHash(@Nullable final byte[] passwordHash) {
        this.passwordHash = (passwordHash == null) ? null : Arrays.copyOf(passwordHash, passwordHash.length);
    }

    @Nullable
    public byte[] getPasswordSalt() {
        return (passwordSalt == null) ? null : Arrays.copyOf(passwordSalt, passwordSalt.length);
    }

    public void setPasswordSalt(@Nullable final byte[] passwordSalt) {
        this.passwordSalt = (passwordSalt == null) ? null : Arrays.copyOf(passwordSalt, passwordSalt.length);
    }

    @Nonnull
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Nonnull final String firstName) {
        this.firstName = firstName;
    }

    @Nonnull
    public String getLastName() {
        return lastName;
    }

    public void setLastName(@Nonnull final String lastName) {
        this.lastName = lastName;
    }

    @Nonnull
    public Timestamp getCreatedTime() {
        return (Timestamp) createdTime.clone();
    }

    public void setCreatedTime(@Nonnull final Timestamp createdTime) {
        this.createdTime = (Timestamp) createdTime.clone();
    }

    @Nonnull
    public Timestamp getLastUpdatedTime() {
        return (Timestamp) lastUpdatedTime.clone();
    }

    public void setLastUpdatedTime(@Nonnull final Timestamp lastUpdatedTime) {
        this.lastUpdatedTime = (Timestamp) lastUpdatedTime.clone();
    }

    @Nonnull
    public Set<Weight> getWeights() {
        return weights;
    }

    public void setWeights(@Nonnull final Set<Weight> weights) {
        this.weights = weights;
    }

    @Nonnull
    public Set<Food> getFoods() {
        return foods;
    }

    public void setFoods(@Nonnull final Set<Food> foods) {
        this.foods = foods;
    }

    @Nonnull
    public Set<FoodEaten> getFoodsEaten() {
        return foodsEaten;
    }

    public void setFoodsEaten(@Nonnull final Set<FoodEaten> foodsEaten) {
        this.foodsEaten = foodsEaten;
    }

    @Nonnull
    public Set<ExercisePerformed> getExercisesPerformed() {
        return exercisesPerformed;
    }

    public void setExercisesPerformed(@Nonnull final Set<ExercisePerformed> exercisesPerformed) {
        this.exercisesPerformed = exercisesPerformed;
    }

    @Override
    public boolean equals(final Object other) {
        boolean equals = false;
        if (other instanceof User) {
            final User that = (User) other;
            equals = this.getId().equals(that.getId())
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
        return equals;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode()
                + this.getGender().hashCode()
                + this.getBirthdate().hashCode()
                + this.getActivityLevel().hashCode()
                + this.getEmail().hashCode()
                + (this.getPasswordHash() == null ? 0 : Arrays.hashCode(this.getPasswordHash()))
                + (this.getPasswordSalt() == null ? 0 : Arrays.hashCode(this.getPasswordSalt()))
                + this.getFirstName().hashCode()
                + this.getLastName().hashCode()
                + this.getCreatedTime().hashCode()
                + this.getLastUpdatedTime().hashCode();
    }

}
