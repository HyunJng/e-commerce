package kr.hhplus.be.server.common.event;

public interface EventPublisher {
    void publish(String topic, Object event);
}
