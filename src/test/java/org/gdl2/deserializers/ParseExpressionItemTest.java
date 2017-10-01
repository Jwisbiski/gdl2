package org.gdl2.deserializers;


import org.gdl2.expression.ExpressionItem;
import org.gdl2.expression.OperatorKind;
import org.gdl2.expression.UnaryExpression;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class ParseExpressionItemTest {
    private ExpressionItemDeserializer deserializer = new ExpressionItemDeserializer();
    private ExpressionItem expressionItem;

    @Test
    public void can_parse_expression_item() throws Exception {
        expressionItem = deserializer.parse("max(/data/events/time)");
        assertThat(expressionItem, instanceOf(UnaryExpression.class));
        UnaryExpression unaryExpression = (UnaryExpression) expressionItem;
        assertThat(unaryExpression.getOperator(), is(OperatorKind.MAX));
        assertThat(unaryExpression.getOperand().toString(), is("/data/events/time"));
    }

    @Test
    public void can_parse_expression_with_not_operator_using_keyword() throws Exception {
        expressionItem = deserializer.parse("not($gt0016==5)");
        assertThat(expressionItem, instanceOf(UnaryExpression.class));
        UnaryExpression unaryExpression = (UnaryExpression) expressionItem;
        assertThat(unaryExpression.getOperator(), is(OperatorKind.NOT));
        assertThat(unaryExpression.getOperand().toString(), is("$gt0016==5"));
    }

    @Test
    public void can_parse_expression_with_not_operator_using_exclamation_mark() throws Exception {
        expressionItem = deserializer.parse("!($gt0016==5)");
        assertThat(expressionItem, instanceOf(UnaryExpression.class));
        UnaryExpression unaryExpression = (UnaryExpression) expressionItem;
        assertThat(unaryExpression.getOperator(), is(OperatorKind.NOT));
        assertThat(unaryExpression.getOperand().toString(), is("$gt0016==5"));
    }
}
