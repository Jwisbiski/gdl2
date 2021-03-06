package org.gdl2.runtime;

import org.gdl2.datatypes.DvQuantity;
import org.gdl2.model.Guideline;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class MathFunctionsTest extends TestCommon {
    private final static String MATH_FUNCTIONS_TEST_GUIDE = "math_functions_test.gdl2";
    private Map<String, List<Object>> result;
    private List<Object> list;
    private DvQuantity dvQuantity;

    @BeforeMethod
    public void setUp() throws Exception {
        Interpreter interpreter = new Interpreter();
        Guideline guideline = loadGuideline(MATH_FUNCTIONS_TEST_GUIDE);
        ArrayList<DataInstance> dataInstances = new ArrayList<>();
        dataInstances.add(new DataInstance.Builder().modelId("openEHR-EHR-EVALUATION.test_input.v1")
                .addValue("/path", DvQuantity.valueOf("0.5,1"))
                .build());
        result = interpreter.execute(guideline, dataInstances).getResult();
    }

    private DvQuantity getDvQuantity(String code) {
        list = result.get(code);
        Object dataValue = list.get(list.size() - 1);
        return (DvQuantity) dataValue;
    }

    @Test
    public void should_return_correct_abs_function_result() {
        list = result.get("gt0003");
        Object dataValue = list.get(list.size() - 1);
        DvQuantity dvQuantity = (DvQuantity) dataValue;
        assertThat(dvQuantity.getMagnitude(), closeTo(5.3, 0));
    }

    @Test
    public void should_return_correct_ceil_function_result() {
        list = result.get("gt0004");
        Object dataValue = list.get(list.size() - 1);
        DvQuantity dvQuantity = (DvQuantity) dataValue;
        assertThat(dvQuantity.getMagnitude(), closeTo(2.0, 0));
    }

    @Test
    public void should_return_correct_exp_function_result() {
        list = result.get("gt0005");
        Object dataValue = list.get(list.size() - 1);
        DvQuantity dvQuantity = (DvQuantity) dataValue;
        assertThat(dvQuantity.getMagnitude(), closeTo(20.08, 0.1));
    }

    @Test
    public void should_return_correct_floor_function_result() {
        list = result.get("gt0006");
        Object dataValue = list.get(list.size() - 1);
        DvQuantity dvQuantity = (DvQuantity) dataValue;
        assertThat(dvQuantity.getMagnitude(), closeTo(1.0, 0));
    }

    @Test
    public void should_return_correct_log_function_result() {
        list = result.get("gt0007");
        Object dataValue = list.get(list.size() - 1);
        DvQuantity dvQuantity = (DvQuantity) dataValue;
        assertThat(dvQuantity.getMagnitude(), closeTo(4.60, 0.01));
    }

    @Test
    public void should_return_correct_log10_function_result() {
        list = result.get("gt0008");
        Object dataValue = list.get(list.size() - 1);
        DvQuantity dvQuantity = (DvQuantity) dataValue;
        assertThat(dvQuantity.getMagnitude(), closeTo(3.0, 0));
    }

    @Test
    public void should_return_correct_log1p_function_result() {
        list = result.get("gt0009");
        Object dataValue = list.get(list.size() - 1);
        DvQuantity dvQuantity = (DvQuantity) dataValue;
        assertThat(dvQuantity.getMagnitude(), closeTo(2.39, 0.01));
    }

    @Test
    public void should_return_correct_round_function_result() {
        list = result.get("gt0010");
        Object dataValue = list.get(list.size() - 1);
        DvQuantity dvQuantity = (DvQuantity) dataValue;
        assertThat(dvQuantity.getMagnitude(), closeTo(8.0, 0));
    }

    @Test
    public void should_return_correct_sqrt_function_result() {
        list = result.get("gt0011");
        Object dataValue = list.get(list.size() - 1);
        DvQuantity dvQuantity = (DvQuantity) dataValue;
        assertThat(dvQuantity.getMagnitude(), closeTo(4.0, 0));
    }

    @Test
    public void can_perform_trigonometric_function_sin() {
        dvQuantity = getDvQuantity("gt0100");
        assertThat(dvQuantity.toString(), is("0.4794,1"));
    }

    @Test
    public void can_perform_trigonometric_function_cos() {
        dvQuantity = getDvQuantity("gt0101");
        assertThat(dvQuantity.toString(), is("0.8776,1"));
    }

    @Test
    public void can_perform_trigonometric_function_tan() {
        dvQuantity = getDvQuantity("gt0102");
        assertThat(dvQuantity.toString(), is("0.5463,1"));
    }
}
