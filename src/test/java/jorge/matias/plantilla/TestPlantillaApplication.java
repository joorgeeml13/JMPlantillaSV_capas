package jorge.matias.plantilla;

import org.springframework.boot.SpringApplication;

public class TestPlantillaApplication {

	public static void main(String[] args) {
		SpringApplication.from(PlantillaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
