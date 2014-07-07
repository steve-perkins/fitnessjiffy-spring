package net.steveperkins.fitnessjiffy.dto;

import net.steveperkins.fitnessjiffy.domain.Food;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.sql.Date;
import java.util.UUID;

public final class FoodEatenDTO implements Serializable {

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
            @Nullable final UUID id,
            @Nonnull final UUID userId,
            @Nonnull final FoodDTO food,
            @Nonnull final Date date,
            @Nonnull final Food.ServingType servingType,
            final double servingQty,
            final int calories,
            final double fat,
            final double saturatedFat,
            final double sodium,
            final double carbs,
            final double fiber,
            final double sugar,
            final double protein,
            final double points
    ) {
        this.id = id;
        this.userId = userId;
        this.food = food;
        this.date = (Date) date.clone();
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

    @Nullable
    public UUID getId() {
        return id;
    }

    public void setId(@Nonnull final UUID id) {
        this.id = id;
    }

    @Nonnull
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(@Nonnull final UUID userId) {
        this.userId = userId;
    }

    @Nonnull
    public FoodDTO getFood() {
        return food;
    }

    public void setFood(@Nonnull final FoodDTO food) {
        this.food = food;
    }

    @Nonnull
    public Date getDate() {
        return (Date) date.clone();
    }

    public void setDate(@Nonnull final Date date) {
        this.date = (Date) date.clone();
    }

    @Nonnull
    public Food.ServingType getServingType() {
        return servingType;
    }

    public void setServingType(@Nonnull final Food.ServingType servingType) {
        this.servingType = servingType;
    }

    public double getServingQty() {
        return servingQty;
    }

    public void setServingQty(final double servingQty) {
        this.servingQty = servingQty;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(final int calories) {
        this.calories = calories;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(final double fat) {
        this.fat = fat;
    }

    public double getSaturatedFat() {
        return saturatedFat;
    }

    public void setSaturatedFat(final double saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    public double getSodium() {
        return sodium;
    }

    public void setSodium(final double sodium) {
        this.sodium = sodium;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(final double carbs) {
        this.carbs = carbs;
    }

    public double getFiber() {
        return fiber;
    }

    public void setFiber(final double fiber) {
        this.fiber = fiber;
    }

    public double getSugar() {
        return sugar;
    }

    public void setSugar(final double sugar) {
        this.sugar = sugar;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(final double protein) {
        this.protein = protein;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(final double points) {
        this.points = points;
    }

    @Override
    public boolean equals(final Object other) {
        boolean equals = false;
        if (other instanceof FoodEatenDTO) {
            final FoodEatenDTO that = (FoodEatenDTO) other;
            equals = this.getId().equals(that.getId())
                    && this.getUserId().equals(that.getUserId())
                    && this.getFood().equals(that.getFood())
                    && this.getDate().equals(that.getDate())
                    && this.getServingType().equals(that.getServingType())
                    && this.getServingQty() == that.getServingQty();
        }
        return equals;
    }

    @Override
    public int hashCode() {
        final int idHash = (id == null) ? 0 : id.hashCode();
        final int userIdHash = (userId == null) ? 0 : userId.hashCode();
        final int foodHash = (food == null) ? 0 : food.hashCode();
        final int dateHash = (date == null) ? 0 : date.hashCode();
        final int servingTypeHash = (servingType == null) ? 0 : servingType.hashCode();
        final int servingQtyHash = (int) servingQty;

        return idHash + userIdHash + foodHash + dateHash + servingTypeHash + servingQtyHash;
    }

}
