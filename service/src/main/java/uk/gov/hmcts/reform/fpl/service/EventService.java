package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(Object event) {
        log.info("Publishing event {}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
}
