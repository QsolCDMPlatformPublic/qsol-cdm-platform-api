package qsol.qsolcdmplatformapigithub.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import qsol.qsolcdmplatformapigithub.domain.TntData;

import java.util.List;

public interface DataRepository extends JpaRepository<TntData, Integer> {
    List<TntData> findAllByMasterIdOrderByIdAsc(int masterId, Pageable page);

    List<TntData> findByMasterIdAndIdGreaterThanOrderByIdAsc(int masterId, Long id, Pageable page);

    Boolean existsByIdGreaterThan(Long id);
}
