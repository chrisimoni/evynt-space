package com.chrisimoni.evyntspace.common.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class ApiErrorDetail {
    private String field;
    private String message;
}
