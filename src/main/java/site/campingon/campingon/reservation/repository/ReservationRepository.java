package site.campingon.campingon.reservation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.reservation.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 유저의 모든 예약 조회
    @EntityGraph(attributePaths = {"review"})
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId " +
            "ORDER BY CASE " +
            "WHEN r.status = 'RESERVED' THEN 0 " +
            "WHEN r.status = 'COMPLETED' THEN 1 " +
            "ELSE 2 END, r.checkin ASC")
    Page<Reservation> findReservationsByUserId(@Param("userId")Long userId, Pageable pageable);

    // 특정 예약의 상세 정보 조회 (연관된 캠프, 주소 정보 포함)
    @EntityGraph(attributePaths = {"camp", "campSite"})
    Optional<Reservation> findById(Long id);

    // 예약완료 상태 중 체크인일자가 가까운 예약 정보 조회
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND r.status = 'RESERVED' ORDER BY r.checkin ASC LIMIT 1")
    Reservation findUpcomingReservationByUserId(@Param("userId")Long userId);

    List<Reservation> findByCheckin(LocalDateTime targetTime);

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        WHERE r.campSite.id = :campSiteId 
          AND r.status = 'RESERVED'
          AND DATE(r.checkout) > DATE(:checkin) 
          AND DATE(r.checkin) < DATE(:checkout)
    """)
    boolean existDuplicateCampSite(@Param("campSiteId") Long campSiteId,
                                    @Param("checkin") LocalDate checkin,
                                    @Param("checkout") LocalDate checkout);
}
