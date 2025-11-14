package com.chrisimoni.evyntspace.common.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenException extends AuthenticationException {
    public InvalidTokenException(String msg) { super(msg); }
}
