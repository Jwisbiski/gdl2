package org.gdl2.expression;

import org.gdl2.deserializers.ExpressionItemDeserializer;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LongExpressionTest {
    private ExpressionItemDeserializer deserializer = new ExpressionItemDeserializer();
    private LongExpression longExpression;
    private BinaryExpression binaryExpressions;

    @Test
    public void can_handle_two_additions() {
        longExpression = parse("$gt0002 + $gt0003 + $gt0004");
        assertThat(longExpression.toString(), is("$gt0002+$gt0003+$gt0004"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("($gt0002+$gt0003)+$gt0004"));
    }

    @Test
    public void can_handle_two_additions_with_number() {
        longExpression = parse("$gt0002 + $gt0003 + 1200");
        assertThat(longExpression.toString(), is("$gt0002+$gt0003+1200"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("($gt0002+$gt0003)+1200"));
    }

    @Test
    public void can_handle_three_additions() {
        longExpression = parse("$gt0002 + $gt0003 + $gt0004 + $gt0005");
        assertThat(longExpression.toString(), is("$gt0002+$gt0003+$gt0004+$gt0005"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("(($gt0002+$gt0003)+$gt0004)+$gt0005"));
    }

    @Test
    public void can_handle_three_logical_or() {
        longExpression = parse("$gt0002 || $gt0003 || $gt0004 || $gt0005");
        assertThat(longExpression.toString(), is("$gt0002||$gt0003||$gt0004||$gt0005"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("(($gt0002||$gt0003)||$gt0004)||$gt0005"));
    }

    @Test
    public void can_handle_three_logical_and() {
        longExpression = parse("$gt0002 && $gt0003 && $gt0004 && $gt0005");
        assertThat(longExpression.toString(), is("$gt0002&&$gt0003&&$gt0004&&$gt0005"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("(($gt0002&&$gt0003)&&$gt0004)&&$gt0005"));
    }

    @Test
    public void can_handle_addition_multiplication() {
        longExpression = parse("$gt0002 + $gt0003 * $gt0004");
        assertThat(longExpression.toString(), is("$gt0002+$gt0003*$gt0004"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("$gt0002+($gt0003*$gt0004)"));
    }

    @Test
    public void can_handle_addition_multiplication_division() {
        longExpression = parse("$gt0002 + $gt0003 * $gt0004 - $gt0005");
        assertThat(longExpression.toString(), is("$gt0002+$gt0003*$gt0004-$gt0005"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("($gt0002+($gt0003*$gt0004))-$gt0005"));
    }

    @Test
    public void can_handle_multiplication_subtraction_division_addition_multiplication() {
        longExpression = parse("$gt0002 * $gt0003 - $gt0004 / $gt0005 + $gt0006 * $gt0007");
        assertThat(longExpression.toString(), is("$gt0002*$gt0003-$gt0004/$gt0005+$gt0006*$gt0007"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("(($gt0002*$gt0003)-($gt0004/$gt0005))+($gt0006*$gt0007)"));
    }

    @Test
    public void can_handle_addition_subtraction_division_multiplication_reminder() {
        longExpression = parse("$gt0002 + $gt0003 - $gt0004 / $gt0005 * $gt0006 % $gt0007");
        assertThat(longExpression.toString(), is("$gt0002+$gt0003-$gt0004/$gt0005*$gt0006%$gt0007"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("($gt0002+$gt0003)-((($gt0004/$gt0005)*$gt0006)%$gt0007)"));
    }

    @Test
    public void can_handle_addition_subtraction_division_multiplication_reminder_with_constants() {
        longExpression = parse("$gt0002 + 0.317 - $gt0004 / 100 * $gt0006 % 2");
        assertThat(longExpression.toString(), is("$gt0002+0.317-$gt0004/100*$gt0006%2"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("($gt0002+0.317)-((($gt0004/100)*$gt0006)%2)"));
    }

    @Test
    public void can_handle_addition_exponentiation_subtraction_division_multiplication_reminder_with_constants() {
        longExpression = parse("$gt0002 + 0.317 ^ $gt0003 - $gt0004 / 100 * $gt0006 % 2");
        assertThat(longExpression.toString(), is("$gt0002+0.317^$gt0003-$gt0004/100*$gt0006%2"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("($gt0002+(0.317^$gt0003))-((($gt0004/100)*$gt0006)%2)"));
    }

    @Test
    public void can_handle_addition_exponentiation() {
        longExpression = parse("$gt0002 + $gt0003 ^ 2");
        assertThat(longExpression.toString(), is("$gt0002+$gt0003^2"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("$gt0002+($gt0003^2)"));
    }

    private LongExpression parse(String expression) {
        try {
            ExpressionItem expressionItem = deserializer.parse(expression);
            return (LongExpression) expressionItem;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
