package qsol.qsolcdmplatformapigithub.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import qsol.qsolcdmplatformapigithub.domain.User;

@NoArgsConstructor
@Data
public class CreateUserResponse {

    private Long id;
    private String username;

    public CreateUserResponse(User savedUser) {
        this.id = savedUser.getId();
        this.username = savedUser.getUsername();
    }
}
