package com.cpvt.prereq_visualizer.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cpvt.prereq_visualizer.service.TestItemService;

@WebMvcTest(TestController.class)
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TestItemService testItemService;

    @Test
    void getAllTestItems_returnsListOfItems() throws Exception {
        when(testItemService.getAllTestItems()).thenReturn(List.of());

        mockMvc.perform(get("/api/test-items"))
                .andExpect(status().isOk());
    }
}