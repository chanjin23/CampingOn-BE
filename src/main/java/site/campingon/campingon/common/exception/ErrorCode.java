package site.campingon.campingon.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH-001", "비밀번호가 일치하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-002", "리프레시 토큰이 만료되었습니다."),
    NO_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-003", "토큰이 존재하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-004", "유효하지 않은 토큰입니다."),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "AUTH-005", "인증되지 않은 유저입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-006", "유효하지 않은 사용자 이름 또는 비밀번호입니다."),
    INVALID_SECRET_KEY(HttpStatus.UNAUTHORIZED, "AUTH-007", "유효하지 않은 비밀 키입니다."),
    DELETE_USER_DENIED(HttpStatus.FORBIDDEN, "AUTH-008", "회원 탈퇴가 거부되었습니다."),
    ROLE_NOT_FOUND(HttpStatus.FORBIDDEN, "AUTH-009", "권한 정보가 없습니다."),
    BLACKLIST_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-010", "사용할 수 없는 액세스 토큰입니다."),

    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "ACCOUNT-001", "이미 존재하는 이메일입니다."),
    USER_NOT_FOUND_BY_EMAIL(HttpStatus.NOT_FOUND, "ACCOUNT-002", "해당 이메일의 회원을 찾을 수 없습니다."),
    USER_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "ACCOUNT-003", "해당 아이디의 회원을 찾을 수 없습니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "ACCOUNT-004", "이미 사용 중인 닉네임입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "ACCOUNT-005", "현재 비밀번호가 일치하지 않습니다."),
    PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "ACCOUNT-006", "새로운 비밀번호는 현재 비밀번호와 달라야 합니다."),
    CURRENT_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "ACCOUNT-007", "현재 비밀번호를 입력해야 합니다."),

    CAMP_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "CAMP-001", "캠핑장의 ID를 찾을 수 없습니다."),
    CAMP_NOT_FOUND(HttpStatus.NOT_FOUND, "CAMP-002", "캠핑장을 찾을 수 없습니다."),

    CAMPSITE_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "CAMPSITE-001", "캠핑지의 ID를 찾을 수 없습니다."),
    REQUIRED_RESERVATION_DATE(HttpStatus.BAD_REQUEST, "CAMPSITE-002", "예약일자를 선택하지 않았습니다."),

    REVIEW_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "REVIEW-001", "해당 리뷰를 찾을 수 없습니다."),
    REVIEW_NOT_IN_CAMP(HttpStatus.NOT_FOUND, "REVIEW-002", "리뷰가 해당 캠프에 속하지 않습니다."),
    REVIEW_ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "REVIEW-003", "이미 이 예약에 대해 리뷰를 작성하셨습니다."),

    CAMP_INDUTY_NOT_FOUND(HttpStatus.NOT_FOUND, "INDUTY-001", "존재하지 않는 업종입니다."),

    RESERVATION_STATUS_NOT_FOUND(HttpStatus.BAD_REQUEST, "RESERVATION-001", "알 수없는 예약 상태입니다."),
    RESERVATION_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "RESERVATION-002", "해당 예약 ID를 찾을 수 없습니다."),
    RESERVATION_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "RESERVATION-003", "이미 취소된 예약입니다."),
    RESERVATION_ALREADY_COMPLETE(HttpStatus.BAD_REQUEST, "RESERVATION-004", "이미 지난 예약입니다."),
    RESERVATION_NOT_COMPLETED_FOR_REVIEW(HttpStatus.BAD_REQUEST, "RESERVATION-005", "후기는 체크인이 완료된 상태에서만 작성할 수 있습니다."),
    RESERVATION_DUPLICATE(HttpStatus.BAD_REQUEST, "RESERVATION_006", "누군가 해당 캠핑지를 예약한상태입니다."),
    RESERVATION_NOT_VALIDATE(HttpStatus.BAD_REQUEST, "RESERVATION_007", "올바르지 않은 데이터 요청입니다."),

    BOOKMARK_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKMARK-001", "북유찾없"),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "미완", "잘못된 접근입니다."),

    FILE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE-001", "업로드 가능한 파일 개수를 초과했습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE-002", "파일 크기가 허용된 용량을 초과했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE-003", "지원하지 않는 파일 형식입니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE-004", "요청한 파일을 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-006", "파일 업로드 중 오류가 발생했습니다."),
    INVALID_FILE_LIST(HttpStatus.BAD_REQUEST, "FILE-006", "파일 목록이 비어있거나 유효하지 않습니다."),
    INVALID_FILE_PATH(HttpStatus.BAD_REQUEST, "FILE-007", "파일 경로나 이름이 유효하지 않습니다."),
    FILE_MOVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-008", "파일 이동 중 오류가 발생했습니다."),

    // 키워드 찾을 수 없음
    KEYWORD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "KEYWORD-001", "키워드는 최대 5개까지만 등록 가능합니다."),

    GO_CAMPING_BAD_REQUEST(HttpStatus.BAD_REQUEST, "GOCAMPING-001", "잘못된 고캠핑 데이터 요청입니다."),
    GO_CAMPING_IMAGE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "GOCAMPING-002", "DB에 저장된 캠프가 없거나 잘못된 데이터 요청입니다."),
    GO_CAMPING_DATA_NO_CONTENT(HttpStatus.NO_CONTENT, "GOCAMPING-003", "요청하신 응답은 데이터가 없습니다."),
    GO_CAMPING_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GOCAMPING-004", "고캠핑서비스에서 서버오류가발생했습니다."),
    GO_CAMPING_DATA_MAPPING_ERROR(HttpStatus.BAD_REQUEST, "GOCAMPING-005", "데이터의 Json 매핑이 잘못됐습니다."),
    GO_CAMPING_NETWORK_ERROR(HttpStatus.BAD_REQUEST, "GOCAMPING-006", "고캠핑 네트워크 오류입니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}