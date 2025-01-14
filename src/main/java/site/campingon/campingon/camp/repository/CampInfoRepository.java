package site.campingon.campingon.camp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.camp.entity.CampInfo;

import java.util.Optional;

@Repository
public interface CampInfoRepository extends JpaRepository<CampInfo, Long> {
    @Query("SELECT ci FROM CampInfo ci " +
            "JOIN FETCH ci.camp c " +
            "JOIN FETCH c.campAddr " +
            "WHERE c.id = :campId")
    Optional<CampInfo> findByCampId(@Param("campId") Long id);
}