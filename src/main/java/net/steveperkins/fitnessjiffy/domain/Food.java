package net.steveperkins.fitnessjiffy.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
public class Food {

    public enum ServingType {
        OUNCE(1), CUP(8), POUND(16), PINT(16), TABLESPOON(0.5), TEASPOON(0.1667), GRAM(0.03527), CUSTOM(0);
        private double value;
        private ServingType(double value) {
            this.value = value;
        }
        public static ServingType fromValue(double value) {
            for(ServingType servingType : ServingType.values()) {
                if(servingType.getValue() == value) {
                    return servingType;
                }
            }
            return null;
        }
        public static ServingType fromString(String s) {
            for(ServingType servingType : ServingType.values()) {
                if(servingType.toString().equalsIgnoreCase(s)) {
                    return servingType;
                }
            }
            return null;
        }
        public double getValue() {
            return this.value;
        }
    }

    @Id
    @Column(columnDefinition = "BYTEA", length = 16)
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

    public Food(UUID id, User owner, String name, ServingType defaultServingType,
                double servingTypeQty, int calories, double fat, double saturatedFat,
                double carbs, double fiber, double sugar, double protein, double sodium) {
        this.id = (id != null) ? id : UUID.randomUUID();
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
    }

    public Food() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServingType getDefaultServingType() {
        return defaultServingType;
    }

    public void setDefaultServingType(ServingType defaultServingType) {
        this.defaultServingType = defaultServingType;
    }

    public Double getServingTypeQty() {
        return servingTypeQty;
    }

    public void setServingTypeQty(Double servingTypeQty) {
        this.servingTypeQty = servingTypeQty;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getSaturatedFat() {
        return saturatedFat;
    }

    public void setSaturatedFat(Double saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    public Double getCarbs() {
        return carbs;
    }

    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }

    public Double getFiber() {
        return fiber;
    }

    public void setFiber(Double fiber) {
        this.fiber = fiber;
    }

    public Double getSugar() {
        return sugar;
    }

    public void setSugar(Double sugar) {
        this.sugar = sugar;
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public Double getSodium() {
        return sodium;
    }

    public void setSodium(Double sodium) {
        this.sodium = sodium;
    }

    public double getPoints() {
        double fiber = (this.fiber <= 4) ? this.fiber : 4;
        double points = (calories / 50) + (fat / 12) - (fiber / 5);
        return (points > 0) ? points : 0;
    }
	
}
