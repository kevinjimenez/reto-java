package com.banco.ms_cuenta.messaging;

import com.banco.ms_cuenta.config.RabbitMQConfig;
import com.banco.ms_cuenta.entities.ClienteReplica;
import com.banco.ms_cuenta.events.ClienteEvent;
import com.banco.ms_cuenta.repositories.ClienteReplicaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteEventListener {

    private final ClienteReplicaRepository clienteReplicaRepository;

    @RabbitListener(queues = RabbitMQConfig.CLIENTE_EVENTS_QUEUE)
    @Transactional
    public void escucharEventoCliente(ClienteEvent evento) {
        log.info("Evento recibido: {} para cliente {}", evento.getTipoEvento(), evento.getClienteId());

        switch (evento.getTipoEvento()) {
            case "CREATED", "UPDATED" -> clienteReplicaRepository.save(
                    ClienteReplica.builder()
                            .clientePersonaId(evento.getClientePersonaId())
                            .clienteId(evento.getClienteId())
                            .nombre(evento.getNombre())
                            .estado(evento.getEstado())
                            .build());
            case "DELETED" -> clienteReplicaRepository.findByClienteId(evento.getClienteId())
                    .ifPresent(clienteReplicaRepository::delete);
            default -> log.warn("Tipo de evento desconocido: {}",evento.getTipoEvento());
        }
    }

}
