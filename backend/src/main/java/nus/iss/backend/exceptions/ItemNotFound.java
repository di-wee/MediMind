package nus.iss.backend.exceptions;

public class ItemNotFound extends RuntimeException {
    public ItemNotFound(String message) {
        super(message);
    }
}
