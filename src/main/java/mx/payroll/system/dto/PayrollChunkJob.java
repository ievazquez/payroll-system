// DTO que representa un lote de trabajo
package mx.payroll.system.dto;

public class PayrollChunkJob {
    private String periodId;
    private int pageNumber;
    private int pageSize;

    // Constructor, Getters, Setters
    public PayrollChunkJob(String periodId, int pageNumber, int pageSize) {
        this.periodId = periodId;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }
}
