package net.steveperkins.fitnessjiffy.dto;

import net.steveperkins.fitnessjiffy.domain.User;

import java.util.UUID;

public class UserDTO {

    private UUID id;
    private User.Gender gender;
    private int age;
    private double heightInInches;
    private User.ActivityLevel activityLevel;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private double currentWeight;
    private double bmi;
    private int maintenanceCalories;
    private int dailyPoints;

    public UserDTO(UUID id,
                   User.Gender gender,
                   int age,
                   double heightInInches,
                   User.ActivityLevel activityLevel,
                   String username,
                   String password,
                   String firstName,
                   String lastName,
                   boolean isActive,
                   double currentWeight,
                   double bmi,
                   int maintenanceCalories,
                   int dailyPoints
    ) {
        this.id = id;
        this.gender = gender;
        this.age = age;
        this.heightInInches = heightInInches;
        this.activityLevel = activityLevel;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
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

    public User.ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(User.ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
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

    public void setActive(boolean isActive) {
        this.isActive = isActive;
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
                && this.getAge() == that.getAge()
                && this.getHeightInInches() == that.getHeightInInches()
                && this.getActivityLevel().equals(that.getActivityLevel())
                && this.getUsername() == that.getUsername()
                && this.getPassword() == that.getPassword()
                && this.getFirstName() == that.getFirstName()
                && this.getLastName() == that.getLastName()
                && this.isActive() == that.isActive()
                && this.getCurrentWeight() == that.getCurrentWeight()
                && this.getBmi() == that.getBmi()
                && this.getMaintenanceCalories() == that.getMaintenanceCalories()
                && this.getDailyPoints() == that.getDailyPoints();
    }

}
