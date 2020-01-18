package uk.co.brggs.dynamicflink.outputevents;

import uk.co.brggs.dynamicflink.events.DateFormatter;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class OutputEventTest {
    @Test
    void outputEvent_CanBeSerialised() {
        val outputEvent = OutputEvent.builder()
                .source("DynamicFlinkJob")
                .matchedRuleId("ruleId")
                .matchedRuleVersion(1)
                .eventTime(DateFormatter.getInstance().format(new Date(System.currentTimeMillis())))
                .matchedTime(DateFormatter.getInstance().format(new Date(System.currentTimeMillis())))
                .build();

        val serialiser = new OutputEventSerializationSchema();
        val output = serialiser.serialize(outputEvent);
        assertThat(output).isNotNull();

        val outputString = new String(output);
        assertThat(outputString).isNotEmpty();
        assertThat(outputString).contains("\"@timestamp\":");
    }
}
