package qsol.qsolcdmplatformapigithub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import qsol.qsolcdmplatformapigithub.domain.UniqueNumber;

import java.util.Optional;

@Repository
public interface UniqueNumberRepository extends JpaRepository<UniqueNumber, Long> {
    Optional<UniqueNumber> findTopByOrderByIdDesc();
}
