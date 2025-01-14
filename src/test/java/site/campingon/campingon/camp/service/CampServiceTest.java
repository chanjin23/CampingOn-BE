package site.campingon.campingon.camp.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.locationtech.jts.geom.Point;
import site.campingon.campingon.bookmark.repository.BookmarkRepository;
import site.campingon.campingon.camp.dto.*;
import site.campingon.campingon.camp.entity.*;
import site.campingon.campingon.camp.mapper.CampMapper;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.camp.repository.CampSiteRepository;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.user.repository.UserKeywordRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampServiceTest {

  @Mock
  private CampRepository campRepository;

  @Mock
  private CampSiteRepository campSiteRepository;

  @Mock
  private UserKeywordRepository userKeywordRepository;

  @Mock
  private BookmarkRepository bookMarkRepository;

  @Mock
  private CampMapper campMapper;


  @InjectMocks
  private CampService campService;


  private Camp mockCamp;
  private CampListResponseDto mockCampListDto;
  private CampDetailResponseDto mockCampDetailDto;
  private List<String> mockKeywords;
  private CampAddr mockCampAddr;
  private CampInfo mockCampInfo;
  private List<CampInduty> mockCampInduties;
  private CampInfoDto mockCampInfoDto;
  private CampAddrDto mockCampAddrDto;

  @BeforeEach
  void setUp() {
    GeometryFactory geometryFactory = new GeometryFactory();

    // WKT(Well-Known Text) 형식으로 포인트 생성
    Point point = null;
    try {
      String pointWKT = "POINT(127.0 37.0)";
      point = (Point) new WKTReader(geometryFactory).read(pointWKT);
    } catch (org.locationtech.jts.io.ParseException e) {
      throw new RuntimeException("Point 생성 중 오류 발생", e);
    }

    mockCampAddr = CampAddr.builder()
        .id(1L)
        .city("TestCity")
        .state("TestState")
        .zipcode("12345")
        .location(point)  // 생성된 JTS Point 객체 설정
        .streetAddr("Test Street")
        .build();

    mockCampInfo = CampInfo.builder()
        .id(1L)
        .recommendCnt(10)
        .bookmarkCnt(5)
        .build();

    mockCampInduties = Arrays.asList(
        CampInduty.builder()
            .id(1L)
            .induty(Induty.NORMAL_SITE)
            .build(),
        CampInduty.builder()
            .id(2L)
            .induty(Induty.CAR_SITE)
            .build()
    );

    mockCamp = Camp.builder()
        .id(1L)
        .campName("Test Camp")
        .tel("010-1234-5678")
        .lineIntro("Test Line Intro")
        .homepage("http://test.com")
        .outdoorFacility("Test Facility")
        .thumbImage("test.jpg")
        .induty(mockCampInduties)
        .campAddr(mockCampAddr)
        .campInfo(mockCampInfo)
        .build();

    mockCampAddrDto = CampAddrDto.builder()
        .city(mockCampAddr.getCity())
        .state(mockCampAddr.getState())
        .zipcode(mockCampAddr.getZipcode())
        .longitude(point.getX())
        .latitude(point.getY())
        .streetAddr(mockCampAddr.getStreetAddr())
        .detailedAddr(mockCampAddr.getDetailedAddr())
        .build();

    mockCampInfoDto = CampInfoDto.builder()
        .campInfoId(mockCampInfo.getId())
        .recommendCnt(mockCampInfo.getRecommendCnt())
        .bookmarkCnt(mockCampInfo.getBookmarkCnt())
        .build();

    mockCampListDto = CampListResponseDto.builder()
        .campId(1L)
        .name("Test Camp")
        .lineIntro("Test Line Intro")
        .thumbImage("test.jpg")
        .streetAddr("Test Address")
        .keywords(Arrays.asList("keyword1", "keyword2"))
        .isMarked(false)
        .build();

    mockCampDetailDto = CampDetailResponseDto.builder()
        .campId(1L)
        .name("Test Camp")
        .tel("010-1234-5678")
        .lineIntro("Test Line Intro")
        .homepage("http://test.com")
        .outdoorFacility("Test Facility")
        .indutys(Arrays.asList("일반야영장", "자동차야영장"))
        .campAddr(mockCampAddrDto)
        .images(Arrays.asList("image1.jpg", "image2.jpg"))
        .campInfo(mockCampInfoDto)
        .build();

    mockKeywords = Arrays.asList("keyword1", "keyword2");

  }

  @Test
  @DisplayName("로그인한 사용자의 인기 캠핑장 목록 조회 성공 테스트")
  void getPopularCamps_withUserId_success() {
    // given
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 9);
    List<Camp> camps = Arrays.asList(mockCamp);
    Page<Camp> campPage = new PageImpl<>(camps, pageable, camps.size());

    when(campRepository.findPopularCamps(pageable)).thenReturn(campPage);
    when(campMapper.toCampListDto(any(Camp.class))).thenReturn(mockCampListDto);
    when(bookMarkRepository.existsByCampIdAndUserId(anyLong(), anyLong())).thenReturn(false);

    // when
    Page<CampListResponseDto> result = campService.getPopularCamps(userId, pageable);

    // then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(mockCampListDto, result.getContent().get(0));

    assertEquals(mockCampListDto.getName(), result.getContent().get(0).getName());
    assertEquals(mockCampListDto.getLineIntro(), result.getContent().get(0).getLineIntro());
    assertEquals(mockCampListDto.isMarked(), result.getContent().get(0).isMarked());

    verify(campRepository).findPopularCamps(pageable);
    verify(campMapper).toCampListDto(any(Camp.class));
    verify(bookMarkRepository).existsByCampIdAndUserId(anyLong(), anyLong());
  }

  @Test
  @DisplayName("비로그인 사용자의 인기 캠핑장 목록 조회 성공 테스트")
  void getPopularCamps_withoutUserId_success() {
    // given
    Long userId = null;
    Pageable pageable = PageRequest.of(0, 9);
    List<Camp> camps = Arrays.asList(mockCamp);
    Page<Camp> campPage = new PageImpl<>(camps, pageable, camps.size());

    when(campRepository.findPopularCamps(pageable)).thenReturn(campPage);
    when(campMapper.toCampListDto(any(Camp.class))).thenReturn(mockCampListDto);

    // when
    Page<CampListResponseDto> result = campService.getPopularCamps(userId, pageable);

    // then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(mockCampListDto, result.getContent().get(0));

    verify(campRepository).findPopularCamps(pageable);
    verify(campMapper).toCampListDto(any(Camp.class));
    verify(bookMarkRepository, never()).existsByCampIdAndUserId(anyLong(), anyLong());
  }

  @Test
  @DisplayName("추천수가 있는 캠핑장이 없을 때 빈 목록 반환 확인 테스트")
  void getPopularCamps_noRecommendations_returnsEmptyList() {
    // given
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 9);
    Page<Camp> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

    when(campRepository.findPopularCamps(pageable)).thenReturn(emptyPage);

    // when
    Page<CampListResponseDto> result = campService.getPopularCamps(userId, pageable);

    // then
    assertTrue(result.isEmpty());
    assertEquals(0, result.getTotalElements());
    verify(campRepository).findPopularCamps(pageable);
  }


  @Test
  @DisplayName("캠핑장 상세 조회 성공 확인 테스트")
  void getCampDetail_success() {
    // given
    Long campId = 1L;

    when(campRepository.findById(campId)).thenReturn(Optional.of(mockCamp));
    when(campMapper.toCampDetailDto(mockCamp)).thenReturn(mockCampDetailDto);

    // when
    CampDetailResponseDto result = campService.getCampDetail(campId);

    // then
    assertNotNull(result);
    assertEquals(mockCampDetailDto.getCampId(), result.getCampId());
    assertEquals(mockCampDetailDto.getName(), result.getName());
    assertEquals("일반야영장, 자동차야영장", result.getIndutys());

    // CampAddr 관련 검증
    assertNotNull(result.getCampAddr());
    assertEquals(mockCampAddr.getCity(), result.getCampAddr().getCity());
    assertEquals(mockCampAddr.getState(), result.getCampAddr().getState());

    // CampInfo 관련 검증
    assertNotNull(result.getCampInfo());
    assertEquals(mockCampInfo.getRecommendCnt(), result.getCampInfo().getRecommendCnt());
    assertEquals(mockCampInfo.getBookmarkCnt(), result.getCampInfo().getBookmarkCnt());

    verify(campRepository).findById(campId);
    verify(campMapper).toCampDetailDto(mockCamp);
  }

  @Test
  @DisplayName("존재하지 않는 캠핑장 ID로 상세 조회 시 예외 발생 확인 테스트")
  void getCampDetail_invalidId_throwsException() {
    // given
    Long invalidCampId = 999L;
    when(campRepository.findById(invalidCampId)).thenReturn(Optional.empty());

    // when & then
    GlobalException exception = assertThrows(GlobalException.class, () ->
        campService.getCampDetail(invalidCampId)
    );
    assertEquals(ErrorCode.CAMP_NOT_FOUND_BY_ID.getMessage(), exception.getErrorCode().getMessage());
    verify(campRepository).findById(invalidCampId);
  }


  @Test
  @DisplayName("찜한 캠핑장 목록 조회 성공 확인 테스트")
  void getBookmarkedCamps_success() {
    // given
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 3);
    List<Camp> camps = Arrays.asList(mockCamp);
    Page<Camp> campPage = new PageImpl<>(camps, pageable, camps.size());

    when(campRepository.findByBookmarksUserId(userId, pageable))
        .thenReturn(campPage);
    when(campMapper.toCampListDto(any(Camp.class))).thenReturn(mockCampListDto);

    // when
    Page<CampListResponseDto> result = campService.getBookmarkedCamps(userId, pageable);

    // then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertTrue(result.getContent().get(0).isMarked());

    verify(campRepository).findByBookmarksUserId(userId, pageable);
    verify(campMapper).toCampListDto(any(Camp.class));
  }

  @Test
  @DisplayName("찜한 캠핑장이 없을 때 빈 목록 반환 확인 테스트")
  void getBookmarkedCamps_noBookmarks_returnsEmptyList() {
    // given
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 3);
    Page<Camp> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

    when(campRepository.findByBookmarksUserId(userId, pageable))
        .thenReturn(emptyPage);

    // when
    Page<CampListResponseDto> result = campService.getBookmarkedCamps(userId, pageable);

    // then
    assertTrue(result.isEmpty());
    assertEquals(0, result.getTotalElements());
    verify(campRepository).findByBookmarksUserId(userId, pageable);
  }

  // 캠핑장 관리자 테스트 CRUD
  @Test
  @DisplayName("관리자 캠핑장 생성 확인 테스트")
  void createCamp_success() {
    // given
    when(campRepository.save(any(Camp.class))).thenReturn(mockCamp);
    when(campMapper.toCampDetailDto(mockCamp)).thenReturn(mockCampDetailDto);

    // when
    CampDetailResponseDto result = campService.createCamp(mockCamp);

    // then
    assertNotNull(result);
    assertEquals(mockCampDetailDto.getCampId(), result.getCampId());
    assertEquals(mockCampDetailDto.getName(), result.getName());

    verify(campRepository).save(any(Camp.class));
    verify(campMapper).toCampDetailDto(mockCamp);
  }

  @Test
  @DisplayName("관리자 캠핑장 수정 확인 테스트")
  void updateCamp_success() {
    // given
    Long campId = 1L;
    Camp updatedCamp = Camp.builder()
        .id(campId)
        .campName("Updated Camp")
        .tel("010-9876-5432")
        .lineIntro("Updated Intro")
        .homepage("http://updated.com")
        .outdoorFacility("Updated Facility")
        .induty(mockCampInduties)
        .campAddr(mockCampAddr)
        .campInfo(mockCampInfo)
        .build();

    // 업데이트된 DetailResponseDto 설정
    CampDetailResponseDto updatedDto = CampDetailResponseDto.builder()
        .campId(1L)
        .name("Updated Camp")
        .tel("010-9876-5432")
        .lineIntro("Updated Intro")
        .homepage("http://updated.com")
        .outdoorFacility("Updated Facility")
        .indutys(Arrays.asList("일반야영장", "자동차야영장"))
        .campAddr(mockCampAddrDto)
        .images(Arrays.asList("updated1.jpg", "updated2.jpg"))
        .campInfo(mockCampInfoDto)
        .build();

    when(campRepository.findById(campId)).thenReturn(Optional.of(mockCamp));
    when(campRepository.save(any(Camp.class))).thenReturn(updatedCamp);
    when(campMapper.toCampDetailDto(updatedCamp)).thenReturn(updatedDto);

    // when
    CampDetailResponseDto result = campService.updateCamp(campId, updatedCamp);

    // then
    assertNotNull(result);
    assertEquals(updatedDto.getCampId(), result.getCampId());
    assertEquals(updatedDto.getName(), result.getName());
    assertEquals(updatedDto.getTel(), result.getTel());

    verify(campRepository).findById(campId);
    verify(campRepository).save(any(Camp.class));
    verify(campMapper).toCampDetailDto(any(Camp.class));
  }

  @Test
  @DisplayName("관리자 캠핑장 삭제 확인 테스트")
  void deleteCamp_success() {
    // given
    Long campId = 1L;

    // when
    campService.deleteCamp(campId);

    // then
    verify(campRepository).deleteById(campId);
  }

  @Test
  @DisplayName("관리자 캠핑장 목록 조회 확인 테스트")
  void getAllCamps_success() {
    // given
    List<Camp> camps = Arrays.asList(mockCamp);
    when(campRepository.findAll()).thenReturn(camps);
    when(campMapper.toCampListDto(any(Camp.class))).thenReturn(mockCampListDto);

    // when
    List<CampListResponseDto> result = campService.getAllCamps();

    // then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockCampListDto, result.get(0));

    verify(campRepository).findAll();
    verify(campMapper).toCampListDto(any(Camp.class));
  }
}