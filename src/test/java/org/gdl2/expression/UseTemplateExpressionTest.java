package org.gdl2.expression;

import org.gdl2.deserializers.ExpressionItemDeserializer;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class UseTemplateExpressionTest {
    private ExpressionItemDeserializer deserializer = new ExpressionItemDeserializer();

    @Test
    public void should_produce_correct_string_for_an_expression_without_assignments() {
        String expectedString = "use_template($gt2022)";
        UseTemplateExpression useTemplateExpression = parse(expectedString);
        String actualString = useTemplateExpression.toString();

        assertEquals(actualString, expectedString);
    }

    @Test
    public void should_produce_correct_string_for_an_expression_with_one_assignment() {
        String expectedString = "use_template($gt2022($gt0003='Hello World'))";
        UseTemplateExpression useTemplateExpression = parse(expectedString);
        String actualString = useTemplateExpression.toString();

        assertEquals(actualString, expectedString);
    }

    @Test
    public void should_produce_correct_string_for_an_expression_with_conditional_assignments() {
        String expectedString = "use_template($gt2020|goal|[if: $gt0102,$gt0105; $gt3020|category|: $gt0101,$gt0104; $gt3030|text|: $gt0103,$gt0106])";
        UseTemplateExpression useTemplateExpression = parse(expectedString);
        String actualString = useTemplateExpression.toString();

        assertEquals(actualString, expectedString);
    }


    @Test
    public void should_produce_correct_string_for_an_expression_with_single_assignment_with_expression() {
        String expectedString = "use_template($gt1002($gt2001=$currentDateTime+3,mo))";
        UseTemplateExpression useTemplateExpression = parse(expectedString);
        String actualString = useTemplateExpression.toString();

        assertEquals(actualString, expectedString);
    }

    @Test
    public void should_produce_correct_string_for_an_expression_with_multiple_assignments() {
        String expectedString = "use_template($gt1002($gt2000=$currentDateTime;$gt2001=$currentDateTime+3,mo))";
        UseTemplateExpression useTemplateExpression = parse(expectedString);
        String actualString = useTemplateExpression.toString();
        assertEquals(actualString, expectedString);
    }

    private UseTemplateExpression parse(String expression) {
        try {
            ExpressionItem expressionItem = deserializer.parse(expression);
            return (UseTemplateExpression) expressionItem;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}