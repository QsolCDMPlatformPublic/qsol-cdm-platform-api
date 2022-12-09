package qsol.qsolcdmplatformapigithub.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CursorResult<T> {
    private List<T> values;
    private boolean hasNext;
}
