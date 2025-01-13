package site.campingon.campingon.common.public_data.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import site.campingon.campingon.camp.repository.*;
import site.campingon.campingon.common.public_data.GoCampingPath;
import site.campingon.campingon.common.public_data.PublicDataConstants;
import site.campingon.campingon.common.public_data.dto.GoCampingDataDto;
import site.campingon.campingon.common.public_data.mapper.GoCampingMapper;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


//todo test 코드 추후작성...
@ExtendWith(MockitoExtension.class) //InjectMocks, Mock 어노테이션 감지
class GoCampingServiceTest {

    @InjectMocks
    private GoCampingService goCampingService;

    @Mock
    private GoCampingMapper goCampingMapper;

    @Mock
    private GoCampingProviderService goCampingProviderService;

    @Mock
    private CampAddrRepository campAddrRepository;

    @Mock
    private CampImageRepository campImageRepository;

    @Mock
    private CampRepository campRepository;

    @Mock
    private CampSiteRepository campSiteRepository;

    @Mock
    private CampIndutyRepository campIndutyRepository;

    @Mock
    private RestTemplate restTemplate;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("공공데이터 전체 API 조회 테스트 : 성공")
    void testGetAndConvertToGoCampingDataDto_Success() throws Exception {
        //given
        String numOfRows = "1";
        String pageNo = "1";

        URI mockUri = new URI("http://example.com"); // 반환할 실제 URI 객체 생성

        // Item 객체 생성
        GoCampingDataDto.Item item = GoCampingDataDto.Item.builder()
                .contentId(123L).facltNm("Sample Camping Site")
                .lineIntro("Beautiful camping site with scenic views")
                .intro("Enjoy a perfect getaway with all amenities.")
                .doNm("Seoul").sigunguNm("Gangnam-gu").zipcode("12345")
                .addr1("123 Camping Rd").addr2("Apt 456").tel("123-456-7890")
                .homepage("http://samplecamping.com").gnrlSiteCo(10).autoSiteCo(5)
                .glampSiteCo(3).caravSiteCo(2).indvdlCaravSiteCo(1)
                .glampInnerFclty("Glamping facilities available")
                .caravInnerFclty("Caravan facilities available")
                .sbrsCl("Shower, BBQ Area").animalCmgCl("가능")
                .firstImageUrl("http://imageurl.com/sample.jpg")
                .mapX(37.5665).mapY(126.9780)
                .build();

        // GoCampingDataDto 객체 생성
        GoCampingDataDto goCampingDataDto = GoCampingDataDto.builder()
                .response(GoCampingDataDto.Response.builder()
                        .body(
                                GoCampingDataDto.Body.builder()
                                        .items(
                                                GoCampingDataDto.Items.builder()
                                                        .item(List.of(item)) // Item 리스트 추가
                                                        .build()
                                        )
                                        .build()
                        )
                        .build()
                )
                .build();
        System.out.println("goCampingDataDto :" + goCampingDataDto);
        when(goCampingProviderService.createUri(any(GoCampingPath.class),anyString(), anyString(), anyString(), anyString())).thenReturn(mockUri);

        doReturn(goCampingDataDto).when(restTemplate).getForObject(eq(mockUri), eq(GoCampingDataDto.class));

        GoCampingDataDto result = goCampingService.fetchCampData(GoCampingPath.BASED_LIST, "numOfRows", numOfRows, "pageNo", pageNo);

        // then
        assertThat(result).isNotNull();

        GoCampingDataDto.Item resultItem = result.getResponse().getBody().getItems().getItem().getFirst();
        assertThat(resultItem.getContentId()).isEqualTo(123L);
        assertThat(resultItem.getFacltNm()).isEqualTo("Sample Camping Site");
        assertThat(resultItem.getLineIntro()).isEqualTo("Beautiful camping site with scenic views");
        assertThat(resultItem.getDoNm()).isEqualTo("Seoul");
        assertThat(resultItem.getSigunguNm()).isEqualTo("Gangnam-gu");
        assertThat(resultItem.getZipcode()).isEqualTo("12345");
        assertThat(resultItem.getFirstImageUrl()).isEqualTo("http://imageurl.com/sample.jpg");
    }

//    @Test
//    @DisplayName("공공데이터 이미지 API 조회 : 성공")
//    void testGetAndConvertToGoCampingImageDataDto_Success() throws Exception {
//        //given
//        List<Camp> mockCamps = List.of(
//                Camp.builder().id(1L).build(),
//                Camp.builder().id(2L).build(),
//                Camp.builder().id(3L).build()
//        );
//        Long imageCnt = 10L;
//        GoCampingImageDto mockImageDto = new GoCampingImageDto();
//
//        //when
//        when(campRepository.findAll()).thenReturn(mockCamps);  // 필요에 따라 값 설정
//
//        List<Long> list = campRepository.findAll().stream()
//                .map(Camp::getId)
//                .toList();
//        System.out.println("Mock Camps: " + list.size());  // 이 부분을 출력하여 확인
//
//        doReturn(mockImageDto)
//                .when(restTemplate).getForObject(any(URI.class), eq(GoCampingImageDto.class));
//
//        List<GoCampingImageDto> result
//                = goCampingService.getAndConvertToGoCampingImageDataDto(imageCnt);
//
//        //then
//        assertThat(result).isNotNull();
//        assertThat(result).hasSize(3);  // 3개 이미지 api 조회
//    }

    @Test
    @DisplayName("공공데이터 URI 생성: 성공")
    void testCreateUri_Success() throws Exception {
        // Given
        GoCampingPath goCampingPath = GoCampingPath.BASED_LIST;  // 예시 GoCampingPath (enum 또는 상수)
        String param1Key = "numOfRows";
        String param1Value = "1";
        String param2Key = "pageNo";
        String param2Value = "1";

        // When
        URI uri = goCampingProviderService.createUri(goCampingPath, param1Key, param1Value, param2Key, param2Value);

        // Then
        String expectedUriString = PublicDataConstants.GO_CAMPING_END_POINT + goCampingPath.getPath() +
                "?_type=json" +
                "&MobileOS=ETC" +
                "&MobileApp=Camping_On" +
                "&serviceKey" +
                "&numOfRows=1" +
                "&pageNo=1";

        assertThat(uri.toString()).isEqualTo(expectedUriString);
    }

//    @Test
//    @DisplayName("Camp 관련 엔티티 생성 및 DB 저장 : 성공")
//    void testCreateCampByGoCampingData_Success() throws Exception {
//        // given
//        GoCampingDataDto.Item item = GoCampingDataDto.Item.builder()
//                .contentId(1L)
//                .facltNm("Mock Camp")
//                .lineIntro("A beautiful camping site.")
//                .intro("This is a detailed intro about the camp.")
//                .doNm("Mock Province")
//                .sigunguNm("Mock City")
//                .zipcode("12345")
//                .addr1("123 Mock Street")
//                .addr2("Building 5")
//                .tel("123-456-7890")
//                .homepage("https://mockcamp.com")
//                .gnrlSiteCo(10)
//                .autoSiteCo(5)
//                .glampSiteCo(2)
//                .caravSiteCo(3)
//                .indvdlCaravSiteCo(1)
//                .glampInnerFclty("Tent, Bed")
//                .caravInnerFclty("Kitchen, Bathroom")
//                .sbrsCl("Restroom, Shower")
//                .animalCmgCl("불가능")
//                .firstImageUrl("https://mockcamp.com/image.jpg")
//                .mapX(37.5665)
//                .mapY(126.9780)
//                .createdtime("2024-01-01 12:00:00")
//                .modifiedtime("2024-01-10 12:00:00")
//                .build();
//        GoCampingDataDto goCampingDataDto = GoCampingDataDto.builder()
//                .response(GoCampingDataDto.Response.builder()
//                        .body(GoCampingDataDto.Body.builder()
//                                .items(GoCampingDataDto.Items.builder()
//                                        .item(List.of(item))
//                                        .build())
//                                .build())
//                        .build())
//                .build();
//        List<GoCampingParsedResponseDto> expectedResult = new ArrayList<>();  // 기대되는 결과값
//
//        // mock 반환 값 설정
//        when(goCampingMapper.toGoCampingParsedResponseDtoList(anyList()))
//                .thenReturn(expectedResult);  // 데이터 매핑을 mock 처리
//
//        // Camp, CampAddr, CampSite 엔티티 mock 설정
//        Camp mockCamp = Camp.builder()
//                .id(1L)
//                .campName("Mock Camp")
//                .build();
//
//        CampAddr mockCampAddr = CampAddr.builder()
//                .camp(mockCamp)
//                .city("Mock City")
//                .state("Mock State")
//                .build();
//
//        // campRepository, campAddrRepository mock 설정
//        when(campRepository.save(any(Camp.class))).thenReturn(mockCamp);
//        when(campAddrRepository.save(any(CampAddr.class))).thenReturn(mockCampAddr);
//
//        // when
//        List<GoCampingParsedResponseDto> result = goCampingService.createCampByGoCampingData(goCampingDataDto);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result).hasSize(1);  // 예상되는 parsedResponseDto 리스트 크기
//        verify(campRepository, times(1)).save(any(Camp.class));  // campRepository의 save 메서드가 한 번 호출되었는지 검증
//        verify(campAddrRepository, times(1)).save(any(CampAddr.class));  // campAddrRepository의 save 메서드가 한 번 호출되었는지 검증
//        verify(campSiteRepository, times(5)).save(any(CampSite.class));  // createCampSite에서 각 캠프 사이트가 5번 저장되는지 검증 (예시로 5번 호출)
//
//    }

}
