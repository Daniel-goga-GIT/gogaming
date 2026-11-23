package org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories;



import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);
    @Query("SELECT u.id FROM User u WHERE u.email = :email")
    Long getIdByEmail(String email);
}