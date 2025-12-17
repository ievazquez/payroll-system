package mx.payroll.system.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PAYROLL_QUEUE = "payroll_queue";

    @Bean
    public Queue payrollQueue() {
        // durable=true: La cola sobrevive a reinicios de RabbitMQ
        // exclusive=false: Puede ser accedida por múltiples conexiones
        // autoDelete=false: No se elimina cuando no hay consumidores
        return new Queue(PAYROLL_QUEUE, true, false, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
