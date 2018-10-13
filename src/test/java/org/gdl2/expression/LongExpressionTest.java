package org.gdl2.expression;

import org.gdl2.deserializers.ExpressionItemDeserializer;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.AssertJUnit.fail;

public class LongExpressionTest {
    private ExpressionItemDeserializer deserializer = new ExpressionItemDeserializer();
    private LongExpression longExpression;
    private List<BinaryExpression> binaryExpressions;

    @Test
    public void can_parse_long_addition_expression() {
        longExpression = parse("$gt0002 + $gt0003 + $gt0004 + $gt0005");
        binaryExpressions = longExpression.toBooleanExpressions();
        assertThat(binaryExpressions.size(), is(3));
    }

    private LongExpression parse(String expression) {
        try {
            ExpressionItem expressionItem = deserializer.parse(expression);
            return (LongExpression) expressionItem;
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }
}
