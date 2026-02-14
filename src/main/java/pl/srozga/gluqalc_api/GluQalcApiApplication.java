package pl.srozga.gluqalc_api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableAsync
public class GluQalcApiApplication {
	static void main(String[] args) {
		SpringApplication.run(GluQalcApiApplication.class, args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Locale.setDefault(Locale.ENGLISH);
	}
}
