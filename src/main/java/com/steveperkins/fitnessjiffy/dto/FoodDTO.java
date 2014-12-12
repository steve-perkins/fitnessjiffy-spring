package com.steveperkins.fitnessjiffy.dto;

import com.steveperkins.fitnessjiffy.domain.Food;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.UUID;

public final class FoodDTO implements Serializable {

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
            @Nullable final UUID id, // will be null if this represents a new Food which hasn't yet been persisted
            @Nullable final UUID ownerId, // will be null if this represents a "global" Food, usable by all users
            @Nonnull final String name,
            @Nonnull final Food.ServingType defaultServingType,
            final double servingTypeQty,
            final int calories,
            final double fat,
            final double saturatedFat,
            final double carbs,
            final double fiber,
            final double sugar,
            final double protein,
            final double sodium
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

    public void setId(@Nonnull final UUID id) {
        this.id = id;
    }

    @Nullable
    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(@Nonnull final UUID ownerId) {
        this.ownerId = ownerId;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull final String name) {
        this.name = name;
    }

    @Nonnull
    public Food.ServingType getDefaultServingType() {
        return defaultServingType;
    }

    public void setDefaultServingType(@Nonnull final Food.ServingType defaultServingType) {
        this.defaultServingType = defaultServingType;
    }

    public double getServingTypeQty() {
        return servingTypeQty;
    }

    public void setServingTypeQty(final double servingTypeQty) {
        this.servingTypeQty = servingTypeQty;
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

    public double getSodium() {
        return sodium;
    }

    public void setSodium(final double sodium) {
        this.sodium = sodium;
    }

    @Override
    public boolean equals(final Object other) {
        boolean equals = false;
        if (other instanceof FoodDTO) {
            final FoodDTO that = (FoodDTO) other;
            equals = this.getId().equals(that.getId())
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
        return equals;
    }

    @Override
    public int hashCode() {
        final int idHash = (id == null) ? 0 : id.hashCode();
        final int ownerIdHash = (ownerId == null) ? 0 : ownerId.hashCode();
        final int nameHash = (name == null) ? 0 : name.hashCode();
        final int defaultServingTypeHash = (defaultServingType == null) ? 0 : defaultServingType.hashCode();
        final int servingTypeQtyHash = (int) servingTypeQty;
        final int caloriesHash = calories;
        final int fatHash = (int) fat;
        final int saturatedFatHash = (int) saturatedFat;
        final int carbsHash = (int) carbs;
        final int fiberHash = (int) fiber;
        final int sugarHash = (int) sugar;
        final int proteinHash = (int) protein;
        final int sodiumHash = (int) sodium;

        return idHash + ownerIdHash + nameHash + defaultServingTypeHash + servingTypeQtyHash
                + caloriesHash + fatHash + saturatedFatHash + carbsHash + fiberHash + sugarHash
                + proteinHash + sodiumHash;
    }

}
