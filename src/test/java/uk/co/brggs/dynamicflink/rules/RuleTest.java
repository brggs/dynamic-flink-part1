package uk.co.brggs.dynamicflink.rules;

import uk.co.brggs.dynamicflink.blocks.conditions.EqualCondition;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class RuleTest {

    @Test
    void simpleRule_CanBeSerialisedAndDeserialised() throws IOException {
        val rule = Rule.builder()
                .condition(new EqualCondition("hostname", "importantLaptop"))
                .build();
        val ruleData = new ObjectMapper().writeValueAsString(rule);

        assertThat(ruleData.contains("importantLaptop")).isTrue();

        val outputRule = new ObjectMapper().readValue(ruleData, Rule.class);
        assertThat(outputRule).isNotNull();
    }
}
