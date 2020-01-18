package uk.co.brggs.dynamicflink.rules;

import uk.co.brggs.dynamicflink.blocks.conditions.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a rule containing a condition, which defines which events will be matched.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {

    /**
     * The unique ID of the rule.
     */
    private String id;

    /**
     * The version number of the rule.
     */
    private int version;

    /**
     * The conditions under which an event will match the rule.
     */
    private Condition condition;
}
