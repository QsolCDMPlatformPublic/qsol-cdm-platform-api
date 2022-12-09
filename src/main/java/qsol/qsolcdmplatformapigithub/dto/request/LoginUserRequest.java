package qsol.qsolcdmplatformapigithub.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Data
public class LoginUserRequest {

    @NotBlank(message = "Please enter your username.")
    private String username;

    @NotBlank(message = "Please enter your password.")
    private String password;
}
