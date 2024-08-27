package it.unimib.travelhub.util;

import android.content.Context;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimib.travelhub.R;
import it.unimib.travelhub.model.IValidator;
import it.unimib.travelhub.model.ValidationResult;

public class CredentialValidator implements IValidator {

    private Context context;
    private static volatile CredentialValidator INSTANCE = null;

    private CredentialValidator(){}

    public static CredentialValidator getInstance(){
        if(INSTANCE == null){
            synchronized (CredentialValidator.class){
                if(INSTANCE == null){
                    INSTANCE = new CredentialValidator();
                }
            }
        }
        return INSTANCE;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public ValidationResult validateMail(String email) {
        if(EmailValidator.getInstance().isValid((email))){
            return new ValidationResult(true, context.getString(R.string.mail_valid));
        }
        else{
            return new ValidationResult(false,context.getString(R.string.error_email));
        }
    }

    @Override
    public ValidationResult validatePassword(String password) {
        if (password.length() < 6) {
            return new ValidationResult(false, context.getString(R.string.pwd_error_numberOfcharacters));
        }

        // Contiene almeno una lettera maiuscola
        if (!containsUppercase(password)) {
            return new ValidationResult(false, context.getString(R.string.pwd_error_capitalLetter));
        }

        // Contiene almeno una lettera minuscola
        if (!containsLowercase(password)) {
            return new ValidationResult(false, context.getString(R.string.pwd_error_lowerCaseLetter));
        }

        // Contiene almeno un numero
        if (!containsDigit(password)) {
            return new ValidationResult(false, context.getString(R.string.pwd_error_number));
        }

        // Contiene almeno un carattere speciale
        if (!containsSpecialCharacter(password)) {
            return new ValidationResult(false, context.getString(R.string.pwd_error_specialCharacter));
        }

        // Passa tutte le regole di validazione
        return new ValidationResult(true, context.getString(R.string.pwd_valid));
    }

    private static boolean containsUppercase(String password) {
        return !password.equals(password.toLowerCase());
    }

    private static boolean containsLowercase(String password) {
        return !password.equals(password.toUpperCase());
    }

    private static boolean containsDigit(String password) {
        return password.matches(".*\\d.*");
    }
    private static boolean containsSpecialCharacter(String password) {
        Pattern specialCharPattern = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
        Matcher matcher = specialCharPattern.matcher(password);
        return matcher.find();
    }
}
