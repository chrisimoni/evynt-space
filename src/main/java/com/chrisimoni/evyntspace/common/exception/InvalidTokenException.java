package com.chrisimoni.evyntspace.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.AuthenticationException;

@Getter
@Setter
public class InvalidTokenException extends AuthenticationException {
    public InvalidTokenException(String msg) { super(msg); }
}
