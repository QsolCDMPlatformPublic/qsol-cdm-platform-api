package qsol.qsolcdmplatformapigithub.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import qsol.qsolcdmplatformapigithub.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Data
public class UserResponse {

    private Long id;
    private String username;
    private LocalDateTime createdDate;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.createdDate = user.getCreatedDate();
    }
}
