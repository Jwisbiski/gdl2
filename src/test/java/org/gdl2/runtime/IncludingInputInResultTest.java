package org.gdl2.runtime;

import org.gdl2.datatypes.DvQuantity;
import org.gdl2.model.Guideline;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

public class IncludingInputInResultTest extends TestCommon {
    private static final String GUIDELINE = "BSA_Calculation_include_input_test.v1.gdl2";
    private Interpreter interpreter;

    @BeforeMethod
    public void setUp() throws Exception {
        interpreter = new Interpreter();
    }

    @Test
    public void can_include_single_input() throws Exception {
        Guideline guideline = loadGuideline(GUIDELINE);
        ArrayList<DataInstance> dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("72.0,kg"));
        dataInstances.add(toHeight("180.0,cm"));

        interpreter = new Interpreter(RuntimeConfiguration.builder()
                .language("en")
                .includingInputWithPredicate(true)
                .objectCreatorPlugin(new DefaultObjectCreator())
                .terminologySubsumptionEvaluators(Collections.emptyMap())
                .build());
        List<DataInstance> result = interpreter.executeSingleGuideline(guideline, dataInstances);
        assertThat(result.size(), is(3));
        DataInstance dataInstance = result.get(0);
        assertThat(dataInstance.modelId(), is("openEHR-EHR-OBSERVATION.body_surface_area.v1"));
        DvQuantity dvQuantity = dataInstance.getDvQuantity("/data[at0001]/events[at0002]/data[at0003]/items[at0004]");
        assertThat(dvQuantity.getMagnitude(), closeTo(1.90, 0.1));
        assertThat(dvQuantity.getPrecision(), is(2));
        assertThat(dvQuantity.getUnit(), is("m2"));

        dataInstance = result.get(1);
        assertThat(dataInstance.modelId(), is("openEHR-EHR-OBSERVATION.body_weight.v1"));
        dvQuantity = dataInstance.getDvQuantity("/data[at0002]/events[at0003]/data[at0001]/items[at0004]");
        assertThat(dvQuantity.getMagnitude(), closeTo(72, 0.1));
        assertThat(dvQuantity.getUnit(), is("kg"));

        dataInstance = result.get(2);
        assertThat(dataInstance.modelId(), is("openEHR-EHR-OBSERVATION.height.v1"));
        dvQuantity = dataInstance.getDvQuantity("/data[at0001]/events[at0002]/data[at0003]/items[at0004]");
        assertThat(dvQuantity.getMagnitude(), closeTo(180, 0.1));
        assertThat(dvQuantity.getUnit(), is("cm"));
    }

    @Test
    public void can_include_multiple_input() throws Exception {
        Guideline guideline = loadGuideline(GUIDELINE);
        ArrayList<DataInstance> dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("72.0,kg"));
        dataInstances.add(toWeight("78.0,kg"));
        dataInstances.add(toHeight("180.0,cm"));

        interpreter = new Interpreter(RuntimeConfiguration.builder()
                .language("en")
                .includingInputWithPredicate(true)
                .objectCreatorPlugin(new DefaultObjectCreator())
                .terminologySubsumptionEvaluators(Collections.emptyMap())
                .build());
        List<DataInstance> result = interpreter.executeGuidelines(Collections.singletonList(guideline), dataInstances);
        assertThat(result.size(), is(4));
        DataInstance dataInstance = result.get(0);
        assertThat(dataInstance.modelId(), is("openEHR-EHR-OBSERVATION.body_surface_area.v1"));
        DvQuantity dvQuantity = dataInstance.getDvQuantity("/data[at0001]/events[at0002]/data[at0003]/items[at0004]");
        assertThat(dvQuantity.getMagnitude(), closeTo(1.90, 0.1));
        assertThat(dvQuantity.getPrecision(), is(2));
        assertThat(dvQuantity.getUnit(), is("m2"));

        dataInstance = result.get(1);
        assertThat(dataInstance.modelId(), is("openEHR-EHR-OBSERVATION.body_weight.v1"));
        dvQuantity = dataInstance.getDvQuantity("/data[at0002]/events[at0003]/data[at0001]/items[at0004]");
        assertThat(dvQuantity.getMagnitude(), closeTo(72, 0.1));
        assertThat(dvQuantity.getUnit(), is("kg"));

        dataInstance = result.get(2);
        assertThat(dataInstance.modelId(), is("openEHR-EHR-OBSERVATION.body_weight.v1"));
        dvQuantity = dataInstance.getDvQuantity("/data[at0002]/events[at0003]/data[at0001]/items[at0004]");
        assertThat(dvQuantity.getMagnitude(), closeTo(78, 0.1));
        assertThat(dvQuantity.getUnit(), is("kg"));

        dataInstance = result.get(3);
        assertThat(dataInstance.modelId(), is("openEHR-EHR-OBSERVATION.height.v1"));
        dvQuantity = dataInstance.getDvQuantity("/data[at0001]/events[at0002]/data[at0003]/items[at0004]");
        assertThat(dvQuantity.getMagnitude(), closeTo(180, 0.1));
        assertThat(dvQuantity.getUnit(), is("cm"));
    }
}
