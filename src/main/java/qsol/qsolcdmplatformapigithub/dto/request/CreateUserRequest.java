package qsol.qsolcdmplatformapigithub.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Data
public class CreateUserRequest {

    @Size(min = 6, max = 20, message = "Please enter more than 6 characters and less than 20 characters.")
    @NotBlank(message = "Spaces are not allowed.")
    private String username;

    @Size(min = 8, max = 30, message = "Please enter more than 8 characters and less than 30 characters.")
    @NotBlank(message = "Spaces are not allowed.")
    private String password;
}
