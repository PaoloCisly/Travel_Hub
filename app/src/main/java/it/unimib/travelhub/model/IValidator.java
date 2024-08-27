package it.unimib.travelhub.model;

public interface IValidator {
    ValidationResult validateMail(String mail);
    ValidationResult validatePassword(String password);
}
