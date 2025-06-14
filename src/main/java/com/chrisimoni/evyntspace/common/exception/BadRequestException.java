package com.chrisimoni.evyntspace.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
