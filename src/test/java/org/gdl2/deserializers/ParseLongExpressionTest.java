package org.gdl2.deserializers;


import org.gdl2.expression.ExpressionItem;
import org.gdl2.expression.LongExpression;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class ParseLongExpressionTest {
    private ExpressionItemDeserializer deserializer = new ExpressionItemDeserializer();
    private ExpressionItem expressionItem;

    @Test
    public void can_parse_long_addition_expression() throws Exception {
        expressionItem = deserializer.parse("$gt0002 + $gt0003 + $gt0004 + $gt0005");
        assertThat(expressionItem, instanceOf(LongExpression.class));
        LongExpression longExpression = (LongExpression) expressionItem;
        assertThat(longExpression.getItems().size(), is(4));
    }

    @Test
    public void can_parse_long_or_expression() throws Exception {
        expressionItem = deserializer.parse("$gt0002 || $gt0003 || $gt0004");
        assertThat(expressionItem, instanceOf(LongExpression.class));
        LongExpression longExpression = (LongExpression) expressionItem;
        assertThat(longExpression.getItems().size(), is(3));
    }
}
