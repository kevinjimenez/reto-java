package com.banco.ms_cliente.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String CLIENTE_EXCHANGE= "cliente.events.exchange";
    public static final String ROUTING_KEY_CREATED = "cliente.created";
    public static final String ROUTING_KEY_UPDATED = "cliente.updated";
    public static final String ROUTING_KEY_DELETED = "cliente.deleted";

    @Bean
    public TopicExchange clienteExchange() {
        return new TopicExchange(CLIENTE_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

}
