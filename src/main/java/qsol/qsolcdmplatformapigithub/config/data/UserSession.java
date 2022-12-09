package qsol.qsolcdmplatformapigithub.config.data;

import lombok.Data;
import qsol.qsolcdmplatformapigithub.dto.response.LoginUserResponse;

@Data
public class UserSession {

    private Long id;
    private String username;

    public UserSession(LoginUserResponse response) {
        this.id = response.getId();
        this.username = response.getUsername();
    }
}
