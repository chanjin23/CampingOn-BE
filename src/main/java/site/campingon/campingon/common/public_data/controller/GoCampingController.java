package site.campingon.campingon.common.public_data.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.common.public_data.GoCampingPath;
import site.campingon.campingon.common.public_data.dto.GoCampingDataDto;
import site.campingon.campingon.common.public_data.dto.GoCampingImageDto;
import site.campingon.campingon.common.public_data.dto.GoCampingImageParsedResponseDto;
import site.campingon.campingon.common.public_data.dto.GoCampingParsedResponseDto;
import site.campingon.campingon.common.public_data.service.GoCampingService;

import java.net.URISyntaxException;
import java.util.List;

import static site.campingon.campingon.common.exception.ErrorCode.*;

/**
 * https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15101933
 * 공공데이터 고캠핑 정보 기반으로 캠프관련 엔티티 생성
 */
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
@Slf4j
public class GoCampingController {

    private final GoCampingService goCampingService;

    /**
     * 고캠핑데이터 DB 저장
     */
    @PostMapping("/basedList")
    public ResponseEntity<List<GoCampingParsedResponseDto>> createCampByGoCampingBasedList(
            @RequestParam("numOfRows") Long numOfRows,  //한 페이지 결과 수
            @RequestParam("pageNo") Long pageNo)    //현재 페이지 번호
    {
        try {
            //공공데이터를 조회하고 반환
            GoCampingDataDto goCampingDataDto = goCampingService.fetchCampData(
                    GoCampingPath.BASED_LIST,
                    "numOfRows", numOfRows.toString(),
                    "pageNo", pageNo.toString());

            //Camp 관련 엔티티를 생성하고 DB에 저장한다.
            List<GoCampingParsedResponseDto> goCampingParsedResponseDtos
                    = goCampingService.upsertCampData(goCampingDataDto);

            return ResponseEntity.status(HttpStatus.OK).body(goCampingParsedResponseDtos);
        } catch (InvalidFormatException e) { //
            log.error("고캠핑데이터 저장 실패, 고캠핑 API 파라미터나 서비스키를 다시 확인해주세요");
            throw new GlobalException(GO_CAMPING_BAD_REQUEST);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * DB에 Camp Id를 가져와서 Id가 가지고있는 이미지를 호출 및 저장
     */
    @PostMapping("/imageList")
    public ResponseEntity<List<List<GoCampingImageParsedResponseDto>>> createCampImageByGoCampingImageList(
            @RequestParam("imageCnt") long imageCnt)    //몇개의 이미지개수를 갖고올지
            throws URISyntaxException {

        //공공데이터를 조회하고 dto로 변환
        List<GoCampingImageDto> goCampingImageDto = goCampingService.fetchAllCampImageData(imageCnt);

        //CampImage 를 생성하고 DB에 저장한다.
        List<List<GoCampingImageParsedResponseDto>> goCampingParsedResponseDtos
                = goCampingService.upsertCampImageData(goCampingImageDto);

        if (goCampingParsedResponseDtos == null) {
            throw new GlobalException(GO_CAMPING_DATA_NO_CONTENT);
        }

        return ResponseEntity.status(HttpStatus.OK).body(goCampingParsedResponseDtos);
    }

    @PostMapping("/imageList/city")
    public ResponseEntity<List<List<GoCampingImageParsedResponseDto>>> createCampImageByCityKeyword(
            @RequestParam("imageCnt") long imageCnt,
            @RequestParam("city") String city
    ) throws URISyntaxException {
        List<Long> campIdList=goCampingService.findCampIdByCity(city);

        List<GoCampingImageDto> goCampingImageDtos = goCampingService.fetchCampImageData(campIdList, imageCnt);

        List<List<GoCampingImageParsedResponseDto>> campImageData
                = goCampingService.upsertCampImageData(goCampingImageDtos);

        return ResponseEntity.status(HttpStatus.OK).body(campImageData);
    }

    @PostMapping("/jdbc")
    public ResponseEntity<?> createCampByGoCampingBasedListByJdbc(
            @RequestParam("numOfRows") Long numOfRows,  //한 페이지 결과 수
            @RequestParam("pageNo") Long pageNo)    //현재 페이지 번호
    {
        try {
            //공공데이터를 조회하고 반환
            GoCampingDataDto goCampingDataDto = goCampingService.fetchCampData(
                    GoCampingPath.BASED_LIST,
                    "numOfRows", numOfRows.toString(),
                    "pageNo", pageNo.toString());

            //Camp 관련 엔티티를 생성하고 DB에 저장한다.
            goCampingService.insertCampDataByJdbc(goCampingDataDto);

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (InvalidFormatException e) { //
            log.error("고캠핑데이터 저장 실패, 고캠핑 API 파라미터나 서비스키를 다시 확인해주세요");
            throw new GlobalException(GO_CAMPING_BAD_REQUEST);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/saveAll")
    public ResponseEntity<?> createCampByGoCampingBasedListByJpa(
            @RequestParam("numOfRows") Long numOfRows,  //한 페이지 결과 수
            @RequestParam("pageNo") Long pageNo)    //현재 페이지 번호
    {
        try {
            //공공데이터를 조회하고 반환
            GoCampingDataDto goCampingDataDto = goCampingService.fetchCampData(
                    GoCampingPath.BASED_LIST,
                    "numOfRows", numOfRows.toString(),
                    "pageNo", pageNo.toString());

            //Camp 관련 엔티티를 생성하고 DB에 저장한다.
            goCampingService.insertCampDataByJpa(goCampingDataDto);

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (InvalidFormatException e) { //
            log.error("고캠핑데이터 저장 실패, 고캠핑 API 파라미터나 서비스키를 다시 확인해주세요");
            throw new GlobalException(GO_CAMPING_BAD_REQUEST);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/save")
    public ResponseEntity<?> createCampByGoCampingBasedListSave(
            @RequestParam("numOfRows") Long numOfRows,  //한 페이지 결과 수
            @RequestParam("pageNo") Long pageNo)    //현재 페이지 번호
    {
        try {
            //공공데이터를 조회하고 반환
            GoCampingDataDto goCampingDataDto = goCampingService.fetchCampData(
                    GoCampingPath.BASED_LIST,
                    "numOfRows", numOfRows.toString(),
                    "pageNo", pageNo.toString());

            //Camp 관련 엔티티를 생성하고 DB에 저장한다.
            goCampingService.insertCampDataBySave(goCampingDataDto);

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (InvalidFormatException e) { //
            log.error("고캠핑데이터 저장 실패, 고캠핑 API 파라미터나 서비스키를 다시 확인해주세요");
            throw new GlobalException(GO_CAMPING_BAD_REQUEST);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

//    //위치기반정보 목록 조회
//    @GetMapping("/locationBasedList")
//    public ResponseEntity<List<GoCampingParsedResponseDto>> GetGoCampingLocationBasedList(
//            @RequestParam("numOfRows") Long numOfRows,
//            @RequestParam("pageNo") Long pageNo,
//            @RequestParam("mapX") String mapX,
//            @RequestParam("mapY") String mapY,
//            @RequestParam("radius") String radius)
//            throws URISyntaxException {
//        GoCampingDataDto goCampingDataDto = goCampingService.goCampingDataDtoByGoCampingUrl(
//                GoCampingPath.LOCATION_BASED_LIST,
//                "numOfRows", numOfRows.toString(),
//                "pageNo", pageNo.toString(),
//                "mapX", mapX, "mapY", mapY, "radius", radius);
//
//        List<GoCampingParsedResponseDto> goCampingParsedResponseDtos
//                = goCampingService.createCampByGoCampingData(goCampingDataDto);
//
//        return ResponseEntity.status(HttpStatus.OK).body(goCampingParsedResponseDtos);
//    }

//    //키워드 검색 목록 조회
//    @GetMapping("/searchList")
//    public ResponseEntity<List<GoCampingParsedResponseDto>> GetGoCampingKeywordList(
//            @RequestParam("numOfRows") Long numOfRows,
//            @RequestParam("pageNo") Long pageNo,
//            @RequestParam("keyword") String keyword)
//            throws URISyntaxException {
//        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8); //한글 인코딩
//        GoCampingDataDto goCampingDataDto = goCampingService.goCampingDataDtoByGoCampingUrl(
//                GoCampingPath.SEARCH_LIST,
//                "numOfRows", numOfRows.toString(),
//                "pageNo", pageNo.toString(),
//                "keyword", encodedKeyword);
//
//        List<GoCampingParsedResponseDto> goCampingParsedResponseDtos
//                = goCampingService.createCampByGoCampingData(goCampingDataDto);
//
//        return ResponseEntity.status(HttpStatus.OK).body(goCampingParsedResponseDtos);
//    }

//    //동기화 목록 조회
//    @GetMapping("/basedSyncList")
//    public ResponseEntity<List<GoCampingParsedResponseDto>> GetGoCampingBasedSyncList(
//            @RequestParam("numOfRows") Long numOfRows,
//            @RequestParam("pageNo") Long pageNo,
//            @RequestParam("syncStatus") String syncStatus)
//            throws URISyntaxException {
//        GoCampingDataDto goCampingDataDto = goCampingService.goCampingDataDtoByGoCampingUrl(
//                GoCampingPath.BASED_SYNC_LIST,
//                "numOfRows", numOfRows.toString(),
//                "pageNo", pageNo.toString(),
//                "syncStatus", syncStatus);
//
//        List<GoCampingParsedResponseDto> goCampingParsedResponseDtos = goCampingService.createCampByGoCampingData(goCampingDataDto);
//
//        return ResponseEntity.status(HttpStatus.OK).body(goCampingParsedResponseDtos);
//    }
}
