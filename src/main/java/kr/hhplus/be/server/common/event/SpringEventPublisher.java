package kr.hhplus.be.server.common.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher{

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(String topic, Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}
