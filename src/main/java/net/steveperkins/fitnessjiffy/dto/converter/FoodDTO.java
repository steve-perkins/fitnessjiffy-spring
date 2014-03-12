package net.steveperkins.fitnessjiffy.dto.converter;

import net.steveperkins.fitnessjiffy.domain.Food;

import java.util.UUID;

public class FoodDTO {

    private UUID id;
    private UUID ownerId;
    private String name;
    private Food.ServingType defaultServingType;
    private double servingTypeQty;
    private int calories;
    private double fat;
    private double saturatedFat;
    private double carbs;
    private double fiber;
    private double sugar;
    private double protein;
    private double sodium;

    public FoodDTO(
            UUID id,
            UUID ownerId,
            String name,
            Food.ServingType defaultServingType,
            double servingTypeQty,
            int calories,
            double fat,
            double saturatedFat,
            double carbs,
            double fiber,
            double sugar,
            double protein,
            double sodium
    ) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.defaultServingType = defaultServingType;
        this.servingTypeQty = servingTypeQty;
        this.calories = calories;
        this.fat = fat;
        this.saturatedFat = saturatedFat;
        this.carbs = carbs;
        this.fiber = fiber;
        this.sugar = sugar;
        this.protein = protein;
        this.sodium = sodium;
    }

    public FoodDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Food.ServingType getDefaultServingType() {
        return defaultServingType;
    }

    public void setDefaultServingType(Food.ServingType defaultServingType) {
        this.defaultServingType = defaultServingType;
    }

    public double getServingTypeQty() {
        return servingTypeQty;
    }

    public void setServingTypeQty(double servingTypeQty) {
        this.servingTypeQty = servingTypeQty;
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

    public double getSodium() {
        return sodium;
    }

    public void setSodium(double sodium) {
        this.sodium = sodium;
    }
}
