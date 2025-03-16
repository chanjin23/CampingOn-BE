package site.campingon.campingon.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.mapper.CampSiteMapper;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.reservation.dto.*;
import site.campingon.campingon.reservation.entity.ReservationStatus;
import site.campingon.campingon.reservation.repository.ReservationRepository;
import site.campingon.campingon.reservation.mapper.ReservationMapper;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.reservation.utils.ReservationValidate;
import site.campingon.campingon.user.entity.User;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final CampSiteMapper campSiteMapper;
    private final ReservationMapper reservationMapper;
    private final ReservationValidate reservationValidate;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public Page<ReservationResponseDto> getReservations(Long userId, Pageable pageable) {

        reservationValidate.validateUserById(userId);

        Page<Reservation> reservations = reservationRepository.findReservationsByUserId(userId, pageable);

        return reservations.map(reservationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ReservationResponseDto getReservation(Long userId, Long reservationId) {

        reservationValidate.validateUserById(userId);

        Reservation reservation = reservationValidate.validateReservationById(reservationId);

        return reservationMapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponseDto getUpcomingReservation(Long userId) {

        reservationValidate.validateUserById(userId);

        Reservation reservation = reservationRepository.findUpcomingReservationByUserId(userId);

        return reservationMapper.toResponse(reservation);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createReservation(Long userId, ReservationCreateRequestDto requestDto) {

        //REFACTOR: 예약테이블에서 기존 예약과의 유효성 검증을 한번 더 함
        // 기존 예약 체크인 날짜 < 요청 체크아웃 날짜 && 기존 예약 체크아웃 날짜 > 요청 체크인 날짜 -> true 라면 예외던짐(예약중복)
        reservationValidate.validateCheckinAndCheckout(requestDto.getCheckin(),requestDto.getCheckout());

        User user = reservationValidate.validateUserById(userId);

        CampSite campSite = reservationValidate.validateCampSiteById(requestDto.getCampSiteId());

        Camp camp = reservationValidate.validateCampById(requestDto.getCampId());

        reservationValidate.duplicateCampSite(
                requestDto.getCampSiteId(), requestDto.getCheckin(), requestDto.getCheckout());

        // LocalDate(JSON) -> LocalDateTime(DB)
        LocalDateTime checkin = requestDto.getCheckin().atTime(15, 0);
        LocalDateTime checkout = requestDto.getCheckout().atTime(11, 0);

        Reservation reservation = reservationMapper.toEntity(requestDto)
                .toBuilder()
                .user(user)
                .camp(camp)
                .campSite(campSite)
                .checkin(checkin)
                .checkout(checkout)
                .status(ReservationStatus.RESERVED)
                .build();

        reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(Long userId, Long reservationId, ReservationCancelRequestDto requestDto) {

        reservationValidate.validateUserById(userId);

        reservationValidate.validateCampSiteById(requestDto.getCampSiteId());

        reservationValidate.validateCampById(requestDto.getCampId());

        Reservation reservation = reservationValidate.validateReservationById(requestDto.getId());

        reservationValidate.validateStatus(requestDto.getStatus());

        reservation.cancel(requestDto.getCancelReason());

        reservationRepository.save(reservation);
    }

}
