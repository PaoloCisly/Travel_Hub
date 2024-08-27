package it.unimib.travelhub.model;

public class ValidationResult {
    public boolean success;
    private final String message;

    public ValidationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
