package com.steveperkins.fitnessjiffy.dto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.sql.Date;
import java.util.UUID;

public final class ExercisePerformedDTO implements Serializable {

    private UUID id;
    private UUID userId;
    private ExerciseDTO exercise;
    private Date date;
    private int minutes;
    private int caloriesBurned;
    private double pointsBurned;

    public ExercisePerformedDTO(
            @Nullable final UUID id,
            @Nonnull final UUID userId,
            @Nonnull final ExerciseDTO exercise,
            @Nonnull final Date date,
            final int minutes
    ) {
        this.id = id;
        this.userId = userId;
        this.exercise = exercise;
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
    public ExerciseDTO getExercise() {
        return exercise;
    }

    public void setExercise(@Nonnull final ExerciseDTO exercise) {
        this.exercise = exercise;
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

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(final int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public double getPointsBurned() {
        return pointsBurned;
    }

    public void setPointsBurned(final double pointsBurned) {
        this.pointsBurned = pointsBurned;
    }

    @Override
    public boolean equals(final Object other) {
        boolean equals = false;
        if (other instanceof ExercisePerformedDTO) {
            final ExercisePerformedDTO that = (ExercisePerformedDTO) other;
            if ((this.getId() == null && that.getId() != null) || (that.getId() == null && this.getId() != null)) {
                equals = false;
            } else {
                equals = this.getId().equals(that.getId())
                        && this.getUserId().equals(that.getUserId())
                        && this.getExercise().equals(that.getExercise())
                        && this.getDate().equals(that.getDate())
                        && this.getMinutes() == that.getMinutes()
                        && this.getCaloriesBurned() == that.getCaloriesBurned()
                        && this.getPointsBurned() == that.getPointsBurned();
            }
        }
        return equals;
   }

    @Override
    public int hashCode() {
        final int idHash = (id == null) ? 0 : id.hashCode();
        final int userIdHash = (userId == null) ? 0 : userId.hashCode();
        final int exerciseHash = (exercise == null) ? 0 : exercise.hashCode();
        final int dateHash = (date == null) ? 0 : date.hashCode();
        final int caloriesBurnedHash = Integer.valueOf(caloriesBurned).hashCode();
        final int pointsBurnedHash = Double.valueOf(pointsBurned).hashCode();

        return idHash + userIdHash + exerciseHash + dateHash + minutes + caloriesBurnedHash + pointsBurnedHash;
   }
}
