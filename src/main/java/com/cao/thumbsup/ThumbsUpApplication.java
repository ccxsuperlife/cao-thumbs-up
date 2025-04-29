package com.cao.thumbsup;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@MapperScan("com.cao.thumbsup.mapper")
@EnableRedisHttpSession // 启用Redis存储Session
public class ThumbsUpApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThumbsUpApplication.class, args);
	}

}
