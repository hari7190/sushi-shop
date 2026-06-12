# Sushi Shop

## Requirements

- Java 17
- Maven

## Run

```bash
./mvnw spring-boot:run
```

Server runs at `http://localhost:9000`

## API

Base path: `/api`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/orders` | Place an order |
| GET | `/orders/status` | List orders by status |
| PUT | `/orders/{order_id}/pause` | Pause an order |
| PUT | `/orders/{order_id}/resume` | Resume a paused order |
| DELETE | `/orders/{order_id}` | Cancel an order |


## Postman

Import `postman/Sushi-Shop-API.postman_collection.json`


## Test

```bash
./mvnw test
```
