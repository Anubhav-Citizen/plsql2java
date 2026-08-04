package com.plsql2java.cli;

public enum ExitCode {
    SUCCESS(0),
    PARTIAL_SUCCESS(1),
    VALIDATION_ERROR(2),
    EXECUTION_ERROR(3),
    IO_ERROR(4);

    private final int code;

    ExitCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
