package foo.hari.livebarn.sushishop.mapper;

import foo.hari.livebarn.sushishop.dto.OrderDTO;
import foo.hari.livebarn.sushishop.entity.SushiOrder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class OrderMapper {

    public OrderDTO toDTO(SushiOrder entity) {
        if (entity == null) {
            return null;
        }

        return new OrderDTO(
                entity.getId(),
                entity.getStatusId(),
                entity.getSushi_id(),
                // Convert database LocalDateTime safely to an Instant (UTC)
                entity.getCreatedAt() != null ? entity.getCreatedAt().toInstant(ZoneOffset.UTC) : null
        );
    }
}