package qsol.qsolcdmplatformapigithub.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Getter
@DynamicUpdate
@NoArgsConstructor
public class TntData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int masterId;

    private Long data;

    @Builder
    public TntData(int masterId, Long data) {
        this.masterId = masterId;
        this.data = data;
    }
}
