package com.hrms.quartzjob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.hrms.quartzjob" })
public class QuartzjobApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuartzjobApplication.class, args);
	}

}
