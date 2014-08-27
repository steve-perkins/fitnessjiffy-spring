package net.steveperkins.fitnessjiffy.dto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.sql.Date;
import java.util.UUID;

public final class ExercisePerformedDTO implements Serializable {

    private UUID id;
    private UUID userId;
    private UUID exerciseId;
    private Date date;
    private int minutes;

    public ExercisePerformedDTO(
            @Nullable final UUID id,
            @Nonnull final UUID userId,
            @Nonnull final UUID exerciseId,
            @Nonnull final Date date,
            final int minutes
    ) {
        this.id = id;
        this.userId = userId;
        this.exerciseId = exerciseId;
        this.date = (Date) date.clone();
        this.minutes = minutes;
    }

    public ExercisePerformedDTO() {
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
    public UUID getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(@Nonnull final UUID exerciseId) {
        this.exerciseId = exerciseId;
    }

    @Nonnull
    public Date getDate() {
        return (Date) date.clone();
    }

    public void setDate(@Nonnull final Date date) {
        this.date = (Date) date.clone();
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(final int minutes) {
        this.minutes = minutes;
    }

    @Override
    public boolean equals(final Object other) {
        boolean equals = false;
        if (other instanceof ExercisePerformedDTO) {
            final ExercisePerformedDTO that = (ExercisePerformedDTO) other;
            equals = this.getId().equals(that.getId())
                    && this.getUserId().equals(that.getUserId())
                    && this.getExerciseId().equals(that.getExerciseId())
                    && this.getDate().equals(that.getDate())
                    && this.getMinutes() == that.getMinutes();
        }
        return equals;
   }

    @Override
    public int hashCode() {
        final int idHash = (id == null) ? 0 : id.hashCode();
        final int userIdHash = (userId == null) ? 0 : userId.hashCode();
        final int exerciseIdHash = (exerciseId == null) ? 0 : exerciseId.hashCode();
        final int dateHash = (date == null) ? 0 : date.hashCode();

        return idHash + userIdHash + exerciseIdHash + dateHash + minutes;
   }
}
