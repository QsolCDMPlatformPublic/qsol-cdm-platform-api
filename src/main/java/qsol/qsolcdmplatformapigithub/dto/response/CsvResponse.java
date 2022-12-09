package qsol.qsolcdmplatformapigithub.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public class CsvResponse {
    private String csvFileName;
    private int csvDataCount;

    public CsvResponse(String csvFileName, int csvDataCount) {
        this.csvFileName = csvFileName;
        this.csvDataCount = csvDataCount;
    }
}
