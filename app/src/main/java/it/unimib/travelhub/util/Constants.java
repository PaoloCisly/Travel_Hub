package it.unimib.travelhub.util;

public class Constants {
    public static final String PICS_FOLDER = "/.picFolder/";
    public static final String DESTINATIONS_HINTS = "destinations_hints";
    public static final String FRIENDS_HINTS = "friends_hints";
    public static final String DESTINATIONS_TEXTS = "destinations_texts";
    public static final String FRIENDS_TEXTS = "friends_texts";
    public static final String SHARED_PREFERENCES_FILE_NAME = "it.unimib.travelhub.preferences";
    public static final String ENCRYPTED_SHARED_PREFERENCES_FILE_NAME = "it.unimib.travelhub.encrypted_preferences";
    public static final String ENCRYPTED_DATA_FILE_NAME = "it.unimib.travelhub.encrypted_file.txt";

    // User information
    public static final String EMAIL_ADDRESS = "email_address";
    public static final String PASSWORD = "password";
    public static final String USERNAME = "username";
    public static final String USER_NAME = "user_name";
    public static final String USER_SURNAME = "user_surname";
    public static final String USER_BIRTHDATE = "user_birthDate";
    public static final String USER_PROFILE_IMAGE = "user_profile_image";

    //FIREBASE
    public static final String FIREBASE_REALTIME_DATABASE = "https://travelhub-9bc21-default-rtdb.europe-west1.firebasedatabase.app/";
    public static final String ID_TOKEN = "google_token";
    public static final String FIREBASE_USERS_COLLECTION = "users";
    public static final String FIREBASE_USERNAMES_COLLECTION = "usernames";

    public static final String FIREBASE_TRAVELS_COLLECTION = "travels";

    public static final String UNEXPECTED_ERROR = "unexpected_error";
    public static final String INVALID_USER_ERROR = "invalidUserError";
    public static final String INVALID_CREDENTIALS_ERROR = "invalidCredentials";
    public static final String USER_COLLISION_ERROR = "userCollisionError";
    public static final String WEAK_PASSWORD_ERROR = "passwordIsWeak";
    public static final String USERNAME_NOT_AVAILABLE = "Username already taken";
    public static final String TRAVELS_TEST_JSON_FILE = "travels_data.json";
    public static final String TRAVEL_ADDED = "TRAVEL_ADDED";
    public static final String TRAVEL_DELETED = "TRAVEL_DELETED";

    public static final String TRAVEL_UPDATED = "TRAVEL_UPDATED";

    public static final int TRAVELS_DATABASE_VERSION = 1;
    public static final String TRAVELS_DATABASE_NAME = "travels_database";
    public static final String LAST_UPDATE = "last_update";
    public static final long FRESH_TIMEOUT = 60 * 60 * 1000; // 1 hour

    //Images
    public static final String PROFILE_PICTURE_FILE_NAME = "profile_picture.webp";
    public static final String PROFILE_PICTURE_TEMP_FILE_NAME = "profile_picture_temp.webp";
    public static final String PROFILE_IMAGE_REMOTE_PATH = "profile_images/";

}
