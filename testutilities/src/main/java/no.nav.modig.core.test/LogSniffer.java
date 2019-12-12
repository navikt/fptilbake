//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package no.nav.modig.core.test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

public class LogSniffer implements TestRule {
    private final Map<ILoggingEvent, Boolean> logbackAppender = new LinkedHashMap<>();
    private final Level minimumLevel;
    private final boolean denyOthersWhenMatched;

    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        Logger.getLogger("global").setLevel(java.util.logging.Level.FINEST);
    }

    public LogSniffer() {
        this(Level.INFO);
    }

    public LogSniffer(Level minimumLevel) {
        this(minimumLevel, true);
    }

    public LogSniffer(Level minimumLevel, boolean denyOthersWhenMatched) {
        this.minimumLevel = minimumLevel;
        this.denyOthersWhenMatched = denyOthersWhenMatched;
        installTurboFilter(getLoggerContext());
    }

    private LoggerContext getLoggerContext() {
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    private void installTurboFilter(LoggerContext lc) {
        lc.addTurboFilter(new TurboFilter() {
            public FilterReply decide(Marker marker, ch.qos.logback.classic.Logger logger, Level level, String format, Object[] argArray, Throwable t) {
                if (format != null && level != null && level.isGreaterOrEqual(LogSniffer.this.minimumLevel)) {
                    LoggingEvent loggingEvent = new LoggingEvent(ch.qos.logback.classic.Logger.FQCN, logger, level, format, t, argArray);
                    LogSniffer.this.logbackAppender.put(loggingEvent, Boolean.FALSE);
                    return LogSniffer.this.denyOthersWhenMatched ? FilterReply.DENY : FilterReply.NEUTRAL;
                } else {
                    return FilterReply.NEUTRAL;
                }
            }
        });
    }

    public void assertHasErrorMessage(String substring) {
        if (!hasLogEntry(substring, null, Level.ERROR)) {
            throw new AssertionError(String.format("Could not find log message matching [%s], with level ERROR.  Has [%s]", substring, this));
        } else {
            markEntryAsserted(substring, null, Level.ERROR);
        }
    }

    public void assertHasErrorMessage(String substring, Class<? extends Throwable> t) {
        if (!hasLogEntry(substring, t, Level.ERROR)) {
            throw new AssertionError(String.format("Could not find log message matching [%s], for exception [%s], with level ERROR.  Has [%s]", substring, t, this));
        } else {
            markEntryAsserted(substring, t, Level.ERROR);
        }
    }

    public void assertHasWarnMessage(String substring) {
        if (!hasLogEntry(substring, null, Level.WARN)) {
            throw new AssertionError(String.format("Could not find log message matching [%s], with level WARN.  Has [%s]", substring, this));
        } else {
            markEntryAsserted(substring, null, Level.WARN);
        }
    }

    public void assertHasWarnMessage(String substring, Class<? extends Throwable> t) {
        if (!hasLogEntry(substring, t, Level.WARN)) {
            throw new AssertionError(String.format("Could not find log message matching [%s], for exception [%s], with level WARN.  Has [%s]", substring, t, this));
        } else {
            markEntryAsserted(substring, t, Level.WARN);
        }
    }

    public void assertHasInfoMessage(String substring) {
        if (!hasLogEntry(substring, null, Level.INFO)) {
            throw new AssertionError(String.format("Could not find log message matching [%s], with level INFO.  Has [%s]", substring, this));
        } else {
            markEntryAsserted(substring, null, Level.INFO);
        }
    }

    public void assertNoErrors() {
        if (countErrors() > 0) {
            throw new AssertionError("No errors expected, but has " + this);
        }
    }

    public void assertNoWarnings() {
        if (countWarnings() > 0) {
            throw new AssertionError("No warnings expected, but has " + this);
        }
    }

    public void assertNoErrorsOrWarnings() {
        assertNoErrors();
        assertNoWarnings();
    }

    public void assertNoLogEntries() {
        if (!logbackAppender.isEmpty()) {
            throw new AssertionError("No log entries expected, but has " + this);
        }
    }

    private void markEntryAsserted(String substring, Class<? extends Throwable> t, Level level) {
        List<ILoggingEvent> events = new ArrayList<>(logbackAppender.keySet());
        for (ILoggingEvent loggingEvent : events) {
            if (eventMatches(loggingEvent, substring, t, level)) {
                logbackAppender.put(loggingEvent, Boolean.TRUE);
            }
        }
    }

    private boolean eventMatches(ILoggingEvent loggingEvent, String substring, Class<? extends Throwable> t, Level level) {
        if (substring != null && !loggingEvent.getFormattedMessage().contains(substring)) {
            return false;
        }
        if (t != null && (loggingEvent.getThrowableProxy() == null || loggingEvent.getThrowableProxy().getClassName() == null || !loggingEvent.getThrowableProxy().getClassName().equals(t.getName()))) {
            return false;
        }
        return level == null || level == loggingEvent.getLevel();
    }

    private long countErrors() {
        return countUnassertedEntries(Level.ERROR);
    }

    private long countWarnings() {
        return countUnassertedEntries(Level.WARN);
    }

    private long countUnassertedEntries(Level level) {
        return logbackAppender.entrySet()
            .stream()
            .filter(e -> e.getKey().getLevel().equals(level) && e.getValue() != Boolean.TRUE)
            .count();
    }

    public int countEntries(String substring) {
        return countLogbackEntries(substring, null, null);
    }

    private boolean hasLogEntry(String substring, Class<? extends Throwable> t, Level level) {
        return countLogbackEntries(substring, t, level) > 0;
    }

    private int countLogbackEntries(String substring, Class<? extends Throwable> t, Level level) {
        int count = 0;
        for (ILoggingEvent loggingEvent : logbackAppender.keySet()) {
            if (eventMatches(loggingEvent, substring, t, level)) {
                ++count;
            }
        }

        return count;
    }

    public void clearLog() {
        logbackAppender.clear();
    }

    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                    LogSniffer.this.assertNoErrorsOrWarnings();
                } finally {
                    LogSniffer.this.getLoggerContext().resetTurboFilterList();
                }

            }
        };
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (ILoggingEvent event : logbackAppender.keySet()) {
            buf.append(event.getLevel())
                .append(":")
                .append(event.getFormattedMessage())
                .append('\n');
        }
        return buf.toString();
    }


}
