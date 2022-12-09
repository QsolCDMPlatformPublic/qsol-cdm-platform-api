package qsol.qsolcdmplatformapigithub.exception.user;

import qsol.qsolcdmplatformapigithub.exception.GlobalException;

public class UserNotFoundException extends GlobalException {

    private static final String MESSAGE = "User not found.";

    public UserNotFoundException() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 404;
    }
}
