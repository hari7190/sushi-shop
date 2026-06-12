package foo.hari.livebarn.sushishop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OrderStatusResponseDTO(
        @JsonProperty("in-progress") List<OrderStatusEntryDTO> inProgress,
        List<OrderStatusEntryDTO> created,
        List<OrderStatusEntryDTO> paused,
        List<OrderStatusEntryDTO> cancelled,
        List<OrderStatusEntryDTO> completed
) {}
