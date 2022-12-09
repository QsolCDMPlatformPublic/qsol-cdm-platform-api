package qsol.qsolcdmplatformapigithub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import qsol.qsolcdmplatformapigithub.domain.TntMaster;

import java.util.Optional;

@Repository
public interface MasterRepository extends JpaRepository<TntMaster, Integer> {

    Optional<TntMaster> findTopByOrderByIdDesc();
}
