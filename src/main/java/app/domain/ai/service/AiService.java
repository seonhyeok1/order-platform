package app.domain.ai.service;

import app.domain.ai.model.dto.request.AiRequestDto;
import app.domain.ai.model.dto.response.AiResponseDto;

public interface AiService {
    AiResponseDto generateDescription(AiRequestDto aiRequestDto);
}
