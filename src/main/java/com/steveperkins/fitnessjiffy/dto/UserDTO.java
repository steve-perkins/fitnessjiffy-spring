package com.steveperkins.fitnessjiffy.dto;

import com.steveperkins.fitnessjiffy.domain.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.sql.Date;
import java.util.UUID;

public final class UserDTO implements Serializable {

    private UUID id;
    private User.Gender gender;
    private Date birthdate;
    private double heightInInches;
    private User.ActivityLevel activityLevel;
    private String email;
    private String firstName;
    private String lastName;
    private String timeZone;
    private double currentWeight;
    private double bmi;
    private int maintenanceCalories;
    private double dailyPoints;

    public UserDTO(
            @Nullable final UUID id,
            @Nonnull final User.Gender gender,
            @Nonnull final Date birthdate,
            final double heightInInches,
            @Nonnull final User.ActivityLevel activityLevel,
            @Nonnull final String email,
            @Nonnull final String firstName,
            @Nonnull final String lastName,
            @Nonnull final String timeZone,
            final double currentWeight,
            final double bmi,
            final int maintenanceCalories,
            final double dailyPoints
    ) {
        this.id = id;
        this.gender = gender;
        this.birthdate = (Date) birthdate.clone();
        this.heightInInches = heightInInches;
        this.activityLevel = activityLevel;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.timeZone = timeZone;
        this.currentWeight = currentWeight;
        this.bmi = bmi;
        this.maintenanceCalories = maintenanceCalories;
        this.dailyPoints = dailyPoints;
    }

    public UserDTO() {
    }

    @Nullable
    public UUID getId() {
        return id;
    }

    public void setId(@Nonnull final UUID id) {
        this.id = id;
    }

    @Nonnull
    public User.Gender getGender() {
        return gender;
    }

    public void setGender(@Nonnull final User.Gender gender) {
        this.gender = gender;
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
    public User.ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(@Nonnull final User.ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    @Nonnull
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nonnull final String email) {
        this.email = email;
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
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(@Nonnull final String timeZone) {
        this.timeZone = timeZone;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(final double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public double getBmi() {
        return bmi;
    }

    public void setBmi(final double bmi) {
        this.bmi = bmi;
    }

    public int getMaintenanceCalories() {
        return maintenanceCalories;
    }

    public void setMaintenanceCalories(final int maintenanceCalories) {
        this.maintenanceCalories = maintenanceCalories;
    }

    public double getDailyPoints() {
        return dailyPoints;
    }

    public void setDailyPoints(final double dailyPoints) {
        this.dailyPoints = dailyPoints;
    }

    @Override
    public boolean equals(final Object other) {
        boolean equals = false;
        if (other instanceof UserDTO) {
            final UserDTO that = (UserDTO) other;
            equals = this.getId().equals(that.getId())
                    && this.getGender().equals(that.getGender())
                    && this.getBirthdate() == that.getBirthdate()
                    && this.getHeightInInches() == that.getHeightInInches()
                    && this.getActivityLevel().equals(that.getActivityLevel())
                    && this.getEmail().equals(that.getEmail())
                    && this.getFirstName().equals(that.getFirstName())
                    && this.getLastName().equals(that.getLastName())
                    && this.getTimeZone().equals(that.getTimeZone())
                    && this.getCurrentWeight() == that.getCurrentWeight()
                    && this.getBmi() == that.getBmi()
                    && this.getMaintenanceCalories() == that.getMaintenanceCalories()
                    && this.getDailyPoints() == that.getDailyPoints();
        }
        return equals;
    }

    @Override
    public int hashCode() {
        final int idHash = (id == null) ? 0 : id.hashCode();
        final int genderHash = (gender == null) ? 0 : gender.hashCode();
        final int birthdateHash = (birthdate == null) ? 0 : birthdate.hashCode();
        final int heightInInchesHash = Double.valueOf(heightInInches).hashCode();
        final int activityLevelHash = (activityLevel == null) ? 0 : activityLevel.hashCode();
        final int emailHash = (email == null) ? 0 : email.hashCode();
        final int firstNameHash = (firstName == null) ? 0 : firstName.hashCode();
        final int lastNameHash = (lastName == null) ? 0 : lastName.hashCode();
        final int currentWeightHash = Double.valueOf(currentWeight).hashCode();
        final int maintenanceCaloriesHash = Integer.valueOf(maintenanceCalories).hashCode();
        final int dailyPointsHash = Double.valueOf(dailyPoints).hashCode();

        return idHash + genderHash + birthdateHash + heightInInchesHash + activityLevelHash + emailHash
                + firstNameHash + lastNameHash + currentWeightHash + maintenanceCaloriesHash + dailyPointsHash;
    }

}
