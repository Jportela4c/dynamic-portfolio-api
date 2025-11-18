package com.ofb.mock.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MockData {
    private Map<String, List<Investment>> investments;
    private Map<String, Customer> customers;
    private Map<String, List<Transaction>> transactions;
}
