package com.cao.thumbsup;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cao.thumbsup.mapper")
public class ThumbsUpApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThumbsUpApplication.class, args);
	}

}
