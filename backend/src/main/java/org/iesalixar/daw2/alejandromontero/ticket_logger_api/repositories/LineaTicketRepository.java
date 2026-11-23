package org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.LineaTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LineaTicketRepository extends JpaRepository<LineaTicket, Long> {
    List<LineaTicket> findByTicketId(Long ticketId);
}
