package uk.co.brggs.dynamicflink;

import uk.co.brggs.dynamicflink.control.ControlOutput;
import uk.co.brggs.dynamicflink.outputevents.OutputEvent;
import uk.co.brggs.dynamicflink.control.ControlInput;
import uk.co.brggs.dynamicflink.control.ControlInputWatermarkAssigner;
import uk.co.brggs.dynamicflink.control.ControlOutputTag;
import uk.co.brggs.dynamicflink.events.EventTimestampExtractor;
import uk.co.brggs.dynamicflink.rules.Rule;
import lombok.val;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * Class containing a single method which connects the separate parts of the Dynamic Flink job.
 */
public class DynamicFlink {

    /**
     * Builds the Dynamic Flink job graph.
     *
     * @param eventStream Provides events/messages to process
     * @param controlStream Provides rules
     * @param outputEventSink Collects results of the job
     * @param controlOutput Collects replies to control events
     */
    public static void build(
            DataStream<String> eventStream,
            DataStream<ControlInput> controlStream,
            SinkFunction<OutputEvent> outputEventSink,
            SinkFunction<ControlOutput> controlOutput
    ) {
        // Assign timestamps based on the event time specified in the event
        val timestampedEventStream = eventStream.assignTimestampsAndWatermarks(
                new EventTimestampExtractor(Time.seconds(10)));

        // Set up the descriptor for storing the rules in managed state
        val controlInputStateDescriptor = new MapStateDescriptor<>(
                "RulesBroadcastState",
                Types.STRING,
                Types.POJO(Rule.class));

        // Broadcast the control stream, so that rules are available at every node
        val controlBroadcastStream = controlStream
                .assignTimestampsAndWatermarks(new ControlInputWatermarkAssigner())
                .broadcast(controlInputStateDescriptor);

        // Send the events into the input function.  The matching event stream is then split based on the type of the
        // block which matched the event.
        val processFunctionOutput = timestampedEventStream
                .connect(controlBroadcastStream)
                .process(new InputBroadcastProcessFunction());

        // Send control output to the appropriate stream
        processFunctionOutput
                .getSideOutput(ControlOutputTag.controlOutput)
                .addSink(controlOutput)
                .uid("control-sideoutput-sink")
                .name("control-sideoutput-sink");

        // Send the output
        processFunctionOutput.addSink(outputEventSink);
    }
}

