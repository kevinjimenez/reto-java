package com.banco.ms_cliente.messaging;

import com.banco.ms_cliente.config.RabbitMQConfig;
import com.banco.ms_cliente.entities.Cliente;
import com.banco.ms_cliente.events.ClienteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publicarCreado(Cliente cliente) {
        publicar(cliente, "CREATED", RabbitMQConfig.ROUTING_KEY_CREATED);
    }

    public void publicarActualizado(Cliente cliente) {
        publicar(cliente, "UPDATED", RabbitMQConfig.ROUTING_KEY_UPDATED);
    }

    public void publicarEliminado(Cliente cliente) {
        publicar(cliente, "DELETED", RabbitMQConfig.ROUTING_KEY_DELETED);
    }


    private void publicar(Cliente cliente, String tipoEvento, String routingKey) {
        ClienteEvent evento = ClienteEvent.builder()
                .tipoEvento(tipoEvento)
                .clientePersonaId(cliente.getId())
                .clienteId(cliente.getClienteId())
                .nombre(cliente.getNombre())
                .estado(cliente.getEstado())
                .build();

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.CLIENTE_EXCHANGE, routingKey, evento);
            log.info("Evento {} publicado para cliente {}", tipoEvento, cliente.getClienteId());
        } catch (Exception e) {
            log.error("Error al enviar evento {} para cliente {}: {}", tipoEvento, cliente.getClienteId(), e.getMessage());
        }
    }
}
