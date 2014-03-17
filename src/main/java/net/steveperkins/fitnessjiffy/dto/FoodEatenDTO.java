package net.steveperkins.fitnessjiffy.dto;

import net.steveperkins.fitnessjiffy.domain.Food;

import java.sql.Date;
import java.util.UUID;

public class FoodEatenDTO {

    private UUID id;
    private UUID userId;
    private FoodDTO food;
    private Date date;
    private Food.ServingType servingType;
    private double servingQty;

    // fields derived from the food, times the ratio between the default serving and the serving amount eaten

    private int calories;
    private double fat;
    private double saturatedFat;
    private double sodium;
    private double carbs;
    private double fiber;
    private double sugar;
    private double protein;
    private double points;

    public FoodEatenDTO(
            UUID id,
            UUID userId,
            FoodDTO food,
            Date date,
            Food.ServingType servingType,
            double servingQty,
            int calories,
            double fat,
            double saturatedFat,
            double sodium,
            double carbs,
            double fiber,
            double sugar,
            double protein,
            double points
    ) {
        this.id = id;
        this.userId = userId;
        this.food = food;
        this.date = date;
        this.servingType = servingType;
        this.servingQty = servingQty;
        this.calories = calories;
        this.fat = fat;
        this.saturatedFat = saturatedFat;
        this.sodium = sodium;
        this.carbs = carbs;
        this.fiber = fiber;
        this.sugar = sugar;
        this.protein = protein;
        this.points = points;
    }

    public FoodEatenDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public FoodDTO getFood() {
        return food;
    }

    public void setFood(FoodDTO food) {
        this.food = food;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Food.ServingType getServingType() {
        return servingType;
    }

    public void setServingType(Food.ServingType servingType) {
        this.servingType = servingType;
    }

    public double getServingQty() {
        return servingQty;
    }

    public void setServingQty(double servingQty) {
        this.servingQty = servingQty;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getSaturatedFat() {
        return saturatedFat;
    }

    public void setSaturatedFat(double saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    public double getSodium() {
        return sodium;
    }

    public void setSodium(double sodium) {
        this.sodium = sodium;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFiber() {
        return fiber;
    }

    public void setFiber(double fiber) {
        this.fiber = fiber;
    }

    public double getSugar() {
        return sugar;
    }

    public void setSugar(double sugar) {
        this.sugar = sugar;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }
}
