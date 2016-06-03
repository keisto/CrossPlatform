package com.sandbaks.t_keiser_cross_platform;

public class User {
    String email;
    String firstname;
    String lastname;
    String provider;
    int    age;

    public User() {
    }
    public int getAge() {
        return age;
    }
    public String getFirstname() {
        return firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public String getEmail() {
        return email;
    }
    public String getProvider(){
        return provider;
    }
}
