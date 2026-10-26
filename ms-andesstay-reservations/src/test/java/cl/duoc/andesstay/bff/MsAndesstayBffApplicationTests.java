package cl.duoc.andesstay.bff;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "cloudamqp.url=amqps://test:test@localhost/vhost")
class MsAndesstayBffApplicationTests {

	@Test
	void contextLoads() {
	}

}
