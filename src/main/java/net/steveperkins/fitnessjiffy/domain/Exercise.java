package net.steveperkins.fitnessjiffy.domain;

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

    public Exercise(UUID id, String code, double metabolicEquivalent, String category, String description) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.code = code;
        this.metabolicEquivalent = metabolicEquivalent;
        this.category = category;
        this.description = description;
    }

    public Exercise() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getMetabolicEquivalent() {
        return metabolicEquivalent;
    }

    public void setMetabolicEquivalent(Double metabolicEquivalent) {
        this.metabolicEquivalent = metabolicEquivalent;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return (description != null) ? description.trim() : null;
    }

    public void setDescription(String description) {
        if(description != null) description = description.trim();
        this.description = description;
    }

}
