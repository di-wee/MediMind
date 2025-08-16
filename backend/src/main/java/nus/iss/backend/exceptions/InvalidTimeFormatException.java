package nus.iss.backend.exceptions;

public class InvalidTimeFormatException extends RuntimeException {
    public InvalidTimeFormatException(String message) {
        super(message);
    }
}
