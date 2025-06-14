package com.chrisimoni.evyntspace.common.util;

import com.chrisimoni.evyntspace.common.exception.BadRequestException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil {
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&-]+(?:\\.[a-zA-Z0-9_+&-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,20}$";


    public static void validateEmailFormat(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            throw new BadRequestException("Invalid email address");
        }
    }

    public static void validatePassword(String password) {
        Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        if(!matcher.matches()) {
            throw new BadRequestException("Password must include at least one uppercase letter," +
                    " one lowercase letter, one number, and one special character.");
        }
    }
}
