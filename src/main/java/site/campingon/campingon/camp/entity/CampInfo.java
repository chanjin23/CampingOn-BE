package site.campingon.campingon.camp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "camp_info"
        /*, indexes = {
        @Index(name = "idx_camp_name", columnList = "camp_name")
}*/
)
public class CampInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @OneToOne
    @JoinColumn(name = "camp_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private Camp camp;

    @Builder.Default
    @Column(name = "recommend_cnt")
    private Integer recommendCnt = 0;

    @Builder.Default
    @Column(name = "bookmark_cnt")
    private Integer bookmarkCnt = 0;

    public void updateBookmarkCntUp() {
        bookmarkCnt++;
    }

    public void updateBookmarkCntDown() {
        bookmarkCnt--;
    }

    public void updateRecommendCntUp() {
        recommendCnt++;
    }
}