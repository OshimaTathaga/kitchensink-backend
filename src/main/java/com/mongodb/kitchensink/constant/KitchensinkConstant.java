package com.mongodb.kitchensink.constant;

public interface KitchensinkConstant {
    String EMAIL_VALIDATION_REGEX = "^\\S+@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z0-9-]+$";
    String PASSWORD_VALIDATION_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$";
    String PHONE_NUMBER_VALIDATION_REGEX = "^[6-9][0-9]{9}$";
}
