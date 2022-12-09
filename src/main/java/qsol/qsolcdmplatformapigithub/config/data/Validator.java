package qsol.qsolcdmplatformapigithub.config.data;

import org.springframework.stereotype.Component;
import qsol.qsolcdmplatformapigithub.exception.user.UnauthorizedException;

@Component
public class Validator {

    public void validateLogin(UserSession userSession) {
            if (userSession == null) {
                throw new UnauthorizedException();
            }
    }
}
