package site.campingon.campingon.camp.entity;

import jakarta.persistence.*;
import lombok.*;
import site.campingon.campingon.common.converter.IndutyConverter;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "camp_site"/*, indexes = {
        @Index(name = "idx_is_available", columnList = "is_available"),
        @Index(name = "idx_site_type", columnList = "site_type")
}*/)
public class CampSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "camp_id", nullable = false)
    private Camp camp; // 캠핑장 (N:1 관계)

    @Column(name = "maximum_people", nullable = false)
    private Integer maximumPeople; // 최대 수용 인원

    @Column(nullable = false)
    private Integer price; // 가격

    @Column(length = 50, nullable = false)
    @Convert(converter = IndutyConverter.class) //converter 사용
    private Induty siteType; // 업종 구분

    @Column(name = "indoor_facility", length = 255)
    private String indoorFacility;

    @Builder.Default
    @Column(name = "is_available", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isAvailable = true;


    public void updateCampSite(Camp camp, int maximumPeople, int price, Induty induty, String innerFacility) {
        this.camp = camp;
        this.maximumPeople = maximumPeople;
        this.price = price;
        this.siteType = induty;
        this.indoorFacility = innerFacility;
    }
//
//    @PrePersist
//    @PreUpdate
//    private void setDefaultValues() {
//        if (price == null) {
//            this.price = siteType.getPrice();
//        }
//        if (maximumPeople == null) {
//            this.maximumPeople = siteType.getMaximum_people();
//        }
//    }

}