package app.global.apiPayload.code;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ErrorReasonDTO { // DTO는 DataTransfer Object로 데이터를 주거나 받을때의 형태를 지정한다.

    private HttpStatus httpStatus;
    private final String code;
    private final String message;

}