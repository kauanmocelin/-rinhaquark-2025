package dev.kauanmocelin.rinhaquark.payments.repository;

public enum PaymentProcessorType {
    DEFAULT(0),
    FALLBACK(1);

    private final int code;

    PaymentProcessorType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PaymentProcessorType fromCode(int code) {
        for (PaymentProcessorType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
