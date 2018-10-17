package org.gdl2.deserializers;


import org.gdl2.expression.ExpressionItem;
import org.gdl2.expression.LongExpression;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class ParseLongExpressionTest {
    private ExpressionItemDeserializer deserializer = new ExpressionItemDeserializer();
    private ExpressionItem expressionItem;
    private List<LongExpression.OperandPair> items;

    @Test
    public void can_parse_long_addition_expression() throws Exception {
        expressionItem = deserializer.parse("$gt0002 + $gt0003 + $gt0004 + $gt0005");
        assertThat(expressionItem, instanceOf(LongExpression.class));
        LongExpression longExpression = (LongExpression) expressionItem;
        items = longExpression.getItems();
        assertThat(items.size(), is(4));
        assertThat(items.get(0).getOperator().getSymbol(), is("+"));
        assertThat(items.get(0).getExpressionItem().toString(), is("$gt0002"));
        assertThat(items.get(1).getOperator().getSymbol(), is("+"));
        assertThat(items.get(1).getExpressionItem().toString(), is("$gt0003"));
        assertThat(items.get(2).getOperator().getSymbol(), is("+"));
        assertThat(items.get(2).getExpressionItem().toString(), is("$gt0004"));
        assertThat(items.get(3).getExpressionItem().toString(), is("$gt0005"));
        assertThat(items.get(3).isLast(), is(true));
    }

    @Test
    public void can_parse_logic_or_expression() throws Exception {
        expressionItem = deserializer.parse("$gt0002 || $gt0003 || $gt0004");
        assertThat(expressionItem, instanceOf(LongExpression.class));
        LongExpression longExpression = (LongExpression) expressionItem;
        items = longExpression.getItems();
        assertThat(items.size(), is(3));
        assertThat(items.get(0).getOperator().getSymbol(), is("||"));
        assertThat(items.get(0).getExpressionItem().toString(), is("$gt0002"));
    }

    @Test
    public void can_parse_logic_and_expression() throws Exception {
        expressionItem = deserializer.parse("$gt0002 && $gt0003 && $gt0004 && $gt0005 && $gt0006");
        assertThat(expressionItem, instanceOf(LongExpression.class));
        LongExpression longExpression = (LongExpression) expressionItem;
        items = longExpression.getItems();
        assertThat(items.size(), is(5));
        assertThat(items.get(0).getOperator().getSymbol(), is("&&"));
        assertThat(items.get(0).getExpressionItem().toString(), is("$gt0002"));
        assertThat(items.get(4).getExpressionItem().toString(), is("$gt0006"));
    }

    @Test
    public void can_parse_multiple_operators_expression() throws Exception {
        expressionItem = deserializer.parse("$gt0002 + $gt0003 - $gt0004 * $gt0005 / $gt0006 ^ $gt0006 % $gt0007");
        assertThat(expressionItem, instanceOf(LongExpression.class));
        LongExpression longExpression = (LongExpression) expressionItem;
        items = longExpression.getItems();
        assertThat(items.size(), is(7));
        assertThat(items.get(0).getOperator().getSymbol(), is("+"));
        assertThat(items.get(1).getOperator().getSymbol(), is("-"));
        assertThat(items.get(2).getOperator().getSymbol(), is("*"));
        assertThat(items.get(3).getOperator().getSymbol(), is("/"));
        assertThat(items.get(4).getOperator().getSymbol(), is("^"));
        assertThat(items.get(5).getOperator().getSymbol(), is("%"));
    }
}
