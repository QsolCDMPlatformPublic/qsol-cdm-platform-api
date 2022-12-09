package qsol.qsolcdmplatformapigithub.exception.file;

import qsol.qsolcdmplatformapigithub.exception.GlobalException;

public class FileUnselectedException extends GlobalException {

    private static final String MESSAGE = "파일을 선택해 주세요.";

    public FileUnselectedException() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
