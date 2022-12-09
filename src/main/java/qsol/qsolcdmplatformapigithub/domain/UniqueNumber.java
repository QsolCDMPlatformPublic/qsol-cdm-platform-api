package qsol.qsolcdmplatformapigithub.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class UniqueNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String value;

    @Builder
    public UniqueNumber(Long id, String value) {
        this.id = id;
        this.value = value;
    }

}
