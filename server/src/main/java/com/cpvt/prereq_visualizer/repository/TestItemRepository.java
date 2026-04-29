package com.cpvt.prereq_visualizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cpvt.prereq_visualizer.model.TestItem;

public interface TestItemRepository extends JpaRepository<TestItem, Long> {
}