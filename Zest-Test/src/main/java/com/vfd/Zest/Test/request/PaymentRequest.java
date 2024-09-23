package com.vfd.Zest.Test.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PaymentRequest {
    private String amount;
    private String reference;
    private boolean  useExistingCard;
    private String cardNumber;
    private String cardPin;
    private String cvv2;
    private String expiryDate;
    private boolean shouldTokenize;
    private String email;
    private String otp;
    private String expiryMonth;
    private String narration;
    private String firstname;
    private String lastname;
    private String middlename;
    private String phone;
    private String address;
    private String city;
    private String stateCode;
    private String postalCode;
    private String expiryYear;

}
