package cl.duoc.andesstay.reservations;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "cloudamqp.url=amqps://test:test@localhost/vhost")
class MsAndesstayReservationsApplicationTests {

	@Test
	void contextLoads() {
	}

}
