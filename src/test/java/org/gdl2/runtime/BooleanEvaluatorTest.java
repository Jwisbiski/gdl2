package org.gdl2.runtime;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class BooleanEvaluatorTest {
    private BooleanEvaluator booleanEvaluator;

    @BeforeMethod
    public void setUp() {
        booleanEvaluator = BooleanEvaluator.getInstance();
    }

    @Test
    public void can_false_and_false() {
        assertThat(booleanEvaluator.logicAnd(false, false), is(false));
    }

    @Test
    public void can_false_and_unknown() {
        assertThat(booleanEvaluator.logicAnd(false, null), is(false));
    }

    @Test
    public void can_false_and_true() {
        assertThat(booleanEvaluator.logicAnd(false, true), is(false));
    }

    @Test
    public void can_unknown_and_false() {
        assertThat(booleanEvaluator.logicAnd(null, false), is(false));
    }

    @Test
    public void can_unknown_and_unknown() {
        assertThat(booleanEvaluator.logicAnd(null, null), is(nullValue()));
    }

    @Test
    public void can_unknown_and_true() {
        assertThat(booleanEvaluator.logicAnd(null, true), is(nullValue()));
    }

    @Test
    public void can_true_and_false() {
        assertThat(booleanEvaluator.logicAnd(true, false), is(false));
    }

    @Test
    public void can_true_and_unknown() {
        assertThat(booleanEvaluator.logicAnd(true, null), is(nullValue()));
    }

    @Test
    public void can_true_and_true() {
        assertThat(booleanEvaluator.logicAnd(true, true), is(true));
    }

    @Test
    public void can_false_or_false() {
        assertThat(booleanEvaluator.logicOr(false, false), is(false));
    }

    @Test
    public void can_false_or_unknown() {
        assertThat(booleanEvaluator.logicOr(false, null), is(nullValue()));
    }

    @Test
    public void can_false_or_true() {
        assertThat(booleanEvaluator.logicOr(false, true), is(true));
    }

    @Test
    public void can_unknown_or_false() {
        assertThat(booleanEvaluator.logicOr(null, false), is(nullValue()));
    }

    @Test
    public void can_unknown_or_unknown() {
        assertThat(booleanEvaluator.logicOr(null, null), is(nullValue()));
    }

    @Test
    public void can_unknown_or_true() {
        assertThat(booleanEvaluator.logicOr(null, true), is(true));
    }

    @Test
    public void can_true_or_false() {
        assertThat(booleanEvaluator.logicOr(true, false), is(true));
    }

    @Test
    public void can_true_or_unknown() {
        assertThat(booleanEvaluator.logicOr(true, null), is(true));
    }

    @Test
    public void can_true_or_true() {
        assertThat(booleanEvaluator.logicOr(true, true), is(true));
    }
}
