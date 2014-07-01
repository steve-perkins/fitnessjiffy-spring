package net.steveperkins.fitnessjiffy.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "EXERCISE")
public class Exercise {

    @Id
    @Column(columnDefinition = "BYTEA", length = 16)
    private UUID id;

    @Column(name = "CODE", length = 5, nullable = false)
    private String code;

    @Column(name = "METABOLIC_EQUIVALENT", nullable = false)
    private Double metabolicEquivalent;

    @Column(name = "CATEGORY", length = 25, nullable = false)
    private String category;

    @Column(name = "DESCRIPTION", length = 250, nullable = false)
    private String description;

    public Exercise(
            @Nullable UUID id,
            @Nonnull String code,
            double metabolicEquivalent,
            @Nonnull String category,
            @Nonnull String description
    ) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.code = code;
        this.metabolicEquivalent = metabolicEquivalent;
        this.category = category;
        this.description = description;
    }

    public Exercise() {
    }

    @Nonnull
    public UUID getId() {
        return id;
    }

    public void setId(@Nonnull UUID id) {
        this.id = id;
    }

    @Nonnull
    public String getCode() {
        return code;
    }

    public void setCode(@Nonnull String code) {
        this.code = code;
    }

    @Nonnull
    public Double getMetabolicEquivalent() {
        return metabolicEquivalent;
    }

    public void setMetabolicEquivalent(@Nonnull Double metabolicEquivalent) {
        this.metabolicEquivalent = metabolicEquivalent;
    }

    @Nonnull
    public String getCategory() {
        return category;
    }

    public void setCategory(@Nonnull String category) {
        this.category = category;
    }

    @Nonnull
    public String getDescription() {
        return description.trim();
    }

    public void setDescription(@Nonnull String description) {
        if(description != null) description = description.trim();
        this.description = description;
    }

}
