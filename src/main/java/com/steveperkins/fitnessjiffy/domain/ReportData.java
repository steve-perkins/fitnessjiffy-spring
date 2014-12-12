package com.steveperkins.fitnessjiffy.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.sql.Date;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(
        name = "REPORT_DATA",
        uniqueConstraints = @UniqueConstraint(columnNames = {"USER_ID", "DATE"})
)
public final class ReportData {

    @Id
    @Column(columnDefinition = "BYTEA", length = 16)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "DATE", nullable = false)
    private Date date;

    @Column(name = "POUNDS", nullable = false)
    private Double pounds = 0.0;

    @Column(name = "NET_CALORIES", nullable = false)
    private Integer netCalories = 0;

    @Column(name = "NET_POINTS", nullable = false)
    private Double netPoints = 0.0;

    public ReportData(
            @Nullable final UUID id,
            @Nonnull final User user,
            @Nonnull final Date date,
            final double pounds,
            final int netCalories,
            final double netPoints
    ) {
        this.id = Optional.ofNullable(id).orElse(UUID.randomUUID());
        this.user = user;
        this.date = (Date) date.clone();
        this.pounds = pounds;
    }

    public ReportData() {
    }

    @Nonnull
    public UUID getId() {
        return id;
    }

    public void setId(@Nonnull final UUID id) {
        this.id = id;
    }

    @Nonnull
    public User getUser() {
        return user;
    }

    public void setUser(@Nonnull final User user) {
        this.user = user;
    }

    @Nonnull
    public Date getDate() {
        return (Date) date.clone();
    }

    public void setDate(@Nonnull final Date date) {
        this.date = (Date) date.clone();
    }

    @Nonnull
    public Double getPounds() {
        return pounds;
    }

    public void setPounds(@Nonnull final Double pounds) {
        this.pounds = pounds;
    }

    @Nonnull
    public Integer getNetCalories() {
        return netCalories;
    }

    public void setNetCalories(@Nonnull final Integer netCalories) {
        this.netCalories = netCalories;
    }

    @Nonnull
    public Double getNetPoints() {
        return netPoints;
    }

    public void setNetPoints(@Nonnull final Double netPoints) {
        this.netPoints = netPoints;
    }

}
