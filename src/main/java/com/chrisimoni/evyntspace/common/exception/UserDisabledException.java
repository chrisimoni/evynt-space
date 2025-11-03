package com.chrisimoni.evyntspace.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDisabledException extends RuntimeException {
    public UserDisabledException(String message) {
        super(message);
    }
}
