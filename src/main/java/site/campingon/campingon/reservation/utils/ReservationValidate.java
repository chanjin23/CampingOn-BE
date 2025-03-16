package site.campingon.campingon.reservation.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.repository.CampRepository;
import site.campingon.campingon.camp.repository.CampSiteRepository;
import site.campingon.campingon.common.exception.ErrorCode;
import site.campingon.campingon.common.exception.GlobalException;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.reservation.entity.ReservationStatus;
import site.campingon.campingon.reservation.repository.ReservationRepository;
import site.campingon.campingon.user.entity.User;
import site.campingon.campingon.user.repository.UserRepository;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationValidate {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final CampSiteRepository campSiteRepository;
    private final CampRepository campRepository;

    // REFACTOR: 쿼리최적화를 위해 발리데이션을 한번에 하는 방법?
    public Reservation validateReservationById(Long reservationId) {

        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.RESERVATION_NOT_FOUND_BY_ID));

    }

    public User validateUserById(Long userId) {

        return userRepository.findPessimisticById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND_BY_ID));
    }

    public CampSite validateCampSiteById(Long campSiteId) {

        return campSiteRepository.findPessimisticById(campSiteId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CAMPSITE_NOT_FOUND_BY_ID));
    }

    public Camp validateCampById(Long campId) {

        return campRepository.findPessimisticById(campId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CAMP_NOT_FOUND_BY_ID));
    }

    // 예약취소가 가능한지 체크
    public void validateStatus(ReservationStatus status) {

        if (status == ReservationStatus.CANCELED) {
            throw new GlobalException(ErrorCode.RESERVATION_ALREADY_CANCELED);
        }

        if (status == ReservationStatus.COMPLETED) {
            throw new GlobalException(ErrorCode.RESERVATION_ALREADY_COMPLETE);
        }
    }

    @Transactional
    public void duplicateCampSite(Long campSiteId, LocalDate checkin, LocalDate checkout) {
        //동일한 캠핑지에 이미 예약한 데이터가 존재한다면 예외처리
        if (reservationRepository.existDuplicateCampSite(campSiteId, checkin, checkout).isPresent()) {
            throw new GlobalException(ErrorCode.RESERVATION_DUPLICATE);
        }
    }

    public void validateCheckinAndCheckout(LocalDate checkin, LocalDate checkout) {
        //체크아웃이 체크인보다 빠른경우 or 예약날짜가 현재날짜보다 지난경우 예외처리
        //당일 예약 불가
//        log.info("현재 날짜 : {}", LocalDate.now());
//        log.info("체크인 날짜 : {}", checkin);
        if (!checkin.isBefore(checkout) || !LocalDate.now().isBefore(checkin)) {
            throw new GlobalException(ErrorCode.RESERVATION_NOT_VALIDATE);
        }
    }
}
