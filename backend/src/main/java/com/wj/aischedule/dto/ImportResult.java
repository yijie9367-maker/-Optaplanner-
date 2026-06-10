package com.wj.aischedule.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {

    private int successCount;
    private int failCount;
    private List<String> errors;

    public ImportResult() {
        this.successCount = 0;
        this.failCount = 0;
        this.errors = new ArrayList<>();
    }

    public void addSuccess() {
        this.successCount++;
    }

    public void addError(int row, String reason) {
        this.failCount++;
        this.errors.add("第" + row + "行: " + reason);
    }

    public int getSuccessCount() { return successCount; }
    public int getFailCount() { return failCount; }
    public List<String> getErrors() { return errors; }
}
