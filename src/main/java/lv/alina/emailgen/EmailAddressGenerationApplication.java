package lv.alina.emailgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import lv.alina.emailgen.models.Company;
import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.ShortCodes;
import lv.alina.emailgen.models.Symbol;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.repos.ICompanyRepo;
import lv.alina.emailgen.repos.IMainEmailRepo;
import lv.alina.emailgen.repos.IShortCodesRepo;
import lv.alina.emailgen.repos.ISymbolRepo;
import lv.alina.emailgen.repos.IUserRepo;

import org.springframework.boot.CommandLineRunner;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class EmailAddressGenerationApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailAddressGenerationApplication.class, args);
	}
	
	@Bean
	@Order(1)
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
	
//	@Bean
//    @Order(2) 
//    CommandLineRunner addData(IUserRepo userRepo, ICompanyRepo companyRepo, IMainEmailRepo mainEmailRepo, ISymbolRepo symbolRepo, IShortCodesRepo shortCodesRepo) {
//		return new CommandLineRunner() {
////			@Override
////			public void run(String... args) throws Exception {
//// 
////           User user1 = new User("userEmail@example.com", "password", "Alina Test");
////            //userRepo.save(user1);
////            
////            Symbol plus = new Symbol(user1, "+");
////            Symbol minus = new Symbol(user1, "-");
////            Symbol none = new Symbol(user1, "");
////            //symbolRepo.save(plus);
////            //symbolRepo.save(minus);
////            //symbolRepo.save(none);
////            
////            MainEmail mainEmail1 = new MainEmail(user1, "mainEmail@mail.com");
////            //mainEmailRepo.save(mainEmail1);
////            
////            Company company1 = new Company (user1, "Organization 1", "Note example", plus, none);
////            //companyRepo.save(company1);
////            
////            ShortCodes shortCode1 = new ShortCodes(user1, company1, "ORG1");
////            //shortCodesRepo.save(shortCode1);
////            
////            company1.setCurrentShortCode(shortCode1);
////            companyRepo.save(company1);
////
////       
////			}
//			
//			@Override
//			public void run(String... args) {
//
//			    User user1 = userRepo.findByEmail("userEmail@example.com");
//			    if (user1 == null) {
//			        throw new RuntimeException("User not found");
//			    }
//
//			    Company company1 = companyRepo.findByUserAndCompanyNameIgnoreCase(user1, "Organization 1");
//			    if (company1 == null) {
//			        throw new RuntimeException("Company not found");
//			    }
//
//			    ShortCodes shortCode1 = shortCodesRepo.findByUserAndShortCode(user1, "ORG1");
//			    if (shortCode1 == null) {
//			        throw new RuntimeException("ShortCode not found");
//			    }
//
//			    company1.setCurrentShortCode(shortCode1);
//			    companyRepo.save(company1);
//
//			    System.out.println("Current shortcode UPDATED");
//			}
//		};
//    }
//	
	

}
