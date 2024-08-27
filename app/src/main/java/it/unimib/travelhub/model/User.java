package it.unimib.travelhub.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private String username;
    private String name;
    private String surname;
    private Long birthDate;
    private String photoUrl;
    private String email;
    private String idToken;

    public User(String username, String idToken) {
        this.username = username;
        this.idToken = idToken;
    }

    public User() {
        this.username = null;
        this.name = null;
        this.surname = null;
        this.birthDate = null;
        this.photoUrl = null;
        this.email = null;
        this.idToken=null;
    }

    public User(String username) {
        this.username = username;
    }
    public User(String name, String email, String idToken) {
        this.username = name;
        this.email = email;
        this.idToken = idToken;
        this.name = null;
        this.surname = null;
        this.birthDate = null;
        this.photoUrl = null;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof User){
            User user = (User) o;
            return this.idToken.equals(user.idToken);
        }
        return false;
    }

    public User(String username, String name, String surname, Long birthDate, String photoUrl, String email, String idToken) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.photoUrl = photoUrl;
        this.email = email;
        this.idToken = idToken;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    @Exclude
    public Map<String, Object> toMap(ArrayList<Long> travels) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("name", name);
        result.put("surname", surname);
        result.put("birthDate", birthDate);
        result.put("photoUrl", photoUrl);
        result.put("email", email);
        result.put("idToken", idToken);
        result.put("travels", travels);
        return result;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("name", name);
        result.put("surname", surname);
        result.put("birthDate", birthDate);
        result.put("photoUrl", photoUrl);
        result.put("email", email);
        result.put("idToken", idToken);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "name='" + username + '\'' +
                ", email='" + email + '\'' +
                ", idToken='" + idToken + '\'' +
                '}';
    }
    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public Long getBirthDate() {
        return birthDate;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setBirthDate(Long birthDate) { this.birthDate = birthDate; }
}
