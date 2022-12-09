package qsol.qsolcdmplatformapigithub.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class CsvDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long value;
    private LocalDateTime createdDate;

    @Builder
    public CsvDetail(Long id, Long value, LocalDateTime createdDate) {
        this.id = id;
        this.value = value;
        this.createdDate = createdDate;
    }
}
