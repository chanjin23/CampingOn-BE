package site.campingon.campingon.bookmark.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.bookmark.entity.Bookmark;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
  boolean existsByCampIdAndUserId(Long campId, Long userId);

  @Query("SELECT b FROM Bookmark b " +
          "JOIN FETCH b.camp c " +
          "JOIN FETCH c.campAddr " +
          "JOIN FETCH c.campInfo " +
          "JOIN FETCH b.user " +
          "WHERE b.camp.id = :campId AND b.user.id = :userId")
  Optional<Bookmark> findByCampIdAndUserId(@Param("campId") Long campId, @Param("userId") Long userId);
}