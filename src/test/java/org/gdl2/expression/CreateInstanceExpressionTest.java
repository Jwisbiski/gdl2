package org.gdl2.expression;

import org.gdl2.expression.parser.ExpressionParser;
import org.testng.annotations.Test;

import java.io.StringReader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CreateInstanceExpressionTest {

    @Test
    public void testToString() {
        String expression = "$gt0005.create($gt0036=local::at0006|Heart rate > 90 /min|;$gt0037=$gt0012;" +
                "$gt0038=$gt0013;$gt0039=local::at0015|Absent|)";
        AssignmentExpression ae = parseExpression(expression);
        assertThat(ae.toString(), is(expression));
    }

    private AssignmentExpression parseExpression(String expression) {
        try {
            return (AssignmentExpression) new ExpressionParser(new StringReader(expression)).parse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
