package com.steveperkins.fitnessjiffy.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(
        name = "FOOD",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ID", "OWNER_ID"})
)
public final class Food {

    public enum ServingType {

        OUNCE(1), CUP(8), POUND(16), PINT(16), TABLESPOON(0.5), TEASPOON(0.1667), GRAM(0.03527), CUSTOM(0);

        private double value;

        private ServingType(final double value) {
            this.value = value;
        }

        @Nullable
        public static ServingType fromValue(final double value) {
            ServingType match = null;
            for (final ServingType servingType : ServingType.values()) {
                if (servingType.getValue() == value) {
                    match = servingType;
                }
            }
            return match;
        }

        @Nullable
        public static ServingType fromString(@Nonnull final String s) {
            ServingType match = null;
            for (final ServingType servingType : ServingType.values()) {
                if (servingType.toString().equalsIgnoreCase(s)) {
                    match = servingType;
                }
            }
            return match;
        }

        public final double getValue() {
            return this.value;
        }

    }

    @Id
    @Column(name = "ID", columnDefinition = "BYTEA", length = 16)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "OWNER_ID", nullable = true)
    private User owner;

    @Column(name = "NAME", length = 50, nullable = false)
    private String name;

    @Column(name = "DEFAULT_SERVING_TYPE", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private ServingType defaultServingType;

    @Column(name = "SERVING_TYPE_QTY", nullable = false)
    private Double servingTypeQty;

    @Column(name = "CALORIES", nullable = false)
    private Integer calories;

    @Column(name = "FAT", nullable = false)
    private Double fat;

    @Column(name = "SATURATED_FAT", nullable = false)
    private Double saturatedFat;

    @Column(name = "CARBS", nullable = false)
    private Double carbs;

    @Column(name = "FIBER", nullable = false)
    private Double fiber;

    @Column(name = "SUGAR", nullable = false)
    private Double sugar;

    @Column(name = "PROTEIN", nullable = false)
    private Double protein;

    @Column(name = "SODIUM", nullable = false)
    private Double sodium;

    @Column(name = "CREATED_TIME", nullable = false)
    private Timestamp createdTime = new Timestamp(new java.util.Date().getTime());

    @Column(name = "LAST_UPDATED_TIME", nullable = false)
    private Timestamp lastUpdatedTime = new Timestamp(new java.util.Date().getTime());

    public Food(
            @Nullable final UUID id,
            @Nullable final User owner,
            @Nonnull final String name,
            @Nonnull final ServingType defaultServingType,
            final double servingTypeQty,
            final int calories,
            final double fat,
            final double saturatedFat,
            final double carbs,
            final double fiber,
            final double sugar,
            final double protein,
            final double sodium,
            @Nonnull final Timestamp createdTime,
            @Nonnull final Timestamp lastUpdatedTime
    ) {
        this.id = Optional.ofNullable(id).orElse(UUID.randomUUID());
        this.owner = owner;
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
        this.createdTime = (Timestamp) createdTime.clone();
        this.lastUpdatedTime = (Timestamp) lastUpdatedTime.clone();
    }

    public Food() {
    }

    @Nonnull
    public UUID getId() {
        return id;
    }

    public void setId(@Nonnull final UUID id) {
        this.id = id;
    }

    @Nullable
    public User getOwner() {
        return owner;
    }

    public void setOwner(@Nullable final User owner) {
        this.owner = owner;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull final String name) {
        this.name = name;
    }

    @Nonnull
    public ServingType getDefaultServingType() {
        return defaultServingType;
    }

    public void setDefaultServingType(@Nonnull final ServingType defaultServingType) {
        this.defaultServingType = defaultServingType;
    }

    @Nonnull
    public Double getServingTypeQty() {
        return servingTypeQty;
    }

    public void setServingTypeQty(@Nonnull final Double servingTypeQty) {
        this.servingTypeQty = servingTypeQty;
    }

    @Nonnull
    public Integer getCalories() {
        return calories;
    }

    public void setCalories(@Nonnull final Integer calories) {
        this.calories = calories;
    }

    @Nonnull
    public Double getFat() {
        return fat;
    }

    public void setFat(@Nonnull final Double fat) {
        this.fat = fat;
    }

    @Nonnull
    public Double getSaturatedFat() {
        return saturatedFat;
    }

    public void setSaturatedFat(@Nonnull final Double saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    @Nonnull
    public Double getCarbs() {
        return carbs;
    }

    public void setCarbs(@Nonnull final Double carbs) {
        this.carbs = carbs;
    }

    @Nonnull
    public Double getFiber() {
        return fiber;
    }

    public void setFiber(@Nonnull final Double fiber) {
        this.fiber = fiber;
    }

    @Nonnull
    public Double getSugar() {
        return sugar;
    }

    public void setSugar(@Nonnull final Double sugar) {
        this.sugar = sugar;
    }

    @Nonnull
    public Double getProtein() {
        return protein;
    }

    public void setProtein(@Nonnull final Double protein) {
        this.protein = protein;
    }

    @Nonnull
    public Double getSodium() {
        return sodium;
    }

    public void setSodium(@Nonnull final Double sodium) {
        this.sodium = sodium;
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

    public double getPoints() {
        final double fiber = (this.fiber <= 4) ? this.fiber : 4;
        final double points = (calories / 50.0) + (fat / 12.0) - (fiber / 5.0);
        return (points > 0) ? points : 0;
    }

}
