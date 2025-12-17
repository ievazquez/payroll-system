package mx.payroll.system.processing.service;

import mx.payroll.system.config.RabbitMQConfig;
import mx.payroll.system.processing.dispatcher.PayrollChunkJob;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitQueueService implements QueueService {

    private final RabbitTemplate rabbitTemplate;

    public RabbitQueueService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void push(PayrollChunkJob job) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.PAYROLL_QUEUE, job);
        System.out.println("Job pushed to RabbitMQ: " + job);
    }
}
