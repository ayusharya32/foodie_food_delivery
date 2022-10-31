package com.pentaware.foodie.models.api;

public class EmailRequest {
    public String email;
    public String subject;
    public String body;

    public EmailRequest() {}

    public EmailRequest(String email, String subject, String body) {
        this.email = email;
        this.subject = subject;
        this.body = body;
    }
}
