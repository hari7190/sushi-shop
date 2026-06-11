package foo.hari.livebarn.sushishop.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record OrderDTO(int id, int status_id, int sushi_id
        , @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) Instant createdAt) {
}
