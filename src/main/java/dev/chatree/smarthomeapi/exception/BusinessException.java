package dev.chatree.smarthomeapi.exception;

import lombok.Getter;
import org.apache.logging.log4j.Level;

@Getter
public class BusinessException extends Exception {

    private final Level logLevel;

    public BusinessException(String message) {
        super(message);
        this.logLevel = Level.INFO;
    }

    public BusinessException(Level logLevel, String message) {
        super(message);
        this.logLevel = logLevel;
    }

}
