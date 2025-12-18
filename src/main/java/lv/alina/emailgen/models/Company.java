package lv.alina.emailgen.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "companies", uniqueConstraints = @UniqueConstraint(name = "unique_user_company_name",columnNames = {"user_id", "company_name"}))
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"user", "shortCodes", "counters", "generatedEmails"})
public class Company {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Setter(AccessLevel.NONE)
	@Column(name = "company_id", nullable = false, updatable = false)
	private Long companyId;
	
	@Column(name = "company_name", nullable = false, length = 50)
	private String companyName;
	
	@Column(name = "notes", length = 255)
	private String notes;
	
	@Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_company_user"))
	private User user;
	
    @ManyToOne(optional = false)
    @JoinColumn(name = "symbol_before_shortcode_id", nullable = false,foreignKey = @ForeignKey(name = "fk_symbol_before_shortcode"))
    private Symbol symbolBeforeShortcode;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "symbol_before_sequence_id", nullable = false,foreignKey = @ForeignKey(name = "fk_symbol_before_sequence"))
    private Symbol symbolBeforeSequence;

    @OneToMany(mappedBy = "company")
    private List<ShortCodes> shortCodes = new ArrayList<>();

    @OneToMany(mappedBy = "company")
    private List<ShortCodeCounter> counters = new ArrayList<>();

    @OneToMany(mappedBy = "company")
    private List<GeneratedEmail> generatedEmails = new ArrayList<>();
    
    @OneToOne
    @JoinColumn(name = "current_short_code_id", nullable = true, unique = true, foreignKey = @ForeignKey(name = "fk_company_current_shortcode"))
    private ShortCodes currentShortCode;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public Company(User user, String companyName, String notes, Symbol symbolBeforeShortcode, Symbol symbolBeforeSequence) {
        this.user = user;
        this.companyName = companyName;
        this.notes = notes;
        this.symbolBeforeShortcode = symbolBeforeShortcode;
        this.symbolBeforeSequence = symbolBeforeSequence;
    }
    
    public void setCurrentShortCode(ShortCodes currentShortCode) {
        this.currentShortCode = currentShortCode;
    }
    
    public Company() {
    }

}
