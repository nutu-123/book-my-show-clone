package com.showtime.booking_service.config;


import com.showtime.booking_service.constants.AppConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Booking Service.
 *
 * Exchange: showtime.exchange (Direct)
 * Queues:
 *   booking.confirmed.queue  → booking confirmed events
 *   booking.cancelled.queue  → booking cancelled events
 *
 * DLQ (Dead Letter Queue):
 *   showtime.dead.letter.queue → failed messages
 *
 * Message format: JSON (Jackson2JsonMessageConverter)
 */
@Configuration
public class RabbitMQConfig {

    // ── Exchange ──

    @Bean
    public DirectExchange showtimeExchange() {
        return new DirectExchange(AppConstants.EXCHANGE_NAME, true, false);
    }

    // ── Dead Letter Exchange & Queue ──

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("showtime.dlx", true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("showtime.dead.letter.queue").build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("showtime.dead.letter");
    }

    // ── Booking Confirmed Queue ──

    @Bean
    public Queue bookingConfirmedQueue() {
        return QueueBuilder
                .durable("booking.confirmed.queue")
                .withArgument("x-dead-letter-exchange", "showtime.dlx")
                .withArgument("x-dead-letter-routing-key",
                        "showtime.dead.letter")
                .withArgument("x-message-ttl", 86400000)  // 24 hours
                .build();
    }

    @Bean
    public Binding bookingConfirmedBinding() {
        return BindingBuilder
                .bind(bookingConfirmedQueue())
                .to(showtimeExchange())
                .with(AppConstants.BOOKING_CONFIRMED_ROUTING);
    }

    // ── Booking Cancelled Queue ──

    @Bean
    public Queue bookingCancelledQueue() {
        return QueueBuilder
                .durable("booking.cancelled.queue")
                .withArgument("x-dead-letter-exchange", "showtime.dlx")
                .withArgument("x-dead-letter-routing-key",
                        "showtime.dead.letter")
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Binding bookingCancelledBinding() {
        return BindingBuilder
                .bind(bookingCancelledQueue())
                .to(showtimeExchange())
                .with(AppConstants.BOOKING_CANCELLED_ROUTING);
    }

    // ── Payment Success Queue (consumed by Booking Service) ──

    @Bean
    public Queue paymentSuccessQueue() {
        return QueueBuilder
                .durable("payment.success.queue")
                .withArgument("x-dead-letter-exchange", "showtime.dlx")
                .withArgument("x-dead-letter-routing-key",
                        "showtime.dead.letter")
                .build();
    }

    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder
                .bind(paymentSuccessQueue())
                .to(showtimeExchange())
                .with(AppConstants.PAYMENT_SUCCESS_ROUTING);
    }

    // ── Message Converter (JSON) ──

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}