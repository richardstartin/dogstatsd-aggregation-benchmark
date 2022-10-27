package com.timgroup.statsd;

public final class CounterMessage extends NumericMessage<Long> {


    public CounterMessage(String aspect, Long value, String[] tags) {
        super(aspect, Type.COUNT, value, tags);
    }

    @Override
    void writeTo(StringBuilder builder, String containerId) {
    }
}
