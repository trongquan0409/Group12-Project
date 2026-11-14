package ltnc.auction.common.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit test class for {@link Validators}.
 * Tests all shared validation and normalization helper methods.
 */
@DisplayName("Validators Unit Tests")
class ValidatorsTest {

    /**
     * Nested test class for string-related validations and normalizations.
     */
    @Nested
    @DisplayName("String Validation Tests")
    class StringValidationTests {

        @Test
        @DisplayName("requireNotBlank returns trimmed text when value is valid")
        void requireNotBlank_validInput_returnsTrimmedString() {
            // Given a valid string with leading and trailing whitespaces
            String input = "  item name  ";

            // When validating with requireNotBlank
            String result = Validators.requireNotBlank(input, "name");

            // Then it should return the trimmed string
            assertEquals("item name", result);
        }

        @Test
        @DisplayName("requireNotBlank throws IllegalArgumentException when text is null, empty, or blank")
        void requireNotBlank_invalidInput_throwsIllegalArgumentException() {
            // Expect exception for null, empty, or whitespace-only strings
            assertThrows(IllegalArgumentException.class, () -> Validators.requireNotBlank(null, "name"));
            assertThrows(IllegalArgumentException.class, () -> Validators.requireNotBlank("", "name"));
            assertThrows(IllegalArgumentException.class, () -> Validators.requireNotBlank("   ", "name"));
        }

        @Test
        @DisplayName("trimToEmpty normalizes null or blank strings to empty string, and trims valid strings")
        void trimToEmpty_variousInputs_returnsNormalizedString() {
            // Valid string gets trimmed
            assertEquals("auction", Validators.trimToEmpty("  auction  "));
            // Null input becomes empty string
            assertEquals("", Validators.trimToEmpty(null));
            // Blank string becomes empty string
            assertEquals("", Validators.trimToEmpty("   "));
        }

        @Test
        @DisplayName("requireMaxLength returns trimmed string when within limit")
        void requireMaxLength_validInput_returnsTrimmedString() {
            // String length is within the max limit after trimming
            String result = Validators.requireMaxLength("  code  ", 10, "productCode");
            assertEquals("code", result);
        }

        @Test
        @DisplayName("requireMaxLength throws IllegalArgumentException when trimmed length exceeds max limit")
        void requireMaxLength_exceedsLength_throwsIllegalArgumentException() {
            // Trimmed length (6) exceeds max limit (5)
            assertThrows(
                    IllegalArgumentException.class,
                    () -> Validators.requireMaxLength("123456", 5, "productCode"));
        }

        @Test
        @DisplayName("requireMaxLength throws NullPointerException when input is null")
        void requireMaxLength_nullInput_throwsNullPointerException() {
            // Null input should trigger NullPointerException
            assertThrows(
                    NullPointerException.class,
                    () -> Validators.requireMaxLength(null, 5, "productCode"));
        }
    }

    /**
     * Nested test class for primitive numeric validations.
     */
    @Nested
    @DisplayName("Numeric Primitive Validation Tests")
    class PrimitiveNumericValidationTests {

        @Test
        @DisplayName("requirePositive long returns value when strictly greater than zero")
        void requirePositive_positiveLong_returnsValue() {
            // Value is strictly greater than zero
            assertEquals(100L, Validators.requirePositive(100L, "id"));
        }

        @Test
        @DisplayName("requirePositive long throws IllegalArgumentException when value is zero or negative")
        void requirePositive_nonPositiveLong_throwsIllegalArgumentException() {
            // Zero and negative values are invalid for positive check
            assertThrows(IllegalArgumentException.class, () -> Validators.requirePositive(0L, "id"));
            assertThrows(IllegalArgumentException.class, () -> Validators.requirePositive(-10L, "id"));
        }

        @Test
        @DisplayName("requireNotNegative int returns value when zero or positive")
        void requireNotNegative_validInt_returnsValue() {
            // Zero and positive values are valid for non-negative check
            assertEquals(0, Validators.requireNotNegative(0, "count"));
            assertEquals(15, Validators.requireNotNegative(15, "count"));
        }

        @Test
        @DisplayName("requireNotNegative int throws IllegalArgumentException when value is negative")
        void requireNotNegative_negativeInt_throwsIllegalArgumentException() {
            // Negative value should trigger exception
            assertThrows(IllegalArgumentException.class, () -> Validators.requireNotNegative(-1, "count"));
        }

        @Test
        @DisplayName("requireInRange int returns value when within inclusive range")
        void requireInRange_validValue_returnsValue() {
            // Values inside or at boundaries of the [1, 10] range
            assertEquals(5, Validators.requireInRange(5, 1, 10, "score"));
            assertEquals(1, Validators.requireInRange(1, 1, 10, "score"));
            assertEquals(10, Validators.requireInRange(10, 1, 10, "score"));
        }

        @Test
        @DisplayName("requireInRange int throws IllegalArgumentException when value is out of bounds or min > max")
        void requireInRange_invalidInputs_throwsIllegalArgumentException() {
            // Value is outside the [1, 10] range
            assertThrows(IllegalArgumentException.class, () -> Validators.requireInRange(0, 1, 10, "score"));
            assertThrows(
                    IllegalArgumentException.class, () -> Validators.requireInRange(11, 1, 10, "score"));

            // Invalid range configuration where min > max
            assertThrows(
                    IllegalArgumentException.class, () -> Validators.requireInRange(5, 10, 1, "score"));
        }
    }

    /**
     * Nested test class for BigDecimal validations.
     */
    @Nested
    @DisplayName("BigDecimal Validation Tests")
    class BigDecimalValidationTests {

        @Test
        @DisplayName("requirePositive BigDecimal returns value when greater than zero")
        void requirePositive_positiveBigDecimal_returnsValue() {
            BigDecimal positiveVal = new BigDecimal("10.50");
            assertEquals(positiveVal, Validators.requirePositive(positiveVal, "price"));
        }

        @Test
        @DisplayName("requirePositive BigDecimal throws exception when null, zero, or negative")
        void requirePositive_invalidBigDecimal_throwsExpectedExceptions() {
            // Null should trigger NullPointerException
            assertThrows(
                    NullPointerException.class,
                    () -> Validators.requirePositive((BigDecimal) null, "price"));
            // Zero and negative should trigger IllegalArgumentException
            assertThrows(
                    IllegalArgumentException.class,
                    () -> Validators.requirePositive(BigDecimal.ZERO, "price"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> Validators.requirePositive(new BigDecimal("-0.01"), "price"));
        }

        @Test
        @DisplayName("requireNotNegative BigDecimal returns value when zero or positive")
        void requireNotNegative_validBigDecimal_returnsValue() {
            assertEquals(BigDecimal.ZERO, Validators.requireNotNegative(BigDecimal.ZERO, "bidAmount"));
            BigDecimal positiveVal = new BigDecimal("50.00");
            assertEquals(positiveVal, Validators.requireNotNegative(positiveVal, "bidAmount"));
        }

        @Test
        @DisplayName("requireNotNegative BigDecimal throws exception when null or negative")
        void requireNotNegative_invalidBigDecimal_throwsExpectedExceptions() {
            // Null triggers NullPointerException
            assertThrows(
                    NullPointerException.class,
                    () -> Validators.requireNotNegative((BigDecimal) null, "bidAmount"));
            // Negative triggers IllegalArgumentException
            assertThrows(
                    IllegalArgumentException.class,
                    () -> Validators.requireNotNegative(new BigDecimal("-1.00"), "bidAmount"));
        }
    }

    /**
     * Nested test class for temporal/Instant validations.
     */
    @Nested
    @DisplayName("Temporal Validation Tests")
    class TemporalValidationTests {

        @Test
        @DisplayName("requireEndAfterStart passes when end instant is strictly after start instant")
        void requireEndAfterStart_validTimes_doesNotThrow() {
            Instant start = Instant.now();
            Instant end = start.plusSeconds(3600); // 1 hour later

            // Should pass without throwing any exception
            assertDoesNotThrow(() -> Validators.requireEndAfterStart(start, end));
        }

        @Test
        @DisplayName("requireEndAfterStart throws IllegalArgumentException when end is before or equal to start")
        void requireEndAfterStart_invalidTimeOrder_throwsIllegalArgumentException() {
            Instant start = Instant.now();
            Instant equalEnd = start;
            Instant earlierEnd = start.minusSeconds(10);

            // Equal or earlier end times are invalid
            assertThrows(
                    IllegalArgumentException.class, () -> Validators.requireEndAfterStart(start, equalEnd));
            assertThrows(
                    IllegalArgumentException.class, () -> Validators.requireEndAfterStart(start, earlierEnd));
        }

        @Test
        @DisplayName("requireEndAfterStart throws NullPointerException when start or end is null")
        void requireEndAfterStart_nullTimeInputs_throwsNullPointerException() {
            Instant now = Instant.now();

            // Null checks for temporal parameters
            assertThrows(
                    NullPointerException.class, () -> Validators.requireEndAfterStart(null, now));
            assertThrows(
                    NullPointerException.class, () -> Validators.requireEndAfterStart(now, null));
        }
    }
}