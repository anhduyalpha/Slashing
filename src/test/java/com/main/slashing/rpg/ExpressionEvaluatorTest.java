package com.main.slashing.rpg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionEvaluatorTest {
    @Test
    void evaluatesWithOperatorPrecedence() {
        assertEquals(14d, ExpressionEvaluator.evaluate("2+3*4"));
    }

    @Test
    void evaluatesWithParentheses() {
        assertEquals(20d, ExpressionEvaluator.evaluate("(2+3)*4"));
    }

    @Test
    void handlesDivisionByZero() {
        assertEquals(0d, ExpressionEvaluator.evaluate("10/0"));
    }

    @Test
    void evaluatesWithWhitespaceAndDecimals() {
        assertEquals(7.5d, ExpressionEvaluator.evaluate(" 1.5 + 6 "));
    }
}
