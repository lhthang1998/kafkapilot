package com.example.kafkapilot;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@SpringBootApplication
public class KafkapilotApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkapilotApplication.class, args);
	}

}
