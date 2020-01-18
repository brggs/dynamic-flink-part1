package uk.co.brggs.dynamicflink.blocks.conditions;

import uk.co.brggs.dynamicflink.events.InputEvent;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface for classes which determine whether an event will match against a rule
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface Condition {
    boolean checkMatch(InputEvent eventContent);
}
