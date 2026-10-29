package cl.duoc.andesstay.reservations;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "rabbitmq.url=amqp://andesstay:andesstay_local@localhost:5672/%2F")
class MsAndesstayReservationsApplicationTests {

	@Test
	void contextLoads() {
	}

}
