package qsol.qsolcdmplatformapigithub.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Data
public class ErrorResponse {
    private String message;
    private Map<String, String> field = new HashMap<>();
    private int statusCode;

    @Builder
    public ErrorResponse(String message, HashMap<String, String> field, int statusCode) {
        this.message = message;
        this.field = field;
        this.statusCode = statusCode;
    }
}
