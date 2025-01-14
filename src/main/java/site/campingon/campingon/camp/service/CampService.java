package site.campingon.campingon.camp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.campingon.campingon.camp.dto.CampDetailResponseDto;
import site.campingon.campingon.camp.dto.CampListResponseDto;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.mapper.CampMapper;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.bookmark.repository.BookmarkRepository;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampService {

  private final CampRepository campRepository;
  private final BookmarkRepository bookMarkRepository;
  private final CampMapper campMapper;

  // 인기 캠핑장 조회
  public Page<CampListResponseDto> getPopularCamps(Long userId, Pageable pageable) {
    return campRepository.findPopularCamps(pageable)
        .map(camp -> {
          CampListResponseDto dto = campMapper.toCampListDto(camp);
          if (userId != 0L) {
            dto.setMarked(bookMarkRepository.existsByCampIdAndUserId(camp.getId(), userId));
          }
          return dto;
        });
  }

  // 캠핑장 상세 조회
  public CampDetailResponseDto getCampDetail(Long campId) {
    Camp camp = campRepository.findById(campId)
        .orElseThrow(() -> new GlobalException(ErrorCode.CAMP_NOT_FOUND_BY_ID));

    // induty 리스트 생성
    List<String> indutyList = Optional.ofNullable(camp.getInduty())
        .orElse(Collections.emptyList()) // Null 방어 코드
        .stream()
        .map(induty -> induty.getInduty().getType()) // Induty Enum의 type 값 추출
        .collect(Collectors.toList());

    CampDetailResponseDto dto = campMapper.toCampDetailDto(camp);
    dto.setIndutys(indutyList);
    return dto;
  }

  // 사용자의 찜한 캠핑장 목록 조회
  public Page<CampListResponseDto> getBookmarkedCamps(Long userId, Pageable pageable) {
    Page<Camp> bookmarkedCamps = campRepository.findByBookmarksUserId(userId, pageable);

    List<CampListResponseDto> campDtos = bookmarkedCamps.getContent().stream()
        .map(camp -> {
          CampListResponseDto dto = campMapper.toCampListDto(camp);
          dto.setMarked(true);
          return dto;
        })
        .collect(Collectors.toList());

    return new PageImpl<>(campDtos, pageable, bookmarkedCamps.getTotalElements());
  }

  // 캠핑장 생성
  @Transactional
  public CampDetailResponseDto createCamp(Camp camp) {
      return campMapper.toCampDetailDto(campRepository.save(camp));
  }

  // 캠핑장 수정
  @Transactional
  public CampDetailResponseDto updateCamp(Long campId, Camp updatedCamp) {
    Camp existingCamp = campRepository.findById(campId)
        .orElseThrow(() -> new GlobalException(ErrorCode.CAMP_NOT_FOUND_BY_ID));

    campMapper.updateCampFromDto(updatedCamp, existingCamp);
    return campMapper.toCampDetailDto(campRepository.save(existingCamp));
  }

  // 캠핑장 삭제
  @Transactional
  public void deleteCamp(Long id) {
    campRepository.deleteById(id);
  }

  // 모든 캠핑장 조회
  public List<CampListResponseDto> getAllCamps() {
      List<Camp> camps = campRepository.findAll();
      return camps.stream()
              .map(campMapper::toCampListDto)
              .toList();
  }

}