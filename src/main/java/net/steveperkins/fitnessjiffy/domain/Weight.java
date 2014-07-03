package net.steveperkins.fitnessjiffy.domain;

import com.google.common.base.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Date;
import java.util.UUID;

@Entity
@Table(name = "WEIGHT")
public final class Weight {

    @Id
    @Column(columnDefinition = "BYTEA", length = 16)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "DATE", nullable = false)
    private Date date;

    @Column(name = "POUNDS", nullable = false)
    private Double pounds;

    public Weight(
            @Nullable final UUID id,
            @Nonnull final User user,
            @Nonnull final Date date,
            final double pounds
    ) {
        this.id = Optional.fromNullable(id).or(UUID.randomUUID());
        this.user = user;
        this.date = (Date) date.clone();
        this.pounds = pounds;
    }

    public Weight() {
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

}
