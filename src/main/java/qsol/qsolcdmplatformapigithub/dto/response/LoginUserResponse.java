package qsol.qsolcdmplatformapigithub.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import qsol.qsolcdmplatformapigithub.domain.User;

@NoArgsConstructor
@Data
public class LoginUserResponse {

    private Long id;
    private String username;

    public LoginUserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }
}
