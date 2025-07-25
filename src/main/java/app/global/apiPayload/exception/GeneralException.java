package app.global.apiPayload.exception;


import app.global.apiPayload.code.ErrorReasonDTO;
import app.global.apiPayload.code.status.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private final ErrorStatus errorStatus;

    public ErrorReasonDTO getErrorReasonHttpStatus(){
        return this.errorStatus.getReasonHttpStatus();
    }
}