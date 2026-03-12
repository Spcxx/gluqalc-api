package pl.srozga.gluqalc_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication
public class GluQalcApiApplication {
	static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Locale.setDefault(Locale.ENGLISH);

		SpringApplication.run(GluQalcApiApplication.class, args);
	}
}
