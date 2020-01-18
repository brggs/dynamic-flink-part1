package uk.co.brggs.dynamicflink.integration;

import uk.co.brggs.dynamicflink.blocks.conditions.CompositeCondition;
import uk.co.brggs.dynamicflink.blocks.conditions.CompositeType;
import uk.co.brggs.dynamicflink.TestEventGenerator;
import uk.co.brggs.dynamicflink.blocks.conditions.EqualCondition;
import uk.co.brggs.dynamicflink.control.ControlInput;
import uk.co.brggs.dynamicflink.control.ControlInputType;
import uk.co.brggs.dynamicflink.integration.shared.IntegrationTestBase;
import uk.co.brggs.dynamicflink.integration.shared.IntegrationTestCluster;
import uk.co.brggs.dynamicflink.rules.Rule;
import lombok.val;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleEventIntegrationTest extends IntegrationTestBase {

    @Test
    void simpleMatchingCondition_shouldProduceAlert() throws Exception {
        val rule = Rule.builder()
                .condition(
                        new CompositeCondition(
                                CompositeType.AND,
                                Arrays.asList(
                                        new EqualCondition("hostname", "importantLaptop"),
                                        new EqualCondition("destinationIp", "12.23.45.67"))))
                .build();
        val ruleData = new ObjectMapper().writeValueAsString(rule);
        val controlInput = Collections.singletonList(new ControlInput(ControlInputType.ADD_RULE, "matchingRule", 1, ruleData));

        val teg = TestEventGenerator.builder()
                .startTime(Instant.parse("2019-05-21T12:00:00.000Z"))
                .build();

        val matchingEventContent = teg.generate("destinationIp", "12.23.45.67", "hostname", "importantLaptop");

        val events = Arrays.asList(
                matchingEventContent,
                teg.generate("destinationIp", "12.23.45.67", "hostname", "anotherLaptop"));

        testCluster.run(controlInput, events);

        assertEquals(1, IntegrationTestCluster.EventSink.values.size());

        val matchedEvent = IntegrationTestCluster.EventSink.values.get(0);
        assertEquals("matchingRule", matchedEvent.getMatchedRuleId());
        assertEquals(1, matchedEvent.getMatchedRuleVersion());
        assertEquals("2019-05-21T12:00:00.000Z", matchedEvent.getEventTime());
        assertTrue(matchedEvent.getEventContent().contains("importantLaptop"));
    }

    @Test
    void simpleNonMatchingCondition_shouldNotProduceAlert() throws Exception {
        val rule = Rule.builder()
                .condition(new EqualCondition("hostname", "importantLaptop"))
                .build();
        val ruleData = new ObjectMapper().writeValueAsString(rule);
        val controlInput = Collections.singletonList(new ControlInput(ControlInputType.ADD_RULE, "matchingRule", 1, ruleData));

        val teg = TestEventGenerator.builder().build();
        val events = Collections.singletonList(
                teg.generate("destinationIp", "12.23.45.67", "hostname", "anotherLaptop"));

        testCluster.run(controlInput, events);

        assertEquals(0, IntegrationTestCluster.EventSink.values.size());
    }
}