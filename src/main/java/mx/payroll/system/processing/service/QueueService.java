package mx.payroll.system.processing.service;

import mx.payroll.system.processing.dispatcher.PayrollChunkJob;

public interface QueueService {
    void push(PayrollChunkJob job);
}
