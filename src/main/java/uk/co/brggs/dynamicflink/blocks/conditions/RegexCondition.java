package uk.co.brggs.dynamicflink.blocks.conditions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import uk.co.brggs.dynamicflink.events.InputEvent;

import java.util.regex.Pattern;

/**
 * Condition to compare the string value of a specified field for a match against a regex
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegexCondition implements Condition {
    private String key;
    private String regex;

    @Override
    public boolean checkMatch(InputEvent eventContent) {
        val value = eventContent.getField(key);

        if (value != null) {
            val p = Pattern.compile(regex);
            val m = p.matcher(value);

            return m.find();
        } else {
            return false;
        }
    }
}
