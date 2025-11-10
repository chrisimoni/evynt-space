package com.chrisimoni.evyntspace.user.enums;

public enum TokenType {
    // Constants must be separated by a comma (,)
    REFRESH_TOKEN("Refresh Token"),
    PASSWORD_RESET_TOKEN("Password Reset Token"),
    LOGIN_TOKEN("Login Token");

    private final String displayValue;

    TokenType(String displayValue) {
        this.displayValue = displayValue;
    }

    // This method returns the user-friendly string
    @Override
    public String toString() {
        return displayValue;
    }
}
