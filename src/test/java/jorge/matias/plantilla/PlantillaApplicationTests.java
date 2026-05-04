package jorge.matias.plantilla;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
@SpringBootTest
class PlantillaApplicationTests {

	@Test
	void contextLoads() {
	}

}
