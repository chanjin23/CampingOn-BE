package site.campingon.campingon.camp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import site.campingon.campingon.bookmark.entity.Bookmark;
import site.campingon.campingon.common.public_data.dto.GoCampingParsedResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "camp",
        indexes = {
                @Index(name = "idx_camp_id", columnList = "id"),
                @Index(name = "idx_camp_name", columnList = "camp_name")
        }
)
public class Camp{

  @Id
  @Column(columnDefinition = "INT UNSIGNED")
  private Long id;

  @Column(name = "camp_name", length = 50, nullable = false)
  private String campName;

  @Column(length = 20)
  private String tel;

  //    @Lob // MongoDB 호환성을 위해선 Lob 사용 권장
  @Column(name = "intro", columnDefinition = "TEXT")
  private String intro;

  @Column(name = "line_intro", length = 255)
  private String lineIntro; // 요약

  @Column(length = 255)
  private String homepage;

  @Column(name = "outdoor_facility", length = 255)
  private String outdoorFacility;  // 부대 시설

  @Column(name = "thumb_image", length = 255)
  private String thumbImage;  // 썸네일 이미지

  @Column(name = "animal_admission", length = 50)
  private String animalAdmission;

  @OneToMany(mappedBy = "camp", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CampKeyword> keywords=new ArrayList<>();

  @OneToMany(mappedBy = "camp", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CampImage> images=new ArrayList<>();

  @OneToMany(mappedBy = "camp",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
  @Builder.Default
  private List<CampInduty> induty=new ArrayList<>();  // 업종

  @OneToOne(mappedBy = "camp", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
  private CampAddr campAddr;

  @OneToOne(mappedBy = "camp", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
  private CampInfo campInfo;

  @OneToMany(mappedBy = "camp", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Bookmark> bookmarks=new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
  private LocalDateTime createdAt;

  @Column(name = "modified_at", nullable = false, columnDefinition = "DATETIME")
  private LocalDateTime modifiedAt;

  public Camp updateCamp(GoCampingParsedResponseDto data) {
    this.id = data.getContentId();
    this.campName = data.getFacltNm();
    this.lineIntro = data.getLineIntro();
    this.tel = data.getTel();
    this.homepage = data.getHomepage();
    this.outdoorFacility = data.getSbrsCl();
    this.thumbImage = data.getFirstImageUrl();
    this.animalAdmission = data.getAnimalCmgCl();
    this.createdAt = LocalDateTime.parse(
            data.getCreatedtime()
            , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    this.modifiedAt = LocalDateTime.parse(
            data.getModifiedtime()
            , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    return this;  //자기자신 반환
  }
}
