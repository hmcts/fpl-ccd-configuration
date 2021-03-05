package uk.gov.hmcts.reform.fpl.utils.extension;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class TestLogger extends ListAppender<ILoggingEvent> implements AutoCloseable {

    private final Logger logger;

    public TestLogger(Class loggedClass) {
        this.logger = (Logger) LoggerFactory.getLogger(loggedClass);
        this.start();
        this.logger.addAppender(this);
    }

    public List<String> getWarns() {
        return get(Level.WARN);
    }

    public List<String> getInfos() {
        return get(Level.INFO);
    }

    public List<String> getErrors() {
        return get(Level.ERROR);
    }

    public List<String> get(Level level) {
        return this.list.stream()
            .filter(event -> level.equals(event.getLevel()))
            .map(ILoggingEvent::getFormattedMessage)
            .collect(Collectors.toList());
    }

    public List<String> get() {
        return this.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .collect(Collectors.toList());
    }

    public void reset() {
        this.list.clear();
    }

    @Override
    public void close() {
        this.stop();
        logger.detachAppender(this);
    }
}
