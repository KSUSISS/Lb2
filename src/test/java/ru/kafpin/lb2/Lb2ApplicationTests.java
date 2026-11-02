package ru.kafpin.lb2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:contexttest;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always"
})
class Lb2ApplicationTests {

	@Test
	void contextLoads() {
	}

}
