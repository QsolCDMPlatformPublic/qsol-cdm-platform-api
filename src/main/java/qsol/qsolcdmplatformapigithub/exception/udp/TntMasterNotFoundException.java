package qsol.qsolcdmplatformapigithub.exception.udp;

import qsol.qsolcdmplatformapigithub.exception.GlobalException;

public class TntMasterNotFoundException extends GlobalException {


    private static final String MESSAGE = "Not Found TntMaster";
    public TntMasterNotFoundException() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
