package com.steveperkins.fitnessjiffy.dto;

import javax.annotation.Nonnull;
import java.sql.Date;
import java.util.UUID;

public final class ReportDataDTO {

    private UUID id;
    private UUID userId;
    private Date date;
    private double pounds;
    private int netCalories;
    private double netPoints;

    public ReportDataDTO(
            @Nonnull final UUID id,
            @Nonnull final UUID userId,
            @Nonnull final Date date,
            final double pounds,
            final int netCalories,
            final double netPoints
    ) {
        this.id = id;
        this.userId = userId;
        this.date = (Date) date.clone();
        this.pounds = pounds;
        this.netCalories = netCalories;
        this.netPoints = netPoints;
    }

    public ReportDataDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return (Date) date.clone();
    }

    public void setDate(final Date date) {
        this.date = (Date) date.clone();
    }

    public double getPounds() {
        return pounds;
    }

    public void setPounds(final double pounds) {
        this.pounds = pounds;
    }

    public int getNetCalories() {
        return netCalories;
    }

    public void setNetCalories(final int netCalories) {
        this.netCalories = netCalories;
    }

    public double getNetPoints() {
        return netPoints;
    }

    public void setNetPoints(final double netPoints) {
        this.netPoints = netPoints;
    }

    @Override
    public boolean equals(final Object other) {
        boolean equals = false;
        if (other instanceof ReportDataDTO) {
            final ReportDataDTO that = (ReportDataDTO) other;
            equals = this.id.equals(that.id)
                    && this.userId.equals(that.userId)
                    && this.date.equals(that.date)
                    && this.pounds == that.pounds
                    && this.netCalories == that.netCalories
                    && this.netPoints == that.netPoints;
        }
        return equals;
    }

    @Override
    public int hashCode() {
        final int idHash = (id == null) ? 0 : id.hashCode();
        final int userIdHash = (userId == null) ? 0 : userId.hashCode();
        final int dateHash = (date == null) ? 0 : date.hashCode();
        final int poundsHash = Double.valueOf(pounds).hashCode();
        final int netCaloriesHash = Integer.valueOf(netCalories).hashCode();
        final int netPointsHash = Double.valueOf(netPoints).hashCode();

        return idHash + userIdHash + dateHash + poundsHash + netCaloriesHash + netPointsHash;
    }

}
