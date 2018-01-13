/**
 * Events class with attributes, getter and setter
 */

package com.example.hamburger_w.eventreporter;

public class Event {
    private String title;
    private String address;
    private String description;

    private int like;
    private String id;
    private long time;
    private String username;
    private String imgUri;
    private int commentNumber;
    private double latitude;
    private double longitude;

    /**
     * getter and setter, encapsulation
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public int getCommentNumber() {
        return commentNumber;
    }

    public void setCommentNumber(int commentNumber) {
        this.commentNumber = commentNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * constructor
     */
    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public Event() {
    }

    public Event(String title, String address, String description) {
        this.title = title;
        this.address = address;
        this.description = description;
    }
}

