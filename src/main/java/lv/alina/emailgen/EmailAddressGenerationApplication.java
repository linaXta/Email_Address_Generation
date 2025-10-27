package lv.alina.emailgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

import javax.sql.DataSource;
import java.sql.Connection;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.repos.IMainEmailRepo;
import lv.alina.emailgen.repos.IUserRepo;

@SpringBootApplication
public class EmailAddressGenerationApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailAddressGenerationApplication.class, args);
	}
	
	@Bean
    CommandLineRunner dbPing(DataSource dataSource) {
        return args -> {
            try (Connection c = dataSource.getConnection()) {
                System.out.println("DB connection OK → " + c.getMetaData().getURL());
            } catch (Exception e) {
                System.err.println("DB connection FAILED: " + e.getMessage());
                throw e;
            }
        };
    }

}
