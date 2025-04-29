package com.cao.thumbsup;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Date;

@SpringBootTest
class ThumbsUpApplicationTests {

	@Test
	void contextLoads() {
		LocalDate localDate = LocalDate.now();
//		System.out.println(now);
		localDate = localDate.plusMonths(-1);
		System.out.println(localDate);
	}

}
