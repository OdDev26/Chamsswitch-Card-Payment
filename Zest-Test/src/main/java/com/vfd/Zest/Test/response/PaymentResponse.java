package com.vfd.Zest.Test.response;

import lombok.Data;

@Data
public class PaymentResponse<T> {
    private String status;
    private boolean success;
    private com.vfd.Zest.Test.response.Data data;
    private String message;
    private T errors;
}
