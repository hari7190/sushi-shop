package foo.hari.livebarn.sushishop.controller;

import foo.hari.livebarn.sushishop.repository.StatusRepository;
import foo.hari.livebarn.sushishop.repository.SushiOrderRepository;
import foo.hari.livebarn.sushishop.task.CookOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    SushiOrderRepository sushiOrderRepository;

    @MockitoBean
    CookOrchestrator cookOrchestrator;

    @Test
    void placeOrder_withValidSushi_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sushi_name": "California Roll"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.msg", is("Order Created")))
                .andExpect(jsonPath("$.order.sushi_id", is(1)))
                .andExpect(jsonPath("$.order.status_id", is(statusRepository.getStatusByName("created").getId())));

        var orders = sushiOrderRepository.findAll();
        org.junit.jupiter.api.Assertions.assertEquals(1, orders.size());
        org.junit.jupiter.api.Assertions.assertEquals(30, orders.get(0).getRemaining_time());
    }

    @Test
    void placeOrder_withUnknownSushi_returnsNotFound() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sushi_name": "Unknown Roll"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(404)))
                .andExpect(jsonPath("$.msg", is("No Sushi Found!")));
    }
}
