package org.gdl2.expression;

import org.gdl2.expression.parser.ExpressionParser;
import org.testng.annotations.Test;

import java.io.StringReader;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertNotNull;

public class NegativeConstantNumberTest {

    @Test
    public void can_parse_expression_with_negative_integer() {
        String value = "$gt1000.magnitude^(-2)";
        ExpressionItem expressionItem = parseExpression(value);
        assertNotNull(expressionItem);
        assertThat(((IntegerConstant)((BinaryExpression) expressionItem).getRight()).getIntegerValue(), is(-2));
    }

    @Test
    public void can_parse_expression_with_negative_double() {
        String value = "$gt1000.magnitude^(-0.5)";
        ExpressionItem expressionItem = parseExpression(value);
        assertNotNull(expressionItem);
        assertThat(((DoubleConstant)((BinaryExpression) expressionItem).getRight()).getDoubleValue(), is(-0.5));
    }

    ExpressionItem parseExpression(String expression) {
        try {
            return new ExpressionParser(new StringReader(expression)).parse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
