package org.gdl2.runtime;

import org.gdl2.datatypes.DvCodedText;
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

public class AssignCodedTextByCodedTextConstantTest extends TestCommon {
    private Interpreter interpreter;

    @BeforeMethod
    public void setUp() throws Exception {
        interpreter = new Interpreter();
    }

    @Test
    public void can_assign_coded_text_value() throws Exception {
        Guideline guideline = loadGuideline("assign_coded_text_with_attributes");
        ArrayList<DataInstance> dataInstances = new ArrayList<>();

        List<DataInstance> result = interpreter.executeSingleGuideline(guideline, dataInstances);
        DataInstance dataInstance = result.get(0);
        DvCodedText dvCodedText = dataInstance.getDvCodedText("/data[at0001]");
        assertThat(dvCodedText.getDefiningCode().getTerminology(), is("ATC"));
        assertThat(dvCodedText.getDefiningCode().getCode(), is("C09AA"));
        assertThat(dvCodedText.getValue(), is("ACEI"));
    }
}
