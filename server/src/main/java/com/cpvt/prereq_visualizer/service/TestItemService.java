package com.cpvt.prereq_visualizer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cpvt.prereq_visualizer.model.TestItem;
import com.cpvt.prereq_visualizer.repository.TestItemRepository;

@Service
public class TestItemService {

    private final TestItemRepository testItemRepository;

    public TestItemService(TestItemRepository testItemRepository) {
        this.testItemRepository = testItemRepository;
    }

    public List<TestItem> getAllTestItems() {
        return testItemRepository.findAll();
    }
}