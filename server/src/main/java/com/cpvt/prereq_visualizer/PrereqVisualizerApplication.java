package com.cpvt.prereq_visualizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// ENTRY POINT FOR APP

@SpringBootApplication
public class PrereqVisualizerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrereqVisualizerApplication.class, args);
		System.out.println(" \n\n\n\n\n CTRL + Click this link to test endpoints in the browser: http://localhost:8080/\n\nClose the server with CTRL-C  \n\n\n\n\n");
	}

}
