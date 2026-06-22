package com.requestflow.dto.ai;

import com.requestflow.domain.enums.RequestCategory;
import com.requestflow.domain.enums.RequestPriority;
import com.requestflow.domain.enums.AiSuggestionSource;

public record AiSuggestionDTO(
        RequestCategory category,
        RequestPriority priority,
        String summary,
        AiSuggestionSource source
) {
}
