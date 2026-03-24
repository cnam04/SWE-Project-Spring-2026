package com.cpvt.prereq_visualizer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// This is a model for a test item (uses data from test_items table in SQL Database)
@Entity // This wrapper makes springboot treat the class as model
@Table(name = "test_items") // This wrapper connects the class with the table
public class TestItem {
    @Id // This wrapper signifies that it is specifically an id field
    @GeneratedValue(strategy = GenerationType.IDENTITY) // This days to generate a new id value if there is none
    private Long id;
                                            
    private String name;

        public TestItem() {
    }                                      

    public TestItem(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}

