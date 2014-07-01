package net.steveperkins.fitnessjiffy.dto;

import net.steveperkins.fitnessjiffy.domain.Food;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
            @Nullable UUID id, // will be null if this represents a new Food which hasn't yet been persisted
            @Nullable UUID ownerId, // will be null if this represents a "global" Food, usable by all users
            @Nonnull String name,
            @Nonnull Food.ServingType defaultServingType,
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

    @Nullable
    public UUID getId() {
        return id;
    }

    public void setId(@Nullable UUID id) {
        this.id = id;
    }

    @Nullable
    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(@Nullable UUID ownerId) {
        this.ownerId = ownerId;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    public Food.ServingType getDefaultServingType() {
        return defaultServingType;
    }

    public void setDefaultServingType(@Nonnull Food.ServingType defaultServingType) {
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

    @Override
    public boolean equals(Object other) {
        if(other == null || !(other instanceof FoodDTO)) {
            return false;
        }
        FoodDTO that = (FoodDTO) other;
        return this.getId().equals(that.getId())
                && ((this.getOwnerId() == null && that.getOwnerId() == null) || (this.getOwnerId().equals(that.getOwnerId())))
                && this.getName().equals(that.getName())
                && this.getDefaultServingType().equals(that.getDefaultServingType())
                && this.getServingTypeQty() == that.getServingTypeQty()
                && this.getCalories() == that.getCalories()
                && this.getFat() == that.getFat()
                && this.getSaturatedFat() == that.getSaturatedFat()
                && this.getCarbs() == that.getCarbs()
                && this.getFiber() == that.getFiber()
                && this.getSugar() == that.getSugar()
                && this.getProtein() == that.getProtein()
                && this.getSodium() == that.getSodium();
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode()
                + (this.getOwnerId() != null ? this.getOwnerId().hashCode() : 0)
                + this.getName().hashCode()
                + this.getDefaultServingType().hashCode()
                + (int) this.getServingTypeQty()
                + this.getCalories()
                + (int) this.getFat()
                + (int) this.getSaturatedFat()
                + (int) this.getCarbs()
                + (int) this.getFiber()
                + (int) this.getSugar()
                + (int) this.getProtein()
                + (int) this.getSodium();
    }

}
