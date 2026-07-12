package com.banco.ms_cuenta.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
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
    public static final String CLIENTE_EVENTS_QUEUE = "ms-cuenta.cliente.events.queue";
    public static final String ROUTING_KEY_PATTERN = "cliente.*";

    @Bean
    public TopicExchange clienteExchange() {
        return new TopicExchange(CLIENTE_EXCHANGE, true,false);
    }

    @Bean
    public Queue clienteEventsQueue() {
        return new Queue(CLIENTE_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding clienteEventsBinding(Queue clienteEventsQueue, TopicExchange clienteExchange) {
        return BindingBuilder.bind(clienteEventsQueue).to(clienteExchange).with(ROUTING_KEY_PATTERN);
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
