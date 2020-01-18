package uk.co.brggs.dynamicflink.blocks.conditions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.brggs.dynamicflink.events.InputEvent;

/**
 * Condition to compare the string value of a specified field for an exact value
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EqualCondition implements Condition {
    private String key;
    private String value;

    @Override
    public boolean checkMatch(InputEvent eventContent) {
        return value.equals(eventContent.getField(key));
    }
}
