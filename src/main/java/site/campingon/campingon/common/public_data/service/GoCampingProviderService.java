package site.campingon.campingon.common.public_data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampInduty;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.entity.Induty;
import site.campingon.campingon.camp.repository.*;
import site.campingon.campingon.common.public_data.GoCampingPath;
import site.campingon.campingon.common.public_data.dto.GoCampingParsedResponseDto;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static site.campingon.campingon.camp.entity.Induty.*;
import static site.campingon.campingon.common.public_data.PublicDataConstants.*;
import static site.campingon.campingon.common.public_data.PublicDataConstants.MOBILE_APP;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoCampingProviderService {
    private final CampSiteRepository campSiteRepository;
    private final CampIndutyRepository campIndutyRepository;

    @Value("${public-data.go-camping}")
    private String serviceKey;

    //CampInduty 데이터 삽입
    @Transactional
    public void upsertCampInduty(Camp camp, GoCampingParsedResponseDto data) {
        //기존에 데이터가 있다면 삭제
        campIndutyRepository.deleteAllByCampId(data.getContentId());

        Map<Induty, Integer> siteCounts = Map.of(
                NORMAL_SITE, data.getGnrlSiteCo(),
                CAR_SITE, data.getAutoSiteCo(),
                GLAMP_SITE, data.getGlampSiteCo(),
                CARAV_SITE, data.getCaravSiteCo(),
                PERSONAL_CARAV_SITE, data.getIndvdlCaravSiteCo()
        );

        //야영지가 1이상 존재하면 Induty 테이블에 저장
        siteCounts.forEach((induty, count) -> {
            if (count != 0) {
                CampInduty campInduty = CampInduty.builder()
                        .induty(induty)
                        .camp(camp)
                        .build();
                campIndutyRepository.save(campInduty);
            }
        });
    }

    //CampSite 데이터 삽입
    @Transactional
    public void createCampSite(Camp camp, Integer siteCnt,
                               Induty induty, String innerFacility,
                               int maximum_people, int price) {
        for (int i = 0; i < siteCnt; ++i) {
            CampSite campSite = CampSite.builder()
                    .camp(camp)
                    .maximumPeople(maximum_people)
                    .price(price)
                    .siteType(induty)
                    .indoorFacility(innerFacility)
                    .build();

            campSiteRepository.save(campSite);
        }
    }

    @Transactional
    public void updateCampSite(Camp camp, Integer siteCnt,
                               Induty induty, String innerFacility,
                               int maximumPeople, int price) {
        // 특정 Camp와 Induty에 대한 모든 CampSite 조회
        List<CampSite> existingCampSites = campSiteRepository.findAllByCampAndSiteType(camp, induty);

        int existingCount = existingCampSites.size();

        if (existingCount < siteCnt) {
            //기존 보다 campSite 수가 많다면 부족한 campSite 추가
            for (int i = 0; i < siteCnt - existingCount; i++) {
                CampSite newCampSite = CampSite.builder()
                        .camp(camp)
                        .maximumPeople(maximumPeople)
                        .price(price)
                        .siteType(induty)
                        .indoorFacility(innerFacility)
                        .build();
                campSiteRepository.save(newCampSite);
            }
        } else if (existingCount > siteCnt) {
            // 초과됐다면 CampSite 삭제
            List<CampSite> toRemove = existingCampSites.subList(0, existingCount - siteCnt); //초과된 개수만큼 삭제
            campSiteRepository.deleteAll(toRemove);
        }

        // 나머지 CampSite 정보 업데이트
        for (CampSite campSite : existingCampSites) {
            campSite.updateCampSite(camp, maximumPeople, price, induty, innerFacility);
        }
    }

    //공공데이터 URI 작업 메서드
    public URI createUri(GoCampingPath goCampingPath, String... params)
            throws URISyntaxException {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
                        GO_CAMPING_END_POINT + goCampingPath.getPath())
                .queryParam("_type", CONTENT_TYPE)
                .queryParam("MobileOS", MOBILE_OS)
                .queryParam("MobileApp", MOBILE_APP)
                .queryParam("serviceKey", serviceKey);

        for (int i = 0; i < params.length; i += 2) {
            uriBuilder.queryParam(params[i], params[i + 1]);
        }

        return new URI(uriBuilder.build().toUriString());
    }
}
