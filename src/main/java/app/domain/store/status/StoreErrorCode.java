package app.domain.store.status;

import org.springframework.http.HttpStatus;

import app.global.apiPayload.code.BaseErrorCode;
import app.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StoreErrorCode implements BaseErrorCode {

    STORE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE001", "존재하지 않는 매장 카테고리입니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE002", "존재하지 않는 지역입니다."),
    MERCHANT_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE003", "존재하지 않는 가맹점입니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE004", "해당 가맹점을 찾을 수 없습니다."),
    REGIONCODE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE005", "지역코드가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .httpStatus(httpStatus)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .httpStatus(httpStatus)
                .build();
    }
}