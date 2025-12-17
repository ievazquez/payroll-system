package mx.payroll.system.processing.dispatcher;

public class PayrollChunkJob {
    private String periodId;
    private int page;
    private int pageSize;

    public PayrollChunkJob() {
    }

    public PayrollChunkJob(String periodId, int page, int pageSize) {
        this.periodId = periodId;
        this.page = page;
        this.pageSize = pageSize;
    }

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "PayrollChunkJob{" +
                "periodId='" + periodId + '\'' +
                ", page=" + page +
                ", pageSize=" + pageSize +
                '}';
    }
}
