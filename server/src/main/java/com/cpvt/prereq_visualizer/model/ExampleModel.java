package com.cpvt.prereq_visualizer.model;


// when a controller returns this object it will convert it to JSON automatically
public class ExampleModel{
    private String message;

    public ExampleModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
