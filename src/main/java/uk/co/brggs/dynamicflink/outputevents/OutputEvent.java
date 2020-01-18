package uk.co.brggs.dynamicflink.outputevents;

import uk.co.brggs.dynamicflink.events.InputEvent;
import com.jsoniter.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class OutputEvent {
    /**
     * The time of the event which triggered the rule.
     */
    @Getter(onMethod_ = @JsonProperty(to = {InputEvent.EventTimeField}))
    private String eventTime;

    /**
     * The time at which the rule triggered.
     */
    private String matchedTime;

    /**
     * The source of this event.
     */
    private String source;

    /**
     * The unique ID of the rule.
     */
    private String matchedRuleId;

    /**
     * The version of the rule.
     */
    private int matchedRuleVersion;

    /**
     * The original event content.
     */
    private String eventContent;
}
