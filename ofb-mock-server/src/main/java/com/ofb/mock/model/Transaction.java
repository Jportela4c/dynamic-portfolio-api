package com.ofb.mock.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Transaction {
    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String type;
    private BigDecimal amount;
}
