/**
 * User information class
 */

package com.example.hamburger_w.eventreporter;

public class User {
    private String username;
    private String password;
    private long time;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public User(final String username, final String password, final long timeStamp) { // within the method, final username cannot be re-assigned
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        this.username = username;
        this.password = password;
        this.time = timeStamp;
    }
}
