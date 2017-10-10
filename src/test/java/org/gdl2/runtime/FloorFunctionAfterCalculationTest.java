package org.gdl2.runtime;

import org.gdl2.datatypes.DvDateTime;
import org.gdl2.datatypes.DvQuantity;
import org.gdl2.model.Guideline;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FloorFunctionAfterCalculationTest extends TestCommon {

    @Test
    public void can_execute_floor_function() throws Exception {
        Guideline guideline = loadGuideline("floor_function_after_calculation.v1.0.0.gdl2");

        List<DataInstance> dataInstances = new ArrayList<>();
        dataInstances.add(new DataInstance.Builder()
                .modelId("openEHR-EHR-OBSERVATION.basic_demographic.v1")
                .addValue("/data[at0001]/events[at0002]/data[at0003]/items[at0008]", DvDateTime.valueOf("1953-12-05T00:00:00"))
                .build());

        Map<String, Object> result = new Interpreter(DvDateTime.valueOf("2017-10-04T11:38:00"))
                .execute(guideline, dataInstances);
        Object dataValue = result.get("gt0005");
        assertThat(dataValue, Matchers.instanceOf(DvQuantity.class));
        DvQuantity dvQuantity = (DvQuantity) dataValue;
        assertThat(dvQuantity.getMagnitude(), is(63.0));
        assertThat(dvQuantity.getPrecision(), is(0));
    }
}
