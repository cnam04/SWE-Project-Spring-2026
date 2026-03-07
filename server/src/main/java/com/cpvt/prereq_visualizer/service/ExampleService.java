package com.cpvt.prereq_visualizer.service;

import org.springframework.stereotype.Service;

import com.cpvt.prereq_visualizer.model.ExampleModel; // Import a model class

// Services contain the actual logic of the application
@Service 
public class ExampleService {
    
    public ExampleModel exampleMethod(){
        // Instantiates an ExampleModel object with a message and returns it
        return new ExampleModel("I am a message. Message message message.");
    }

}
