package net.steveperkins.fitnessjiffy.dto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.util.UUID;

public class WeightDTO {

    private UUID id;
    private UUID userId;
    private Date date;
    private double pounds;

    public WeightDTO(
            @Nonnull final UUID id,
            @Nonnull final UUID userId,
            @Nonnull final Date date,
            final double pounds
    ) {
        this.id = id;
        this.userId = userId;
        this.date = (Date) date.clone();
        this.pounds = pounds;
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

    @Override
    public boolean equals(final Object other) {
        boolean equals = false;
        if (other instanceof WeightDTO) {
            final WeightDTO that = (WeightDTO) other;
            equals = this.id.equals(that.id)
                    && this.userId.equals(that.userId)
                    && this.date.equals(that.date)
                    && this.pounds == that.pounds;
        }
        return equals;
    }

    @Override
    public int hashCode() {
        final int idHash = (id == null) ? 0 : id.hashCode();
        final int userIdHash = (userId == null) ? 0 : userId.hashCode();
        final int dateHash = (date == null) ? 0 : date.hashCode();
        final int poundsHash = Double.valueOf(pounds).hashCode();

        return idHash + userIdHash + dateHash + poundsHash;
    }
}
