package net.steveperkins.fitnessjiffy.repository;

import net.steveperkins.fitnessjiffy.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {
}
