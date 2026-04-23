package com.cpvt.prereq_visualizer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cpvt.prereq_visualizer.model.ExampleModel;
import com.cpvt.prereq_visualizer.service.ExampleService;
 // All imports for objects we make will have the path: "import com.cpvt.prereq_visualizer.[FOLDER].[NAME OF FILE];"

@RestController
public class ExampleController {

    // Service object becomes part of the controller class
    private final ExampleService exampleService;

    // Springboot injects the service when you add it in a constructor
    public ExampleController(ExampleService exS){
        this.exampleService = exS;
    }
    @GetMapping("/") // GetMapping maps the url to the endpoint
    public String home() { // this is the controller for this request
        return "Backend works. try testing /api/"; // Returns hard coded data
    }

    @GetMapping("/api/") // GetMapping maps the url to the endpoint -> you could type http://localhost:8080/api/ and it would go to this endpoint
    public ExampleModel example() { // this is the controller for this request
        return exampleService.exampleMethod(); // send the data returned by exampleService.exampleMethod() to the client
    }
}