package org.gdl2.datatypes;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DvQuantityTest {

    @Test
    public void can_parse_dv_quantity() {
        DvQuantity dvQuantity = DvQuantity.valueOf("100.8,kg");
        assertThat(dvQuantity.getMagnitude(), is(100.8));
        assertThat(dvQuantity.getUnit(), is("kg"));
    }

    @Test
    public void can_round_double_up_with_precision() {
        DvQuantity dvQuantity = DvQuantity.builder().unit("%").precision(1).magnitude(9.7502).build();
        assertThat(dvQuantity.toString(), is("9.8,%"));
    }

    @Test
    public void can_round_double_down_with_precision() {
        DvQuantity dvQuantity = DvQuantity.builder().unit("%").precision(1).magnitude(9.7402).build();
        assertThat(dvQuantity.toString(), is("9.7,%"));
    }

    @Test
    public void can_perform_equality_check() {
        DvQuantity dvQuantity = DvQuantity.valueOf("100.8,kg");
        DvQuantity dvQuantity2 = DvQuantity.valueOf("100.8,mg");
        assertThat(dvQuantity.equals(dvQuantity2), is(false));
        assertThat(dvQuantity2.equals(dvQuantity), is(false));
    }
}
