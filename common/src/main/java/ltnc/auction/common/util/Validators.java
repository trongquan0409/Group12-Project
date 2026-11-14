package ltnc.auction.common.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Static helpers that validate and normalize input values shared across the model classes.
 *
 * <p>Methods named {@code require...} return the (possibly normalized) value so they can be used
 * directly in assignments, for example {@code this.name = requireNotBlank(name, "name")}.
 *
 * <p>Convention: a value that is present but invalid causes an {@link IllegalArgumentException};
 * a missing ({@code null}) value that is not handled explicitly causes a {@link
 * NullPointerException} via {@link Objects#requireNonNull(Object, String)}.
 */
public final class Validators {

    private Validators() {
    }

    /**
     * Checks that the text is neither {@code null} nor blank.
     *
     * @param value     the text to check
     * @param fieldName the field name used in the error message
     * @return the text with leading and trailing whitespace removed
     * @throws IllegalArgumentException if {@code value} is {@code null} or blank
     */
    public static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    /**
     * Normalizes an optional text.
     *
     * @param value the text, may be {@code null}
     * @return an empty string if {@code value} is {@code null}, otherwise the trimmed text
     */
    public static String trimToEmpty(String value) {
        return (value == null) ? "" : value.trim();
    }

    /**
     * Checks that the trimmed text does not exceed the maximum length.
     *
     * @param value     the text to check
     * @param max       the maximum number of characters allowed after trimming
     * @param fieldName the field name used in the error message
     * @return the trimmed text
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if the trimmed text is longer than {@code max}
     */
    public static String requireMaxLength(String value, int max, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + max + " characters");
        }
        return trimmed;
    }

    /**
     * Checks that the number is greater than zero.
     *
     * @param value     the number to check
     * @param fieldName the field name used in the error message
     * @return {@code value}
     * @throws IllegalArgumentException if {@code value} is less than or equal to zero
     */
    public static long requirePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive (larger than 0)");
        }
        return value;
    }

    /**
     * Checks that the number is not negative.
     *
     * @param value     the number to check
     * @param fieldName the field name used in the error message
     * @return {@code value}
     * @throws IllegalArgumentException if {@code value} is less than zero
     */
    public static int requireNotNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative (not less than 0)");
        }
        return value;
    }

    /**
     * Checks that the number lies within the inclusive range {@code [min, max]}.
     *
     * @param value     the number to check
     * @param min       the smallest allowed value
     * @param max       the largest allowed value
     * @param fieldName the field name used in the error message
     * @return {@code value}
     * @throws IllegalArgumentException if {@code min} is greater than {@code max}, or if {@code
     *                                  value} is outside the range
     */
    public static int requireInRange(int value, int min, int max, String fieldName) {
        if (min > max) {
            throw new IllegalArgumentException(
                    "Invalid range for " + fieldName + " : min (" + min + ") is greater than max (" + max + ")");
        }
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    fieldName + " must be in range [" + min + ", " + max + "]");
        }
        return value;
    }

    /**
     * Checks that the amount is greater than zero.
     *
     * @param value     the amount to check
     * @param fieldName the field name used in the error message
     * @return {@code value}
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is less than or equal to zero
     */
    public static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive (larger than 0)");
        }
        return value;
    }

    /**
     * Checks that the amount is not negative.
     *
     * @param value     the amount to check
     * @param fieldName the field name used in the error message
     * @return {@code value}
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is less than zero
     */
    public static BigDecimal requireNotNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative (not less than 0)");
        }
        return value;
    }

    /**
     * Checks that the end time is strictly after the start time.
     *
     * @param start the start time
     * @param end   the end time
     * @throws NullPointerException     if {@code start} or {@code end} is {@code null}
     * @throws IllegalArgumentException if {@code end} is not after {@code start}
     */
    public static void requireEndAfterStart(Instant start, Instant end) {
        Objects.requireNonNull(start, "Start time must not be null");
        Objects.requireNonNull(end, "End time must not be null");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}