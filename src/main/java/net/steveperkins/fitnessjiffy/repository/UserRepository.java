package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.User;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {

    @Nullable
    public User findByEmailEquals(@Nonnull String email);

}
