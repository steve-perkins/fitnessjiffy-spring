package net.steveperkins.fitnessjiffy.dto;

import net.steveperkins.fitnessjiffy.domain.User;

import java.sql.Date;
import java.util.UUID;

public class UserDTO {

    private UUID id;
    private User.Gender gender;
    private Date birthdate;
    private double heightInInches;
    private User.ActivityLevel activityLevel;
    private String email;
    private String firstName;
    private String lastName;
    private double currentWeight;
    private double bmi;
    private int maintenanceCalories;
    private int dailyPoints;

    public UserDTO(UUID id,
                   User.Gender gender,
                   Date birthdate,
                   double heightInInches,
                   User.ActivityLevel activityLevel,
                   String email,
                   String firstName,
                   String lastName,
                   double currentWeight,
                   double bmi,
                   int maintenanceCalories,
                   int dailyPoints
    ) {
        this.id = id;
        this.gender = gender;
        this.birthdate = birthdate;
        this.heightInInches = heightInInches;
        this.activityLevel = activityLevel;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.currentWeight = currentWeight;
        this.bmi = bmi;
        this.maintenanceCalories = maintenanceCalories;
        this.dailyPoints = dailyPoints;
    }

    public UserDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User.Gender getGender() {
        return gender;
    }

    public void setGender(User.Gender gender) {
        this.gender = gender;
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

    public User.ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(User.ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public double getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public int getMaintenanceCalories() {
        return maintenanceCalories;
    }

    public void setMaintenanceCalories(int maintenanceCalories) {
        this.maintenanceCalories = maintenanceCalories;
    }

    public int getDailyPoints() {
        return dailyPoints;
    }

    public void setDailyPoints(int dailyPoints) {
        this.dailyPoints = dailyPoints;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof UserDTO)) {
            return false;
        }
        UserDTO that = (UserDTO) other;
        if((this == null && that != null) || (that == null && this != null)) {
            return false;
        }
        return this.getId().equals(that.getId())
                && this.getGender().equals(that.getGender())
                && this.getBirthdate() == that.getBirthdate()
                && this.getHeightInInches() == that.getHeightInInches()
                && this.getActivityLevel().equals(that.getActivityLevel())
                && this.getEmail() == that.getEmail()
                && this.getFirstName() == that.getFirstName()
                && this.getLastName() == that.getLastName()
                && this.getCurrentWeight() == that.getCurrentWeight()
                && this.getBmi() == that.getBmi()
                && this.getMaintenanceCalories() == that.getMaintenanceCalories()
                && this.getDailyPoints() == that.getDailyPoints();
    }

}
