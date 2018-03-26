package org.gdl2;

import org.apache.commons.io.IOUtils;
import org.gdl2.model.Guideline;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class Gdl2RoundTripTest {

    @Test
    public void can_perform_round_trip() throws Exception {
        Guideline guideline = Gdl2.fromGdl2(
                IOUtils.toString(this.getClass().getClassLoader()
                        .getResourceAsStream(("BSA_Calculation.v1.gdl2.json")), "UTF-8"));
        String serialized = Gdl2.toGdl2(guideline);
        Guideline guidelineAfterRoundTrip = Gdl2.fromGdl2(serialized);
        assertThat(guideline.equals(guidelineAfterRoundTrip), is(true));
    }

    @Test
    public void can_perform_round_trip_with_cds_hooks_cards() throws Exception {
        Guideline guideline = Gdl2.fromGdl2(
                IOUtils.toString(this.getClass().getClassLoader()
                        .getResourceAsStream(("BSA_Calculation_cdshooks_test.v1.gdl2.json")), "UTF-8"));
        String serialized = Gdl2.toGdl2(guideline);
        Guideline guidelineAfterRoundTrip = Gdl2.fromGdl2(serialized);
        assertThat(guideline.equals(guidelineAfterRoundTrip), is(true));
    }

    @Test
    public void can_perform_round_trip_with_cds_hooks_cards_output_templates() throws Exception {
        Guideline guideline = Gdl2.fromGdl2(
                IOUtils.toString(this.getClass().getClassLoader()
                        .getResourceAsStream(("BSA_Calculation_cdshooks_test2.v1.gdl2.json")), "UTF-8"));
        String serialized = Gdl2.toGdl2(guideline);
        Guideline guidelineAfterRoundTrip = Gdl2.fromGdl2(serialized);
        assertThat(guideline.equals(guidelineAfterRoundTrip), is(true));
    }
}