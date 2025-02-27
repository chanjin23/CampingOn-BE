package site.campingon.campingon.common.public_data.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import site.campingon.campingon.camp.entity.*;
import site.campingon.campingon.camp.repository.*;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.common.public_data.GoCampingPath;
import site.campingon.campingon.common.public_data.dto.GoCampingDataDto;
import site.campingon.campingon.common.public_data.dto.GoCampingImageDto;
import site.campingon.campingon.common.public_data.dto.GoCampingImageParsedResponseDto;
import site.campingon.campingon.common.public_data.dto.GoCampingParsedResponseDto;
import site.campingon.campingon.common.public_data.mapper.GoCampingMapper;
import site.campingon.campingon.common.public_data.repository.GoCampingJdbcRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static site.campingon.campingon.camp.entity.Induty.*;
import static site.campingon.campingon.common.exception.ErrorCode.*;
import static site.campingon.campingon.common.public_data.repository.GoCampingUpsertSQLConstants.INSERT_MODE;
import static site.campingon.campingon.common.public_data.repository.GoCampingUpsertSQLConstants.UPDATE_MODE;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoCampingService {

    private final GoCampingMapper goCampingMapper;
    private final CampAddrRepository campAddrRepository;
    private final CampImageRepository campImageRepository;
    private final CampInfoRepository campInfoRepository;
    private final CampRepository campRepository;
    private final RestTemplate restTemplate;
    private final GoCampingProviderService goCampingProviderService;
    private final GoCampingJdbcRepository goCampingRepository;
    private static final String IMAGE_PAGE_NO = "1";    //이미지 몇번부터 값 꺼내올지

    //Camp 관련 엔티티 생성 및 DB 저장 메서드
    @Transactional
    public List<GoCampingParsedResponseDto> upsertCampData(GoCampingDataDto goCampingDataDto) {
        if (goCampingDataDto.getResponse() == null) return null; //값이 없다면 null

        List<GoCampingParsedResponseDto> goCampingParsedResponseDtoList = parseGoCampingData(goCampingDataDto);

        for (GoCampingParsedResponseDto data : goCampingParsedResponseDtoList) {
            Integer normalSiteCnt = data.getGnrlSiteCo();   //주요시설 일반야영장
            Integer carSiteCnt = data.getAutoSiteCo();  //주요시설 자동차야영장
            Integer glampSiteCnt = data.getGlampSiteCo();   //주요시설 글램핑
            Integer caravSiteCnt = data.getCaravSiteCo();   //주요시설 카라반
            Integer personalCaravanSiteCnt = data.getIndvdlCaravSiteCo();   //주요시설 개인 카라반
            String glampInnerFacility = data.getGlampInnerFclty();  //글램핑 - 내부시설
            String caravInnerFacility = data.getCaravInnerFclty();  //카라반 - 내부시설
            String pointWKT = String.format("POINT(%f %f)", data.getMapY(), data.getMapX());    //공간데이터

            //데이터에 주요시설이 단, 한개도 없는 경우 DB 생성하지않는다.
            if (checkNoFacility(data)) continue;

            Optional<Camp> findCamp = campRepository.findById(data.getContentId());
            if (findCamp.isPresent()) {
                //이미 캠프가 존재한다면 update
                Camp updateCamp = findCamp.orElseThrow(() -> new GlobalException(CAMP_NOT_FOUND_BY_ID))
                        .updateCamp(data);

                goCampingProviderService.upsertCampInduty(updateCamp, data);

                campAddrRepository.updateWithPoint(
                        updateCamp.getId(),
                        data.getDoNm(),
                        data.getSigunguNm(),
                        data.getZipcode(),
                        data.getAddr1(),
                        data.getAddr2(),
                        pointWKT
                );

                goCampingProviderService.updateCampSite(updateCamp, normalSiteCnt, NORMAL_SITE, null,
                        NORMAL_SITE.getMaximumPeople(), NORMAL_SITE.getPrice());

                goCampingProviderService.updateCampSite(updateCamp, carSiteCnt, CAR_SITE, null,
                        CAR_SITE.getMaximumPeople(), CAR_SITE.getPrice());

                goCampingProviderService.updateCampSite(updateCamp, glampSiteCnt, GLAMP_SITE, glampInnerFacility,
                        GLAMP_SITE.getMaximumPeople(), GLAMP_SITE.getPrice());

                goCampingProviderService.updateCampSite(updateCamp, caravSiteCnt, CARAV_SITE, caravInnerFacility,
                        CARAV_SITE.getMaximumPeople(), CARAV_SITE.getPrice());

                goCampingProviderService.updateCampSite(updateCamp, personalCaravanSiteCnt, PERSONAL_CARAV_SITE,
                        null, PERSONAL_CARAV_SITE.getMaximumPeople(), PERSONAL_CARAV_SITE.getPrice());
            } else {
                //그렇지 않다면 create
                Camp createCamp = buildCampFromData(data);

                campRepository.save(createCamp);

                campInfoRepository.save(CampInfo.builder().camp(createCamp).build());

                goCampingProviderService.upsertCampInduty(createCamp, data);

                campAddrRepository.saveWithPoint(
                        createCamp.getId(),
                        data.getDoNm(),
                        data.getSigunguNm(),
                        data.getZipcode(),
                        data.getAddr1(),
                        data.getAddr2(),
                        pointWKT
                );

                //캠핑지 DB 저장
                goCampingProviderService.createCampSite(createCamp, normalSiteCnt, NORMAL_SITE, null,
                        NORMAL_SITE.getMaximumPeople(), NORMAL_SITE.getPrice());

                goCampingProviderService.createCampSite(createCamp, carSiteCnt, CAR_SITE, null,
                        CAR_SITE.getMaximumPeople(), CAR_SITE.getPrice());

                goCampingProviderService.createCampSite(createCamp, glampSiteCnt, GLAMP_SITE, glampInnerFacility,
                        GLAMP_SITE.getMaximumPeople(), GLAMP_SITE.getPrice());

                goCampingProviderService.createCampSite(createCamp, caravSiteCnt, CARAV_SITE, caravInnerFacility,
                        CARAV_SITE.getMaximumPeople(), CARAV_SITE.getPrice());

                goCampingProviderService.createCampSite(createCamp, personalCaravanSiteCnt, PERSONAL_CARAV_SITE,
                        null, PERSONAL_CARAV_SITE.getMaximumPeople(), PERSONAL_CARAV_SITE.getPrice());
            }
        }
        return goCampingParsedResponseDtoList;
    }

    //CampImage 를 생성 및 DB 저장 메서드
    @Transactional
    public List<List<GoCampingImageParsedResponseDto>> upsertCampImageData(
            List<GoCampingImageDto> goCampingImageDto) {
        List<List<GoCampingImageParsedResponseDto>> goCampingImageParsedResponseDtoList = new ArrayList<>();

        for (GoCampingImageDto goCampingDataDto : goCampingImageDto) {
            if (goCampingDataDto.getResponse() == null) continue;  //image 데이터가 없다면 건너뛰기

            List<GoCampingImageDto.Item> item
                    = goCampingDataDto.getResponse().getBody().getItems().getItem();

            List<GoCampingImageParsedResponseDto> goCampingImageParsedResponseDto =
                    goCampingMapper.toGoCampingImageParsedResponseDtoList(item);

            Camp camp = campRepository.findById(
                            goCampingImageParsedResponseDto.getFirst().getContentId()
                    )
                    .orElseThrow(() -> new GlobalException(CAMP_NOT_FOUND_BY_ID));
            for (GoCampingImageParsedResponseDto data : goCampingImageParsedResponseDto) {
                CampImage campImage = CampImage.builder()
                        .id(data.getSerialnum())
                        .camp(camp)
                        .imageUrl(data.getImageUrl())
                        .build();

                campImageRepository.save(campImage);
            }
            goCampingImageParsedResponseDtoList.add(goCampingImageParsedResponseDto);
        }
        return goCampingImageParsedResponseDtoList;
    }

    @Transactional
    public int deleteCamp(GoCampingDataDto goCampingDataDto) {
        List<GoCampingParsedResponseDto> goCampingParsedResponseDtoList = parseGoCampingData(goCampingDataDto);

        List<Long> idsToDelete = goCampingParsedResponseDtoList.stream()
                .map(GoCampingParsedResponseDto::getContentId)
                .toList();

        return campRepository.deleteByIds(idsToDelete);
    }

    //공공데이터 전체 API 조회하고 dto 변환
    //메서드의 행위에 네이밍 짓기
    public GoCampingDataDto fetchCampData(
            GoCampingPath goCampingPath,
            String... params
    ) throws InvalidFormatException, URISyntaxException {
        URI uri = goCampingProviderService.createUri(goCampingPath, params);

        try {
            return restTemplate.getForObject(uri, GoCampingDataDto.class);  // API 호출
        } catch (HttpClientErrorException e) {
            log.error("HTTP 요청 오류: {}", e.getMessage(), e);
            throw new GlobalException(GO_CAMPING_BAD_REQUEST);  // 적절한 사용자 정의 예외 처리
        } catch (HttpServerErrorException e) {
            log.error("서버 오류: {}", e.getMessage(), e);
            throw new GlobalException(GO_CAMPING_SERVER_ERROR);  // 서버 오류에 대한 처리
        } catch (RestClientException e) {
            if (e.getCause() instanceof JsonMappingException) {
                log.error("JSON 매핑 오류: {}", e.getMessage(), e);
                throw new GlobalException(GO_CAMPING_DATA_MAPPING_ERROR);  // 데이터 매핑 오류
            }
            log.error("RestTemplate 오류: {}", e.getMessage(), e);
            throw new GlobalException(GO_CAMPING_NETWORK_ERROR);  // 네트워크 오류
        }
    }

    //공공데이터 이미지 API 조회하고 dto 변환(DB에 있는 모든 Camp 테이블의 이미지 조회)
    public List<GoCampingImageDto> fetchAllCampImageData(
            long imageCnt)
            throws URISyntaxException {
        List<GoCampingImageDto> goCampingDataDtoList = new ArrayList<>();

        List<Long> campIdList = campRepository.findAll().stream()
                .map(Camp::getId)
                .toList();

        for (Long campId : campIdList) {
            URI uri = goCampingProviderService.createUri(GoCampingPath.IMAGE_LIST,
                    "numOfRows", Long.toString(imageCnt),
                    "pageNo", IMAGE_PAGE_NO,  //몇번부터 시작할지
                    "contentId", campId.toString());

            try {
                goCampingDataDtoList.add(
                        restTemplate.getForObject(uri, GoCampingImageDto.class)); //API 호출
            } catch (Exception e) {
                goCampingDataDtoList.add(new GoCampingImageDto());  //데이터가 없을때 빈객체 주입
            }
        }
        return goCampingDataDtoList;
    }

    //공공데이터 이미지 API 조회 및 Dto 변환(campIdList 에 해당하는 id만 이미지 조회)
    public List<GoCampingImageDto> fetchCampImageData(
            List<Long> campIdList, long imageCnt)
            throws URISyntaxException {
        List<GoCampingImageDto> goCampingDataDtoList = new ArrayList<>();


        for (Long campId : campIdList) {
            URI uri = goCampingProviderService.createUri(GoCampingPath.IMAGE_LIST,
                    "numOfRows", Long.toString(imageCnt),
                    "pageNo", IMAGE_PAGE_NO,  //몇번부터 시작할지
                    "contentId", campId.toString());

            try {
                goCampingDataDtoList.add(
                        restTemplate.getForObject(uri, GoCampingImageDto.class)); //API 호출
            } catch (Exception e) {
                goCampingDataDtoList.add(new GoCampingImageDto());
            }
        }
        return goCampingDataDtoList;
    }

    private Camp buildCampFromData(GoCampingParsedResponseDto data) {
        return Camp.builder()
                .id(data.getContentId())
                .campName(data.getFacltNm())
                .lineIntro(data.getLineIntro())
                .intro(data.getIntro())
                .tel(data.getTel())
                .homepage(data.getHomepage())
                .outdoorFacility(data.getSbrsCl())
                .thumbImage(data.getFirstImageUrl())
                .animalAdmission(data.getAnimalCmgCl())
                .createdAt(
                        LocalDateTime.parse(
                                data.getCreatedtime()
                                , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
                .modifiedAt(LocalDateTime.parse(
                        data.getModifiedtime()
                        , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
                .build();
    }

    private boolean checkNoFacility(GoCampingParsedResponseDto data) {
        return data.getGnrlSiteCo() + data.getAutoSiteCo()
                + data.getGlampSiteCo() + data.getCaravSiteCo()
                + data.getIndvdlCaravSiteCo() == 0;
    }

    private List<GoCampingParsedResponseDto> parseGoCampingData(GoCampingDataDto goCampingDataDto) {
        List<GoCampingDataDto.Item> items = goCampingDataDto.getResponse().getBody().getItems().getItem();
        return goCampingMapper.toGoCampingParsedResponseDtoList(items);
    }

    public List<Long> findCampIdByCity(String city) {
        return campAddrRepository.findCampIdsByCity(city);
    }

    public void insertCampDataByJdbc(GoCampingDataDto goCampingDataDto) {
        List<GoCampingParsedResponseDto> goCampingParsedResponseDtoList = parseGoCampingData(goCampingDataDto);

        //1. 모든 DTO에서 content ID 추출
        List<Long> campIdList = goCampingParsedResponseDtoList.stream()
                .map(GoCampingParsedResponseDto::getContentId)
                .toList();

        //2. 한번의 쿼리로 이미 존재하는 contentId 조회
        List<Long> existingIds = campRepository.existData(campIdList);

        //3. 존재하는 ID와 존재하지 않는 ID 필터링
        List<GoCampingParsedResponseDto> notExistDataDtoDB = goCampingParsedResponseDtoList.stream()
                .filter(dto -> !existingIds.contains(dto.getContentId()))
                .toList();  //DB에 존재하지 않는 데이터

        List<GoCampingParsedResponseDto> existDataDtoFromDB = goCampingParsedResponseDtoList.stream()
                .filter(dto -> existingIds.contains(dto.getContentId()))
                .toList();  //DB에 이미 존재하는 데이터

        //4. 존재하지 않는 ID는 INSERT 하기

        if (!notExistDataDtoDB.isEmpty()) {
            goCampingRepository.bulkUpsertCamp(notExistDataDtoDB, INSERT_MODE);

            goCampingRepository.bulkUpsertCampInfo(notExistDataDtoDB, INSERT_MODE);

            goCampingRepository.bulkInsertCampInduty(notExistDataDtoDB);    //바로 insert

            goCampingRepository.bulkUpsertCampAddr(notExistDataDtoDB, INSERT_MODE);

            goCampingRepository.bulkUpsertCampSite(notExistDataDtoDB, INSERT_MODE);
        }

        //5. 존재하는 ID는 UPDATE 하기

        if (!existDataDtoFromDB.isEmpty()) {
            goCampingRepository.bulkUpsertCamp(existDataDtoFromDB, UPDATE_MODE);

            goCampingRepository.bulkUpsertCampInfo(existDataDtoFromDB, UPDATE_MODE);

            //데이터 삭제 후 다시 insert
            goCampingRepository.bulkDeleteCampInduty(existDataDtoFromDB);
            goCampingRepository.bulkInsertCampInduty(existDataDtoFromDB);

            goCampingRepository.bulkUpsertCampAddr(existDataDtoFromDB, UPDATE_MODE);

            goCampingRepository.bulkUpsertCampSite(existDataDtoFromDB, UPDATE_MODE);
        }
    }

    public void insertCampDataByJpa(GoCampingDataDto goCampingDataDto) {
        List<GoCampingParsedResponseDto> goCampingParsedResponseDtoList = parseGoCampingData(goCampingDataDto);
        List<Camp> list = goCampingParsedResponseDtoList.stream()
                .map(data -> Camp.builder()
                        .id(data.getContentId())
                        .campName(data.getFacltNm())
                        .lineIntro(data.getLineIntro())
                        .intro(data.getIntro())
                        .tel(data.getTel())
                        .homepage(data.getHomepage())
                        .outdoorFacility(data.getSbrsCl())
                        .thumbImage(data.getFirstImageUrl())
                        .animalAdmission(data.getAnimalCmgCl())
                        .createdAt(
                                LocalDateTime.parse(
                                        data.getCreatedtime()
                                        , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        )
                        .modifiedAt(LocalDateTime.parse(
                                data.getModifiedtime()
                                , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        )
                        .build())
                .toList();
        campRepository.saveAll(list);
    }

    public void insertCampDataBySave(GoCampingDataDto goCampingDataDto) {
        List<GoCampingParsedResponseDto> goCampingParsedResponseDtoList = parseGoCampingData(goCampingDataDto);

        for (GoCampingParsedResponseDto goCampingParsedResponseDto : goCampingParsedResponseDtoList) {
            Camp camp = buildCampFromData(goCampingParsedResponseDto);
            campRepository.save(camp);
        }
    }
}
