package com.notic.event;

public record EmailVerificationEvent(String email, long code) {}
