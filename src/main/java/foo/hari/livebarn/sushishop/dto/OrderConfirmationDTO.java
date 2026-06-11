package foo.hari.livebarn.sushishop.dto;

import foo.hari.livebarn.sushishop.entity.SushiOrder;

import java.time.Instant;

public record OrderConfirmationDTO(
        OrderDTO order,
        int code,
        String msg
) {}


//{
//          "order": {
//            "id": 10,
//            "statusId": 1,
//            "sushiId": 1,
//            "createdAt": 1582643059540
//          },
//          "code": 0,
//          "msg": "Order created"
//        }