package net.steveperkins.fitnessjiffy.domain;

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
        name = "EXERCISE_PERFORMED",
        uniqueConstraints = @UniqueConstraint(columnNames = {"USER_ID", "EXERCISE_ID", "DATE"})
)
public class ExercisePerformed {

    @Id
    @Column(name = "ID", columnDefinition = "BYTEA", length = 16)
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
            @Nullable final UUID id,
            @Nonnull final User user,
            @Nonnull final Exercise exercise,
            @Nonnull final Date date,
            final int minutes
    ) {
        this.id = Optional.ofNullable(id).orElse(UUID.randomUUID());
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
    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(@Nonnull final Exercise exercise) {
        this.exercise = exercise;
    }

    @Nonnull
    public Date getDate() {
        return (Date) date.clone();
    }

    public void setDate(@Nonnull final Date date) {
        this.date = (Date) date.clone();
    }

    @Nonnull
    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(@Nonnull final Integer minutes) {
        this.minutes = minutes;
    }

}
