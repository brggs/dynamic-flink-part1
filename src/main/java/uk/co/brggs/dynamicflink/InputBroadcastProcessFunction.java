package uk.co.brggs.dynamicflink;

import uk.co.brggs.dynamicflink.control.*;
import uk.co.brggs.dynamicflink.outputevents.OutputEvent;
import uk.co.brggs.dynamicflink.events.DateFormatter;
import uk.co.brggs.dynamicflink.events.InputEvent;
import uk.co.brggs.dynamicflink.rules.Rule;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Date;

/**
 * Handles and responds to control events, and matches input events against rules.
 */
@Slf4j
public class InputBroadcastProcessFunction
        extends BroadcastProcessFunction<String, ControlInput, OutputEvent> {

    private final MapStateDescriptor<String, Rule> ruleStateDescriptor = new MapStateDescriptor<>(
            "RulesBroadcastState",
            Types.STRING,
            Types.POJO(Rule.class));

    /**
     * Processes control data
     *
     * @param controlInput Control instructions, used to add/remove rules.
     * @param ctx          Provides write access to the broadcast state.
     * @param collector    Not used. Acknowledgments are sent through side-output.
     */
    @Override
    public void processBroadcastElement(ControlInput controlInput, Context ctx, Collector<OutputEvent> collector) {
        val ruleId = controlInput.getRuleId();
        val ruleVersion = controlInput.getRuleVersion();
        val content = controlInput.getContent();
        val controlType = controlInput.getType();

        if (controlType == ControlInputType.ADD_RULE) {
            val rules = ctx.getBroadcastState(ruleStateDescriptor);
            try {
                if (rules.contains(ruleId) && rules.get(ruleId).getVersion() >= ruleVersion) {
                    // Do not apply a rule unless its version is higher than the current one
                    log.info("Rule {}, version {} already present. No action taken.", ruleId, rules.get(ruleId).getVersion());
                } else {
                    val newRule = new ObjectMapper().readValue(content, Rule.class);

                    // Store the ID and version number in the rule object
                    newRule.setId(ruleId);
                    newRule.setVersion(ruleVersion);
                    rules.put(ruleId, newRule);

                    log.info("Rule added, rule {}, version {}", ruleId, ruleVersion);
                }

                val output = new ControlOutput(controlInput, ControlOutputStatus.RULE_ACTIVE);
                ctx.output(ControlOutputTag.controlOutput, output);

            } catch (Exception ex) {
                log.error("Error processing rule {}, content {}, version {}", controlInput.getRuleId(), content, ruleVersion, ex);
            }
        } else {
            log.error("Unknown controlType " + controlType);
        }
    }

    /**
     * Processes events
     *
     * @param eventContent The event data to be processed.
     * @param ctx          Provides read only access to the broadcast state (rules).
     * @param collector    Used to output events matching rules.
     */
    @Override
    public void processElement(String eventContent, ReadOnlyContext ctx, Collector<OutputEvent> collector) {
        try {
            val inputEvent = new InputEvent(eventContent);

            ctx.getBroadcastState(ruleStateDescriptor).immutableEntries().forEach(ruleEntry -> {
                val rule = ruleEntry.getValue();

                if (rule.getCondition().checkMatch(inputEvent)) {
                    val outputEvent = OutputEvent.builder()
                            .matchedRuleId(rule.getId())
                            .matchedRuleVersion(rule.getVersion())
                            .matchedTime(DateFormatter.getInstance().format(new Date(System.currentTimeMillis())))
                            .eventTime(DateFormatter.getInstance().format(ctx.timestamp()))
                            .eventContent(inputEvent.getContent())
                            .build();

                    collector.collect(outputEvent);
                }
            });
        } catch (Exception ex) {
            log.error("Error processing event {}", eventContent, ex);
        }
    }
}
