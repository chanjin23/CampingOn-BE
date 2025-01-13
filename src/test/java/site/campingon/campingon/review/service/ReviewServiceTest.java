package site.campingon.campingon.review.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.reservation.entity.ReservationStatus;
import site.campingon.campingon.reservation.repository.ReservationRepository;
import site.campingon.campingon.review.dto.ReviewCreateRequestDto;
import site.campingon.campingon.review.dto.ReviewResponseDto;
import site.campingon.campingon.review.dto.ReviewUpdateRequestDto;
import site.campingon.campingon.review.entity.Review;
import site.campingon.campingon.review.entity.ReviewImage;
import site.campingon.campingon.review.mapper.ReviewImageMapper;
import site.campingon.campingon.review.mapper.ReviewMapper;
import site.campingon.campingon.review.repository.ReviewImageRepository;
import site.campingon.campingon.review.repository.ReviewRepository;
import site.campingon.campingon.common.s3bucket.service.S3BucketService;
import site.campingon.campingon.user.entity.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static site.campingon.campingon.camp.entity.Induty.CAR_SITE;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private CampRepository campRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private S3BucketService s3BucketService;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private ReviewImageMapper reviewImageMapper;

    @InjectMocks
    private ReviewService reviewService;

    private Camp mockCamp;
    private Reservation mockReservation;
    private Review mockReview;

    @BeforeEach
    void setUp() {
        mockCamp = Camp.builder()
                .id(1L)
                .campName("Mock Camp")
                .build();

        mockReservation = Reservation.builder()
                .id(1L)
                .campSite(CampSite.builder()
                        .siteType(CAR_SITE)
                        .price(CAR_SITE.getPrice())
                        .maximumPeople(CAR_SITE.getMaximumPeople())
                        .build())
                .user(User.builder()
                        .id(123L)
                        .name("Mock User")
                        .email("mockuser@example.com")
                        .build())
                .checkin(LocalDateTime.of(2024, 12, 1, 14, 0))
                .checkout(LocalDateTime.of(2024, 12, 3, 11, 0))
                .guestCnt(2)
                .status(ReservationStatus.COMPLETED)
                .totalPrice(160000)
                .build();

        mockReview = Review.builder()
                .content("Great camping experience!")
                .isRecommend(true)
                .camp(mockCamp)
                .reservation(mockReservation)
                .build();
    }

    @Test
    @DisplayName("리뷰 생성 성공")
    void createReview_Success() throws IOException {
        // Given
        ReviewCreateRequestDto requestDto = createMockReviewRequest();
        Review savedReview = createSavedReview(mockReview);
        List<String> uploadedUrls = List.of("url1", "url2");
        List<ReviewImage> reviewImages = createMockReviewImages(savedReview, uploadedUrls);

        mockCampAndReservationFindById();

        when(reviewMapper.toEntity(requestDto, mockCamp, mockReservation)).thenReturn(mockReview);
        when(reviewRepository.save(mockReview)).thenReturn(savedReview);
        when(s3BucketService.upload(requestDto.getS3Images(), "reviews/1")).thenReturn(uploadedUrls);
        when(reviewImageMapper.toEntityList(uploadedUrls, savedReview)).thenReturn(reviewImages);
        when(reviewImageRepository.saveAll(reviewImages)).thenReturn(reviewImages);
        when(reviewMapper.toResponseDto(savedReview)).thenReturn(
                ReviewResponseDto.builder()
                        .reviewId(savedReview.getId())
                        .content(savedReview.getContent())
                        .recommended(savedReview.isRecommend())
                        .build()
        );

        // When
        ReviewResponseDto responseDto = reviewService.createReview(1L, 1L, requestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(savedReview.getId(), responseDto.getReviewId());
        assertEquals(requestDto.getContent(), responseDto.getContent());
        assertEquals(requestDto.isRecommended(), responseDto.isRecommended());

        verify(reviewRepository, times(1)).save(mockReview);
        verify(reviewImageRepository, times(1)).saveAll(reviewImages);
        verify(s3BucketService, times(1)).upload(requestDto.getS3Images(), "reviews/1");
    }

//    @Test
//    @DisplayName("리뷰 수정 성공")
//    void updateReview_Success() throws IOException {
//        // Given
//        ReviewUpdateRequestDto requestDto = createMockUpdateRequest();
//        Review existingReview = createSavedReview(mockReview);
//        List<ReviewImage> oldReviewImages = List.of(
//                ReviewImage.builder().imageUrl("old_url1").build(),
//                ReviewImage.builder().imageUrl("old_url2").build()
//        );
//
//        when(reviewRepository.findById(1L)).thenReturn(Optional.of(existingReview));
//        when(reviewImageRepository.findByReview(existingReview)).thenReturn(oldReviewImages);
//
//        // When
//        reviewService.updateReview(1L, 1L, requestDto);
//
//        // Then
//        verify(reviewImageRepository).deleteAll(oldReviewImages);
//        verify(s3BucketService).upload(requestDto.getS3Images(), "reviews/1");
//    }

    // Helper Methods
    private void mockCampAndReservationFindById() {
        when(campRepository.findById(1L)).thenReturn(Optional.of(mockCamp));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(mockReservation));
    }

    private ReviewCreateRequestDto createMockReviewRequest() {
        return ReviewCreateRequestDto.builder()
                .content("Great camping experience!")
                .recommended(true)
                .s3Images(List.of(
                        new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "dummy image content".getBytes()),
                        new MockMultipartFile("image2", "image2.jpg", "image/jpeg", "dummy image content".getBytes())
                ))
                .build();
    }

    private ReviewUpdateRequestDto createMockUpdateRequest() {
        return ReviewUpdateRequestDto.builder()
                .content("Updated Content")
                .isRecommend(false)
                .s3Images(List.of(
                        new MockMultipartFile("image1", "updated_image1.jpg", "image/jpeg", "updated dummy image content".getBytes()),
                        new MockMultipartFile("image2", "updated_image2.jpg", "image/jpeg", "updated dummy image content".getBytes())
                ))
                .build();
    }

    private Review createSavedReview(Review baseReview) {
        return Review.builder()
                .id(1L)
                .content(baseReview.getContent())
                .isRecommend(baseReview.isRecommend())
                .camp(baseReview.getCamp())
                .reservation(baseReview.getReservation())
                .build();
    }

    private List<ReviewImage> createMockReviewImages(Review review, List<String> urls) {
        return List.of(
                ReviewImage.builder().imageUrl(urls.get(0)).review(review).build(),
                ReviewImage.builder().imageUrl(urls.get(1)).review(review).build()
        );
    }

//    @Test
//    @DisplayName("캠핑장의 리뷰 조회 성공")
//    void getReviewsByCampId_Success() {
//        // Given
//        Long campId = 1L; // 캠프 ID
//        Camp mockCamp = Camp.builder().id(campId).campName("Mock Camp").build();
//
//        List<Review> mockReviews = List.of(
//                Review.builder().id(1L).content("Great camping experience!").camp(mockCamp).isRecommend(true).build(),
//                Review.builder().id(2L).content("Could be better.").camp(mockCamp).isRecommend(false).build()
//        );
//
//        List<ReviewResponseDto> expectedResponseDtos = List.of(
//                ReviewResponseDto.builder().reviewId(1L).content("Great camping experience!").isRecommend(true).build(),
//                ReviewResponseDto.builder().reviewId(2L).content("Could be better.").isRecommend(false).build()
//        );
//
//        // Mock 설정
//        when(campRepository.findById(campId)).thenReturn(Optional.of(mockCamp));
//        when(reviewRepository.findByCampId(campId)).thenReturn(mockReviews);
//        when(reviewMapper.toResponseDtoList(mockReviews)).thenReturn(expectedResponseDtos);
//
//        // When
//        List<ReviewResponseDto> responseDtos = reviewService.getReviewsByCampId(campId);
//
//        // Then
//        assertNotNull(responseDtos);
//        assertEquals(2, responseDtos.size());
//        assertEquals("Great camping experience!", responseDtos.get(0).getContent());
//        assertEquals("Could be better.", responseDtos.get(1).getContent());
//
//        // Mock 호출 검증
//        verify(campRepository, times(1)).findById(campId);
//        verify(reviewRepository, times(1)).findByCampId(campId);
//        verify(reviewMapper, times(1)).toResponseDtoList(mockReviews);
//    }


//    @Test
//    @DisplayName("캠핑지의 리뷰 조회 성공")
//    void getReviewsByCampSiteId_Success() {
//        // Given
//        Long campSiteId = 1L;
//        CampSite mockCampSite = CampSite.builder().id(campSiteId).siteType(CAR_SITE).build();
//        List<Review> mockReviews = List.of(
//                Review.builder().id(1L).content("Amazing site!").campSite(mockCampSite).isRecommend(true).build(),
//                Review.builder().id(2L).content("Not bad.").campSite(mockCampSite).isRecommend(false).build()
//        );
//        List<ReviewResponseDto> expectedResponseDtos = List.of(
//                ReviewResponseDto.builder().reviewId(1L).content("Amazing site!").isRecommend(true).build(),
//                ReviewResponseDto.builder().reviewId(2L).content("Not bad.").isRecommend(false).build()
//        );
//
//        when(reviewRepository.findByCampSiteId(campSiteId)).thenReturn(mockReviews);
//        when(reviewMapper.toResponseDtoList(mockReviews)).thenReturn(expectedResponseDtos);
//
//        // When
//        List<ReviewResponseDto> responseDtos = reviewService.getReviewsByCampSiteId(campSiteId);
//
//        // Then
//        assertNotNull(responseDtos);
//        assertEquals(2, responseDtos.size());
//        assertEquals("Amazing site!", responseDtos.get(0).getContent());
//        assertEquals("Not bad.", responseDtos.get(1).getContent());
//
//        verify(reviewRepository, times(1)).findByCampSiteId(campSiteId);
//        verify(reviewMapper, times(1)).toResponseDtoList(mockReviews);
//    }


//    @Test
//    @DisplayName("리뷰 삭제 성공")
//    void deleteReview_Success() {
//        // Given
//        Long reviewId = 1L;
//        Review existingReview = createSavedReview(mockReview);
//        List<ReviewImage> reviewImages = List.of(
//                ReviewImage.builder().imageUrl("url1").build(),
//                ReviewImage.builder().imageUrl("url2").build()
//        );
//
//        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
//        when(reviewImageRepository.findByReview(existingReview)).thenReturn(reviewImages);
//
//        // When
//        reviewService.deleteReview(reviewId);
//
//        // Then
//        verify(reviewImageRepository, times(1)).findByReview(existingReview);
//        verify(reviewImageRepository, times(1)).deleteAll(reviewImages);
//        verify(s3BucketService, times(1)).remove(reviewImages.get(0).getImageUrl());
//        verify(s3BucketService, times(1)).remove(reviewImages.get(1).getImageUrl());
//        verify(reviewRepository, times(1)).delete(existingReview);
//    }
//
//    @Test
//    @DisplayName("리뷰 추천 상태 변경 - 권한 있는 유저일 때 성공적으로 추천 상태 변경")
//    void toggleRecommend_ShouldToggleRecommendStatus_WhenUserIsAuthorized() {
//        // Given
//        Long reviewId = 1L;
//        Long userId = 2L;
//
//        // Mocking Review 객체 생성
//        Review review = Review.builder()
//                .id(reviewId)
//                .isRecommend(false)
//                .reservation(Reservation.builder()
//                        .user(User.builder().id(userId).build())
//                        .build())
//                .build();
//
//        // 상태가 변경된 Review 객체
//        Review updatedReview = Review.builder()
//                .id(reviewId)
//                .isRecommend(true)
//                .reservation(review.getReservation())
//                .build();
//
//        // Mocking
//        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
//        given(reviewMapper.toUpdatedReview(review)).willReturn(updatedReview);
//        given(reviewRepository.save(updatedReview)).willReturn(updatedReview);
//
//        // When
//        boolean isRecommended = reviewService.toggleRecommend(reviewId, userId);
//
//        // Then
//        assertThat(isRecommended).isTrue();
//        verify(reviewRepository).findById(reviewId);
//        verify(reviewMapper).toUpdatedReview(review);
//        verify(reviewRepository).save(updatedReview);
//    }
//
//    @Test
//    @DisplayName("리뷰 추천 상태 변경 - 리뷰를 찾을 수 없을 때 예외 발생")
//    void toggleRecommend_ShouldThrowException_WhenReviewNotFound() {
//        // Given
//        Long reviewId = 1L;
//        Long userId = 2L;
//
//        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());
//
//        // When & Then
//        assertThatThrownBy(() -> reviewService.toggleRecommend(reviewId, userId))
//                .isInstanceOf(GlobalException.class)
//                .satisfies(exception -> {
//                    GlobalException globalException = (GlobalException) exception;
//                    assertThat(globalException.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND_BY_ID);
//                });
//
//        verify(reviewRepository).findById(reviewId);
//        verifyNoMoreInteractions(reviewMapper, reviewRepository);
//    }
//
//    @Test
//    @DisplayName("리뷰 추천 상태 변경 - 권한 없는 유저일 때 예외 발생")
//    void toggleRecommend_ShouldThrowException_WhenUserIsNotAuthorized() {
//        // Given
//        Long reviewId = 1L;
//        Long userId = 2L;
//
//        // Mocking Review 객체 생성 (userId 불일치)
//        Review review = Review.builder()
//                .id(reviewId)
//                .isRecommend(false)
//                .reservation(Reservation.builder()
//                        .user(User.builder().id(3L).build()) // 다른 User ID
//                        .build())
//                .build();
//
//        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
//
//        // When & Then
//        assertThatThrownBy(() -> reviewService.toggleRecommend(reviewId, userId))
//                .isInstanceOf(GlobalException.class)
//                .satisfies(exception -> {
//                    GlobalException globalException = (GlobalException) exception;
//                    assertThat(globalException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND_BY_ID);
//                });
//
//        verify(reviewRepository).findById(reviewId);
//        verifyNoMoreInteractions(reviewMapper, reviewRepository);
//    }
}