package de.NikomitK.RaspiOpener.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum Error {

    OK,
    KEY_NOT_SAVED("01"),
    PASSWORD_EXISTS("02"),
    KEY_MISMATCH("03"),
    OTP_NOT_EXISTING("04"),
    PASSWORD_MISMATCH("05"),
    PASSWORD_NOT_SAVED("06"),
    NO_PASSWORD_TO_REPLACE("07"),
    OTP_NOT_SAVED("08"),
    INTERNAL_SERVER_ERROR("09"),
    WRONG_COMMANG("10");

    private String error = null;

}
