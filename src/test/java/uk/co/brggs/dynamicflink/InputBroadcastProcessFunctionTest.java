package uk.co.brggs.dynamicflink;

import uk.co.brggs.dynamicflink.blocks.conditions.EqualCondition;
import uk.co.brggs.dynamicflink.control.ControlInput;
import uk.co.brggs.dynamicflink.control.ControlInputType;
import uk.co.brggs.dynamicflink.rules.Rule;
import lombok.val;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// Suppress warnings caused by mocking generics with Mockito.
@SuppressWarnings({"unchecked"})
class InputBroadcastProcessFunctionTest {

    @Test
    void newRule_shouldAddRule() throws Exception {
        val ruleId = "ruleId";

        val rule = Rule.builder()
                .condition(new EqualCondition("processName", "process.exe"))
                .build();
        val ruleData = new ObjectMapper().writeValueAsString(rule);

        BroadcastState<String, Rule> ruleState = Mockito.mock(BroadcastState.class);
        val contextMock = Mockito.mock(InputBroadcastProcessFunction.Context.class);
        Mockito.<BroadcastState<String, Rule>>when(contextMock.getBroadcastState(any())).thenReturn(ruleState);

        val testFunction = new InputBroadcastProcessFunction();
        testFunction.processBroadcastElement(new ControlInput(ControlInputType.ADD_RULE, ruleId, 1, ruleData), contextMock, null);

        val ruleCaptor = ArgumentCaptor.forClass(Rule.class);
        verify(ruleState, times(1)).put(any(String.class), ruleCaptor.capture());
        val capturedRules = ruleCaptor.getAllValues();
        assertEquals(ruleId, capturedRules.get(0).getId());
    }

    @Test
    void newVersionOfRule_shouldUpdateRule() throws Exception {
        val ruleId = "ruleId";

        val rule = Rule.builder()
                .condition(new EqualCondition("processName", "process.exe"))
                .build();
        val ruleData = new ObjectMapper().writeValueAsString(rule);

        BroadcastState<String, Rule> ruleState = Mockito.mock(BroadcastState.class);
        when(ruleState.contains(ruleId)).thenReturn(true);
        when(ruleState.get(ruleId)).thenReturn(rule);

        val contextMock = Mockito.mock(InputBroadcastProcessFunction.Context.class);
        Mockito.<BroadcastState<String, Rule>>when(contextMock.getBroadcastState(any())).thenReturn(ruleState);

        val testFunction = new InputBroadcastProcessFunction();
        testFunction.processBroadcastElement(new ControlInput(ControlInputType.ADD_RULE, ruleId, 2, ruleData), contextMock, null);

        val ruleCaptor = ArgumentCaptor.forClass(Rule.class);
        verify(ruleState, times(1)).put(any(String.class), ruleCaptor.capture());
        val capturedRules = ruleCaptor.getAllValues();
        assertEquals(ruleId, capturedRules.get(0).getId());
    }

    @Test
    void oldVersionOfRule_shouldNotUpdateRule() throws Exception {
        val ruleId = "ruleId";
        val rule = Rule.builder()
                .version(1)
                .condition(new EqualCondition("processName", "process.exe"))
                .build();

        val oldRuleData = new ObjectMapper().writeValueAsString(rule);

        rule.setVersion(2);

        BroadcastState<String, Rule> ruleState = Mockito.mock(BroadcastState.class);
        when(ruleState.contains(ruleId)).thenReturn(true);
        when(ruleState.get(ruleId)).thenReturn(rule);

        val contextMock = Mockito.mock(InputBroadcastProcessFunction.Context.class);
        Mockito.<BroadcastState<String, Rule>>when(contextMock.getBroadcastState(any())).thenReturn(ruleState);

        val testFunction = new InputBroadcastProcessFunction();

        testFunction.processBroadcastElement(new ControlInput(ControlInputType.ADD_RULE, ruleId, 1, oldRuleData), contextMock, null);

        verify(ruleState, never()).put(any(String.class), any(Rule.class));
    }
}
