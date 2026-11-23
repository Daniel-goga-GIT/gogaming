package org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserId(Long userId);
}
