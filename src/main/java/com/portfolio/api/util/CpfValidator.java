package com.portfolio.api.util;

/**
 * Validador de CPF (Cadastro de Pessoas Físicas) segundo algoritmo oficial brasileiro.
 */
public class CpfValidator {

    private CpfValidator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Valida um CPF usando o algoritmo de checksum oficial.
     *
     * @param cpf CPF a ser validado (com ou sem formatação)
     * @return true se válido, false caso contrário
     */
    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.isEmpty()) {
            return false;
        }

        String cleanCpf = cpf.replaceAll("[^0-9]", "");

        if (cleanCpf.length() != 11) {
            return false;
        }

        if (cleanCpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(cleanCpf.charAt(i)) * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit >= 10) {
                firstDigit = 0;
            }

            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(cleanCpf.charAt(i)) * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit >= 10) {
                secondDigit = 0;
            }

            return firstDigit == Character.getNumericValue(cleanCpf.charAt(9))
                    && secondDigit == Character.getNumericValue(cleanCpf.charAt(10));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Remove formatação do CPF e retorna apenas dígitos.
     *
     * @param cpf CPF formatado
     * @return CPF apenas com dígitos
     */
    public static String normalize(String cpf) {
        return cpf == null ? null : cpf.replaceAll("[^0-9]", "");
    }

    /**
     * Formata CPF para padrão XXX.XXX.XXX-XX.
     *
     * @param cpf CPF sem formatação
     * @return CPF formatado
     */
    public static String format(String cpf) {
        String cleanCpf = normalize(cpf);
        if (cleanCpf == null || cleanCpf.length() != 11) {
            return cpf;
        }
        return String.format("%s.%s.%s-%s",
                cleanCpf.substring(0, 3),
                cleanCpf.substring(3, 6),
                cleanCpf.substring(6, 9),
                cleanCpf.substring(9, 11)
        );
    }

    /**
     * Mascara CPF para exibição segura (XXX.XXX.XXX-**).
     *
     * @param cpf CPF a ser mascarado
     * @return CPF mascarado
     */
    public static String mask(String cpf) {
        String cleanCpf = normalize(cpf);
        if (cleanCpf == null || cleanCpf.length() != 11) {
            return "***.***.***-**";
        }
        return String.format("%s.%s.%s-**",
                cleanCpf.substring(0, 3),
                cleanCpf.substring(3, 6),
                cleanCpf.substring(6, 9)
        );
    }
}
