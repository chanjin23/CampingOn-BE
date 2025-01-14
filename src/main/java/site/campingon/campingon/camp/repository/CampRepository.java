package site.campingon.campingon.camp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.camp.entity.Camp;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampRepository extends JpaRepository<Camp, Long> {

  // 캠핑장 정보의 추천수의 내림차순으로 캠핑장 정렬
  @Query(value = """
    SELECT c FROM Camp c
    JOIN FETCH c.campInfo ci
    JOIN FETCH c.campAddr
    ORDER BY ci.recommendCnt DESC
    """, countQuery = """
    SELECT COUNT(c) FROM Camp c
    """
  )
  Page<Camp> findPopularCamps(Pageable pageable);

  @Query("""
          SELECT c FROM Camp c
          JOIN FETCH c.campInfo
          JOIN FETCH c.campAddr
          WHERE c.id= :campId
          """)
  Optional<Camp> findById(@Param("campId") Long campId);

  // 사용자의 isMarked 된 캠핑장 목록 페이지
  @Query(value = """
          SELECT c FROM Camp c 
          JOIN FETCH c.campInfo
          JOIN FETCH c.campAddr
          JOIN c.bookmarks b 
          WHERE b.user.id = :userId 
          ORDER BY b.createdAt DESC
          """,countQuery = "SELECT COUNT(c) FROM Camp c JOIN c.bookmarks b WHERE b.user.id= :userId")
  Page<Camp> findByBookmarksUserId(@Param("userId") Long userId, Pageable pageable);

  //쿼리 최적화 where in 조건
  @Modifying
  @Query("DELETE FROM Camp c WHERE c.id IN :ids")
  int deleteByIds(@Param("ids") List<Long> ids);

}
