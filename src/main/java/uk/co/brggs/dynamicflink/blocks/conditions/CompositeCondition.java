package uk.co.brggs.dynamicflink.blocks.conditions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.brggs.dynamicflink.events.InputEvent;

import java.util.List;

/**
 * Condition allowing other conditions to be combined
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompositeCondition implements Condition {
    private CompositeType type;
    private List<Condition> conditions;

    @Override
    public boolean checkMatch(InputEvent eventContent) {
        if (type == CompositeType.OR) {
            return conditions.stream().anyMatch(c -> c.checkMatch(eventContent));
        } else {
            return conditions.stream().allMatch(c -> c.checkMatch(eventContent));
        }
    }
}

