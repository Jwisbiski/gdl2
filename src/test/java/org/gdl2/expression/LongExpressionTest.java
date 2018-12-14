package org.gdl2.expression;

import org.gdl2.deserializers.ExpressionItemDeserializer;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;

public class LongExpressionTest {
    private ExpressionItemDeserializer deserializer = new ExpressionItemDeserializer();
    private LongExpression longExpression;
    private BinaryExpression binaryExpressions;

    @Test
    public void can_handle_single_addition() {
        longExpression = parse("$gt0002 + $gt0003");
        assertThat(longExpression.toString(), is("$gt0002+$gt0003"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("$gt0002+$gt0003"));
    }

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

    @Test
    public void can_sort_logicAnd_logicOr() {
        longExpression = parse("$gt0001 || $gt0002 && $gt0003");
        assertThat(longExpression.toString(), is("$gt0001||$gt0002&&$gt0003"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("$gt0001||($gt0002&&$gt0003)"));
    }

    @Test
    public void can_sort_less_than_addition() {
        longExpression = parse("$gt0001 < $gt0002 + $gt0003");
        assertThat(longExpression.toString(), is("$gt0001<$gt0002+$gt0003"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("$gt0001<($gt0002+$gt0003)"));
    }

    @Test
    public void can_sort_operators_from_all_precedence_levels() {
        longExpression = parse("$gt0001 || $gt0002 && $gt0003 == $gt0004 <= $gt0005 + $gt0006 * $gt0007^2");
        assertThat(longExpression.toString(), is("$gt0001||$gt0002&&$gt0003==$gt0004<=$gt0005+$gt0006*$gt0007^2"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("$gt0001||($gt0002&&($gt0003==($gt0004<=($gt0005+($gt0006*($gt0007^2))))))"));
    }

    @Test
    public void can_handle_function() {
        longExpression = parse("2.56-0.926*log($gt0008)+1.6");
        assertThat(longExpression.toString(), is("2.56-0.926*log($gt0008)+1.6"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("(2.56-(0.926*log($gt0008)))+1.6"));
    }

    @Test
    public void can_handle_parentheses_within_expression() {
        longExpression = parse("2.56-0.926*($gt0008.magnitude/180)+1.6");
        assertThat(longExpression.toString(), is("2.56-0.926*($gt0008.magnitude/180)+1.6"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("(2.56-(0.926*($gt0008.magnitude/180)))+1.6"));
    }

    @Test
    public void can_handle_both_function_and_parentheses() {
        longExpression = parse("2.56-0.926*log($gt0008/180)+1.6");
        assertThat(longExpression.toString(), is("2.56-0.926*log($gt0008/180)+1.6"));
        binaryExpressions = longExpression.toBinaryExpression();
        assertThat(binaryExpressions.toString(), is("(2.56-(0.926*log($gt0008/180)))+1.6"));
    }

    @Test
    public void can_check_equality_based_on_value() {
        longExpression = parse("2.56-0.926*log($gt0008)+1.6");
        LongExpression longExpression2 = parse("2.56-0.926*log($gt0008)+1.6");
        assertThat(longExpression.equals(longExpression2), is(true));
        assertThat(longExpression2.equals(longExpression), is(true));
    }

    @Test
    public void can_handle_body_surface_area() {
        longExpression = parse("($gt0005.magnitude*$gt0006.magnitude/3600)^0.5");
        assertThat(longExpression.toString(), is("($gt0005.magnitude*$gt0006.magnitude/3600)^0.5"));
    }

    @Test
    public void can_handle_Revised_Lund_Malmo_Study_equation() {
        longExpression = parse("e^(2.56-0.926*log($gt0008.magnitude/180)) - 0.0158*$gt0005.magnitude + 0.438*log($gt0005.magnitude)");
        assertThat(longExpression.toString(), is("e^(2.56-0.926*log($gt0008.magnitude/180))-0.0158*$gt0005.magnitude+0.438*log($gt0005.magnitude)"));
    }

    @Test // TODO need to fix parenthesis around negative number
    public void can_handle_CKD_EPI_Study_equation() {
        longExpression = parse("144*($gt0003.magnitude/62)^(-0.329)*0.993^($currentDateTime.year-$gt0008.year)");
        assertThat(longExpression.toString(), is("144*($gt0003.magnitude/62)^-0.329*0.993^($currentDateTime.year-$gt0008.year)"));
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

    @Test
    public void can_handle_mixed_logicAnd_logicOr_comparison() {
        ExpressionItem expressionItem = justParse("($gt0003|typeIIDiabetesDiagnosis|!=null&&$gt0006|hypertensionDiagnosis|==null)||$gt0009|heartFailureDiagnosis|!=null");
        assertEquals(expressionItem.toString(), "($gt0003|typeIIDiabetesDiagnosis|!=null&&$gt0006|hypertensionDiagnosis|==null)||$gt0009|heartFailureDiagnosis|!=null");
    }

    @Test
    public void can_handle_datetime_with_addition_of_days() {
        ExpressionItem expressionItem = justParse("$gt0003>$currentDateTime+4,d");
        assertEquals(expressionItem.toString(), "$gt0003>$currentDateTime+4,d");
    }

    @Test
    public void should_not_modify_long_expression_when_creating_binary_expression() {
        String expressionString = "$gt0113<=$currentDateTime-65,a";
        longExpression = parse(expressionString);
        longExpression.toBinaryExpression();
        assertEquals(longExpression.toString(), expressionString);
    }

    @Test
    public void should_not_remove_parenthesis_from_expression() {
        String expression = "($gt0085|currentSmokingStatus| is_a local::gt0082|Occasional smoker|)||(($gt0085|currentSmokingStatus| is_a local::gt0083|Moderate smoker|)||($gt0085|currentSmokingStatus| is_a local::gt0084|Heavy smoker|))";
        LongExpression longExpression = parse(expression);
        assertEquals(longExpression.toString(), expression);
    }

    private ExpressionItem justParse(String expression) {
        try {
            return deserializer.parse(expression);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}