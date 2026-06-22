package com.requestflow.integration;

import com.requestflow.dto.ai.AiSuggestionDTO;
import com.requestflow.dto.ai.AiSuggestionRequestDTO;

public interface AiService {

    AiSuggestionDTO suggest(AiSuggestionRequestDTO request);
}
