package it.unimib.travelhub.crypto_util;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class DataEncryptionUtil {
    private final Application application;

    public DataEncryptionUtil(Application application) {
        this.application = application;
    }

    /**
     * Writes a value using EncryptedSharedPreferences (both key and value will be encrypted).
     * @param sharedPreferencesFileName the name of the SharedPreferences file where to write the data
     * @param key The key associated with the value
     * @param value The value to be written
     */
    public void writeSecretDataWithEncryptedSharedPreferences(String sharedPreferencesFileName,
                                                              String key, String value)
            throws GeneralSecurityException, IOException {

        MasterKey mainKey = new MasterKey.Builder(application)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        // Creates a file with this name, or replaces an existing file that has the same name.
        // Note that the file name cannot contain path separators.
        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                application,
                sharedPreferencesFileName,
                mainKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Reads a value encrypted using EncryptedSharedPreferences class.
     * @param sharedPreferencesFileName the name of the SharedPreferences file where data are saved
     * @param key The key associated with the value to be read
     * @return The decrypted value
     */
    public String readSecretDataWithEncryptedSharedPreferences(String sharedPreferencesFileName,
                                                               String key)
            throws GeneralSecurityException, IOException {

        MasterKey mainKey = new MasterKey.Builder(application)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                application,
                sharedPreferencesFileName,
                mainKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        return sharedPreferences.getString(key, null);
    }

    public void writeSecreteDataOnFile(String fileName, String data)
            throws GeneralSecurityException, IOException {

        MasterKey mainKey = new MasterKey.Builder(application)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();

        // Creates a file with this name, or replaces an existing file that has the same name.
        // Note that the file name cannot contain path separators.
        File fileToWrite = new File(application.getFilesDir(), fileName);
        EncryptedFile encryptedFile = new EncryptedFile.Builder(application,
                fileToWrite,
                mainKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build();

        // File cannot exist before using openFileOutput
        if (fileToWrite.exists()) {
            if (!fileToWrite.delete()) {
                throw new IOException("Cannot delete file: " + fileToWrite.getAbsolutePath());
            }
        }

        byte[] fileContent = data.getBytes(StandardCharsets.UTF_8);
        OutputStream outputStream = encryptedFile.openFileOutput();
        outputStream.write(fileContent);
        outputStream.flush();
        outputStream.close();
    }

    public void flushEncryptedSharedPreferences(String sharedPreferencesFileName)
            throws GeneralSecurityException, IOException {

        MasterKey mainKey = new MasterKey.Builder(application)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                application,
                sharedPreferencesFileName,
                mainKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
