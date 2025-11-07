package lv.alina.emailgen.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "generated_emails",uniqueConstraints = @UniqueConstraint(name = "unique_company_email_address", columnNames = {"company_id", "email_address"}))
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"mainEmail", "company", "shortCode"})
public class GeneratedEmail {
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    @Column(name = "generated_email_id", nullable = false, updatable = false)
    private Long generatedEmailId;
    
    @Column(name = "email_address", nullable = false, length = 320)
    private String generatedEmailAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "notes", length = 255)
    private String notes;

    @ManyToOne(optional = true)//lai izdzēstu main email, bet saglabā ģenerētos zem company
    @JoinColumn(name = "main_email_id", foreignKey = @ForeignKey(name = "fk_generated_main_email"))
    private MainEmail mainEmail;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_generated_email_company"))
    private Company company;

    @ManyToOne(optional = false)
    @JoinColumn(name = "short_code_id", nullable = false, foreignKey = @ForeignKey(name = "fk_generated_email_shortcode"))
    private ShortCodes shortCode;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

}
