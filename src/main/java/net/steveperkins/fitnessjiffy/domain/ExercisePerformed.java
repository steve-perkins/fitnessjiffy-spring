package net.steveperkins.fitnessjiffy.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Date;
import java.util.UUID;

@Entity
public class ExercisePerformed {

    @Id
    @Column(columnDefinition = "BYTEA", length = 16)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "EXERCISE_ID", nullable = false)
    private Exercise exercise;

    @Column(name = "DATE", nullable = false)
    private Date date;

    @Column(name = "MINUTES", nullable = false)
    private Integer minutes;

    public ExercisePerformed(
            @Nullable UUID id,
            @Nonnull User user,
            @Nonnull Exercise exercise,
            @Nonnull Date date,
            int minutes
    ) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.user = user;
        this.exercise = exercise;
        this.date = (Date) date.clone();
        this.minutes = minutes;
    }

    public ExercisePerformed() {
    }

    @Nonnull
    public UUID getId() {
        return id;
    }

    public void setId(@Nonnull UUID id) {
        this.id = id;
    }

    @Nonnull
    public User getUser() {
        return user;
    }

    public void setUser(@Nonnull User user) {
        this.user = user;
    }

    @Nonnull
    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(@Nonnull Exercise exercise) {
        this.exercise = exercise;
    }

    @Nonnull
    public Date getDate() {
        return (Date) date.clone();
    }

    public void setDate(@Nonnull Date date) {
        this.date = (Date) date.clone();
    }

    @Nonnull
    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(@Nonnull Integer minutes) {
        this.minutes = minutes;
    }

}
