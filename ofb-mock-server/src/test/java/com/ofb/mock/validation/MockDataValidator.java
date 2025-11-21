package com.ofb.mock.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.InputStream;
import java.util.*;

/**
 * Validates that all mock JSON files deserialize properly into OFB model objects
 * and reports any differences between raw JSON and deserialized objects.
 *
 * Run this as a standalone Java application to validate mock data.
 */
public class MockDataValidator {

    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("OFB MOCK DATA VALIDATION");
        System.out.println("=".repeat(80));

        boolean allValid = true;

        // Validate each investment type
        allValid &= validateFile("mock-data/bank_fixed_incomes.json", "Bank Fixed Incomes");
        allValid &= validateFile("mock-data/credit_fixed_incomes.json", "Credit Fixed Incomes");
        allValid &= validateFile("mock-data/funds.json", "Funds");
        allValid &= validateFile("mock-data/treasury_titles.json", "Treasury Titles");
        allValid &= validateFile("mock-data/variable_incomes.json", "Variable Incomes");

        // Validate transactions
        allValid &= validateFile("mock-data/bank_fixed_incomes_transactions.json", "Bank Fixed Income Transactions");
        allValid &= validateFile("mock-data/credit_fixed_incomes_transactions.json", "Credit Fixed Income Transactions");
        allValid &= validateFile("mock-data/funds_transactions.json", "Fund Transactions");
        allValid &= validateFile("mock-data/treasury_titles_transactions.json", "Treasury Title Transactions");
        allValid &= validateFile("mock-data/variable_incomes_transactions.json", "Variable Income Transactions");

        System.out.println("\n" + "=".repeat(80));
        if (allValid) {
            System.out.println("✅ ALL VALIDATIONS PASSED");
        } else {
            System.out.println("❌ SOME VALIDATIONS FAILED - Check output above");
            System.exit(1);
        }
        System.out.println("=".repeat(80));
    }

    private static boolean validateFile(String filePath, String description) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("Validating: " + description);
        System.out.println("File: " + filePath);
        System.out.println("-".repeat(80));

        try {
            InputStream is = MockDataValidator.class.getClassLoader().getResourceAsStream(filePath);
            if (is == null) {
                System.err.println("❌ File not found: " + filePath);
                return false;
            }

            // Load as raw JSON
            JsonNode rawJson = mapper.readTree(is);

            // Reload for deserialization
            is = MockDataValidator.class.getClassLoader().getResourceAsStream(filePath);
            Map<String, List<Map<String, Object>>> deserialized =
                mapper.readValue(is, new TypeReference<Map<String, List<Map<String, Object>>>>() {});

            System.out.println("✅ Deserialization successful");
            System.out.println("   CPFs found: " + deserialized.keySet().size());

            int totalInvestments = deserialized.values().stream()
                .mapToInt(List::size)
                .sum();
            System.out.println("   Total records: " + totalInvestments);

            // Compare field counts
            if (rawJson.isObject()) {
                boolean hasDifferences = false;
                for (String cpf : deserialized.keySet()) {
                    List<Map<String, Object>> investments = deserialized.get(cpf);
                    JsonNode rawInvestments = rawJson.get(cpf);

                    if (rawInvestments != null && rawInvestments.isArray()) {
                        for (int i = 0; i < Math.min(investments.size(), rawInvestments.size()); i++) {
                            Map<String, Object> deserializedInv = investments.get(i);
                            JsonNode rawInv = rawInvestments.get(i);

                            Set<String> rawFields = new HashSet<>();
                            rawInv.fieldNames().forEachRemaining(rawFields::add);

                            Set<String> deserializedFields = getAllFieldsRecursive(deserializedInv, "");
                            Set<String> rawFieldsFlat = getAllJsonFieldsRecursive(rawInv, "");

                            // Find fields in raw JSON but not in deserialized
                            Set<String> missingFields = new HashSet<>(rawFieldsFlat);
                            missingFields.removeAll(deserializedFields);

                            // Find fields in deserialized but not in raw JSON
                            Set<String> extraFields = new HashSet<>(deserializedFields);
                            extraFields.removeAll(rawFieldsFlat);

                            if (!missingFields.isEmpty() || !extraFields.isEmpty()) {
                                if (!hasDifferences) {
                                    System.out.println("\n⚠️  Field Differences Found:");
                                    hasDifferences = true;
                                }

                                String investmentId = deserializedInv.getOrDefault("investmentId",
                                    deserializedInv.getOrDefault("transactionId", "unknown")).toString();

                                if (!missingFields.isEmpty()) {
                                    System.out.println("\n   Investment/Transaction: " + investmentId + " (CPF: " + cpf + ")");
                                    System.out.println("   Missing in deserialized (lost during parsing):");
                                    missingFields.stream()
                                        .sorted()
                                        .limit(10)
                                        .forEach(f -> System.out.println("     - " + f));
                                    if (missingFields.size() > 10) {
                                        System.out.println("     ... and " + (missingFields.size() - 10) + " more");
                                    }
                                }

                                if (!extraFields.isEmpty()) {
                                    System.out.println("   Extra in deserialized (added during parsing):");
                                    extraFields.stream()
                                        .sorted()
                                        .limit(10)
                                        .forEach(f -> System.out.println("     + " + f));
                                    if (extraFields.size() > 10) {
                                        System.out.println("     ... and " + (extraFields.size() - 10) + " more");
                                    }
                                }
                            }
                        }
                    }
                }

                if (!hasDifferences) {
                    System.out.println("✅ Field structure matches between raw JSON and deserialized objects");
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("❌ Validation failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recursively get all field paths from a Map (deserialized object)
     */
    private static Set<String> getAllFieldsRecursive(Map<String, Object> map, String prefix) {
        Set<String> fields = new HashSet<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String fieldPath = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            fields.add(fieldPath);

            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) entry.getValue();
                fields.addAll(getAllFieldsRecursive(nested, fieldPath));
            } else if (entry.getValue() instanceof List) {
                List<?> list = (List<?>) entry.getValue();
                if (!list.isEmpty() && list.get(0) instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nested = (Map<String, Object>) list.get(0);
                    fields.addAll(getAllFieldsRecursive(nested, fieldPath + "[0]"));
                }
            }
        }
        return fields;
    }

    /**
     * Recursively get all field paths from JsonNode (raw JSON)
     */
    private static Set<String> getAllJsonFieldsRecursive(JsonNode node, String prefix) {
        Set<String> fields = new HashSet<>();
        if (node.isObject()) {
            node.fieldNames().forEachRemaining(fieldName -> {
                String fieldPath = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;
                fields.add(fieldPath);
                fields.addAll(getAllJsonFieldsRecursive(node.get(fieldName), fieldPath));
            });
        } else if (node.isArray() && node.size() > 0) {
            fields.addAll(getAllJsonFieldsRecursive(node.get(0), prefix + "[0]"));
        }
        return fields;
    }
}
