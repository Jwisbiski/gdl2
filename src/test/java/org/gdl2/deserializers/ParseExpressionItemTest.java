package org.gdl2.deserializers;


import org.gdl2.expression.*;
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

    @Test
    public void can_parse_dv_ordinal_with_negative_value() throws Exception {
        expressionItem = deserializer.parse("$gt0013=-1|local::at0018|45+ years|");
        assertThat(expressionItem, instanceOf(AssignmentExpression.class));
        AssignmentExpression assignmentExpression = (AssignmentExpression) expressionItem;
        assertThat(assignmentExpression.getVariable().getCode(), is("gt0013"));
        assertThat(assignmentExpression.getAssignment(), instanceOf(OrdinalConstant.class));
        OrdinalConstant ordinalConstant = (OrdinalConstant) assignmentExpression.getAssignment();
        assertThat(ordinalConstant.getOrdinal().getValue(), is(-1));
    }

    @Test
    public void can_parse_dv_quantity_with_egfr_unit() throws Exception {
        expressionItem = deserializer.parse("$gt0013=60,mL/min/{1.73_m2}");
        assertThat(expressionItem, instanceOf(AssignmentExpression.class));
        AssignmentExpression assignmentExpression = (AssignmentExpression) expressionItem;
        assertThat(assignmentExpression.getVariable().getCode(), is("gt0013"));
        assertThat(assignmentExpression.getAssignment(), instanceOf(QuantityConstant.class));
        QuantityConstant quantityConstant = (QuantityConstant) assignmentExpression.getAssignment();
        assertThat(quantityConstant.getQuantity().getUnit(), is("mL/min/{1.73_m2}"));
    }

    @Test
    public void can_parse_dv_quantity_with_egfr_unit2() throws Exception {
        expressionItem = deserializer.parse("$gt0013=60,ml/min/1.73m2");
        assertThat(expressionItem, instanceOf(AssignmentExpression.class));
        AssignmentExpression assignmentExpression = (AssignmentExpression) expressionItem;
        assertThat(assignmentExpression.getVariable().getCode(), is("gt0013"));
        assertThat(assignmentExpression.getAssignment(), instanceOf(QuantityConstant.class));
        QuantityConstant quantityConstant = (QuantityConstant) assignmentExpression.getAssignment();
        assertThat(quantityConstant.getQuantity().getUnit(), is("ml/min/1.73m2"));
    }

    @Test
    public void can_parse_variable_current_datetime_year_attribute() throws Exception {
        expressionItem = deserializer.parse("$currentDateTime.year");
        assertThat(expressionItem, instanceOf(Variable.class));
        Variable variable = (Variable) expressionItem;
        assertThat(variable.getCode(), is("currentDateTime"));
        assertThat(variable.getAttribute(), is("year"));
    }

    @Test
    public void can_parse_assignment_with_single_variable() throws Exception {
        expressionItem = deserializer.parse("$gt0004='{$gt0003.text}'");
        assertThat(expressionItem, instanceOf(AssignmentExpression.class));
        AssignmentExpression assignmentExpression = (AssignmentExpression) expressionItem;
        assertThat(assignmentExpression.getVariable().getCode(), is("gt0004"));
        assertThat(assignmentExpression.getAssignment(), instanceOf(StringConstant.class));
    }
}
