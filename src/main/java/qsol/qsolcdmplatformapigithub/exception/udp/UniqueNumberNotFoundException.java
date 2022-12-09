package qsol.qsolcdmplatformapigithub.exception.udp;

import qsol.qsolcdmplatformapigithub.exception.GlobalException;

public class UniqueNumberNotFoundException extends GlobalException {

    private static final String MESSAGE = "Not Found UniqueNumber";

    public UniqueNumberNotFoundException() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
