package uk.co.brggs.dynamicflink.control;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Holds data relating to command/control events for the system
 */
@Data
@NoArgsConstructor
public class ControlInput implements Serializable {
    private String timestamp;
    private ControlInputType type;
    private String ruleId;
    private int ruleVersion;
    private String content;

    public ControlInput(ControlInputType type, String ruleId, int ruleVersion, String content) {
        this.timestamp = Instant.now().toString();
        this.type = type;
        this.ruleId = ruleId;
        this.ruleVersion = ruleVersion;
        this.content = content;
    }
}
