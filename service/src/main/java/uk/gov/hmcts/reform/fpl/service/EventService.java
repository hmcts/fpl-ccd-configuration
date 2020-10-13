package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}
