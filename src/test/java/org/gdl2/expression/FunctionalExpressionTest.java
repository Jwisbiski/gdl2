package org.gdl2.expression;

import org.gdl2.deserializers.ExpressionItemDeserializer;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FunctionalExpressionTest {
    private ExpressionItemDeserializer deserializer = new ExpressionItemDeserializer();
    private FunctionalExpression functionalExpression;

    @Test
    public void testCreateSimpleFunctionalExpression() {
        FunctionalExpression fe = FunctionalExpression.create(new Function("log"));
        assertThat(fe.toString(), is("log()"));
    }

    @Test
    public void testCreateSimpleFunctionalExpressionWithSingleVariable() {
        FunctionalExpression fe = FunctionalExpression.create(new Function("log"),
                ConstantExpression.create("180"));
        assertThat(fe.toString(), is("log(180)"));
    }

    @Test
    public void testCreateFunctionalExpressionWithNestedVariables() {
        List<ExpressionItem> items = new ArrayList<>();
        items.add(ConstantExpression.create("180"));
        BinaryExpression be1 = BinaryExpression.create(
                Variable.createByCode("gt0001"),
                ConstantExpression.create("2"),
                OperatorKind.MULTIPLICATION);
        items.add(be1);
        FunctionalExpression fe = FunctionalExpression.create(new Function("max"),
                items);
        assertThat(fe.toString(), is("max(180,($gt0001*2))"));
    }

    @Test
    public void testCanParseTrigonometricFunctionSin() {
        functionalExpression = parse("sin(0.7853)");
        assertThat(functionalExpression.toString(), is("sin(0.7853)"));
    }

    @Test
    public void testCanParseTrigonometricFunctionCos() {
        functionalExpression = parse("cos(0.7853)");
        assertThat(functionalExpression.toString(), is("cos(0.7853)"));
    }

    @Test
    public void testCanParseTrigonometricFunctionTan() {
        functionalExpression = parse("tan(0.7853)");
        assertThat(functionalExpression.toString(), is("tan(0.7853)"));
    }

    private FunctionalExpression parse(String expression) {
        try {
            ExpressionItem expressionItem = deserializer.parse(expression);
            return (FunctionalExpression) expressionItem;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}