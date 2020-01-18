package uk.co.brggs.dynamicflink.control;

import uk.co.brggs.dynamicflink.blocks.conditions.EqualCondition;
import uk.co.brggs.dynamicflink.rules.Rule;
import lombok.val;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ControlInputTest {

    private String validRuleData;

    ControlInputTest() throws JsonProcessingException {
        val rule = Rule.builder()
                .condition(
                        new EqualCondition("hostname", "importantLaptop"))
                .build();
        validRuleData = new ObjectMapper().writeValueAsString(rule);
    }

    @Test
    void controlInput_CanBeSerialisedAndDeserialised() throws IOException {
        val ce = new ControlInput(ControlInputType.ADD_RULE, "1", 1, validRuleData);
        val ceData = new ObjectMapper().writeValueAsString(ce);
        val outputControlEvent = new ObjectMapper().readValue(ceData, ControlInput.class);

        assertNotNull(outputControlEvent);
    }
}
