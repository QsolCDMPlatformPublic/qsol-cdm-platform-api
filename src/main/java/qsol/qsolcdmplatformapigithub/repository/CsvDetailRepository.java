package qsol.qsolcdmplatformapigithub.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import qsol.qsolcdmplatformapigithub.domain.CsvDetail;

import java.util.List;

@Repository
public interface CsvDetailRepository extends JpaRepository<CsvDetail, Long> {
    boolean existsByIdGreaterThan(Long id);
    List<CsvDetail> findAllByOrderByIdAsc(Pageable page);
    List<CsvDetail> findByIdGreaterThanOrderByIdAsc(Long id, Pageable page);
}
