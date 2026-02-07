package za.co.psybergate.chatterbox.application.common.logging.slf4j;

import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;

public abstract class AbstractSlf4jLogger {

    protected String truncate(Object object) {
        return truncate(object.toString(), 300);
    }

    @SuppressWarnings("SameParameterValue")
    protected String truncate(Object object, int length) {
        return truncate(object.toString(), length);
    }

    @SuppressWarnings("SameParameterValue")
    protected String truncate(String string, int length) {
        if (string == null || string.isEmpty()) {
            throw new ApplicationException("Cannot truncate null/empty string");
        } else if (length <= 0) {
            return string;
        }
        return string.length() > length ? string.substring(0, length - 4) + " ..." : string;
    }
}
