package com.webhook.relay.chatterbox.common.logging.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.Getter;
import lombok.Setter;

/// Credit to <code>Francois Vermaak</code> and <code>Gena Marais</code> for designing this
@Getter
@Setter
public class LevelRangeFilter extends Filter<ILoggingEvent> {

    /// Do we return ACCEPT when a match occurs.
    /// Default is <code>false</code>, so that later filters get run by default
    boolean acceptOnMatch = true;

    Level levelMin;

    public FilterReply decide(ILoggingEvent event) {
        if (this.levelMin != null && !event.getLevel().isGreaterOrEqual(levelMin)) {
            // level of event is less than minimum
            return FilterReply.DENY;
        }

        if (acceptOnMatch) {
            // this filter set up to bypass later filters and always return
            // accept if level in range
            return FilterReply.ACCEPT;
        } else {
            // event is ok for this filter; allow later filters to have a look.
            return FilterReply.NEUTRAL;
        }
    }

}