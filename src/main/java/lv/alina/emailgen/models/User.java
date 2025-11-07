package lv.alina.emailgen.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "users")
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"passwordHash", "mfaSecretEnc", "symbols", "companies", "shortCodes", "mainEmails"})
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Setter(value = AccessLevel.NONE)
	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;
	
	@Column(name = "email", nullable = false, unique = true, length = 255)
	private String email;
	
	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;
	
	@Column(name = "full_name", length = 120)
	private String fullName;
	
	@Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;
	
	@Column(name = "mfa_secret_enc", length = 255)
    private String mfaSecretEnc;
	
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
   
    @OneToMany(mappedBy = "user")
    private List<Symbol> symbols = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    private List<Company> companies = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    private List<ShortCodes> shortCodes = new java.util.ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    private List<MainEmail> mainEmails = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
    	this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
