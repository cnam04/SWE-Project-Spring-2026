package com.cpvt.prereq_visualizer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cpvt.prereq_visualizer.model.TestItem;
import com.cpvt.prereq_visualizer.service.TestItemService;


// This controller tests the entire process of getting data from db and returning it to the client
// Follow the objects in this controller to see.

// Calling /api/test-items tells the service to tell the repository to get the data from the db
//  then, the repository fills up the TestItem objects, which are returned to the TestItemService, which returns the data to
//   the controller, whcih sends it back to the client
// The flow is: client->controller->service->repository->db   db->repository->service->controller->client
@RestController
public class TestController {

    private final TestItemService testItemService;

    public TestController(TestItemService testItemService) {
        this.testItemService = testItemService;
    }

    @GetMapping("/api/test-items")
    public List<TestItem> getAllTestItems() {
        return testItemService.getAllTestItems();
    }
}