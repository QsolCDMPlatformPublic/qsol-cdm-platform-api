package qsol.qsolcdmplatformapigithub.exception.user;

import qsol.qsolcdmplatformapigithub.exception.GlobalException;

public class UnauthorizedException extends GlobalException {

    private static final String MESSAGE = "unauthorized";

    public UnauthorizedException() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
