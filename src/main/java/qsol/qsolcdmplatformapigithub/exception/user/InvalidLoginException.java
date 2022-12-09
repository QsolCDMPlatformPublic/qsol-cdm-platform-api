package qsol.qsolcdmplatformapigithub.exception.user;

import qsol.qsolcdmplatformapigithub.exception.GlobalException;

public class InvalidLoginException extends GlobalException {

    private static final String MESSAGE = "username or password is invalid";

    public InvalidLoginException() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
