package com.ofb.mock;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class ApiEndpointsTest {

    @Test
    public void testGetInvestmentsForExistingCustomer() {
        given()
        .when()
            .get("/api/investments/12345678901")
        .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("data.size()", equalTo(2))
            .body("data[0].investmentId", notNullValue())
            .body("data[0].productType", notNullValue());
    }

    @Test
    public void testGetInvestmentsForNonExistingCustomer() {
        given()
        .when()
            .get("/api/investments/99999999999")
        .then()
            .statusCode(404)
            .body("error", equalTo("Customer not found"));
    }

    @Test
    public void testGetCustomerByCpf() {
        given()
        .when()
            .get("/api/customers/12345678901")
        .then()
            .statusCode(200)
            .body("data.cpf", equalTo("12345678901"))
            .body("data.name", equalTo("João Silva"))
            .body("data.email", notNullValue());
    }

    @Test
    public void testGetCustomerNotFound() {
        given()
        .when()
            .get("/api/customers/99999999999")
        .then()
            .statusCode(404)
            .body("error", equalTo("Customer not found"));
    }

    @Test
    public void testGetTransactionsByCpf() {
        given()
        .when()
            .get("/api/transactions/12345678901")
        .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("data.size()", equalTo(2))
            .body("data[0].transactionId", notNullValue())
            .body("data[0].type", notNullValue())
            .body("data[0].amount", notNullValue());
    }

    @Test
    public void testGetTransactionsForNonExistingCustomer() {
        given()
        .when()
            .get("/api/transactions/99999999999")
        .then()
            .statusCode(404)
            .body("error", equalTo("Customer not found"));
    }

    @Test
    public void testMultipleCpfInvestments() {
        // Test different CPFs
        String[] cpfs = {"12345678901", "23456789012", "34567890123", "45678901234", "56789012345"};

        for (String cpf : cpfs) {
            given()
            .when()
                .get("/api/investments/" + cpf)
            .then()
                .statusCode(200)
                .body("data", notNullValue());
        }
    }
}
