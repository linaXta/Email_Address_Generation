package lv.alina.emailgen.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "main_emails",uniqueConstraints = @UniqueConstraint(name = "unique_main_email_user_email", columnNames = {"user_id", "main_email"}))
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"user", "historyRecords", "generatedEmails", "deletedGeneratedEmails"})
public class MainEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    @Column(name = "main_email_id", nullable = false, updatable = false)
    private Long mainEmailId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_main_email_user"))
    private User user;

    @Column(name = "main_email", nullable = false, length = 255)
    private String mainEmail;
    
    @Column(name = "generation_count", nullable = false)
    private Integer generationCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @OneToMany(mappedBy = "mainEmail")
    private Collection<MainEmailHistory> historyRecords = new ArrayList<>();

    @OneToMany(mappedBy = "mainEmail")
    private Collection<GeneratedEmail> generatedEmails = new ArrayList<>();
    
    @OneToMany(mappedBy = "mainEmail")
    private List<DeletedGeneratedEmail> deletedGeneratedEmails = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    public MainEmail(User user, String mainEmail) {
        this.user = user;
        this.mainEmail = mainEmail;
        this.generationCount = 0;
    }

    public MainEmail() {
    }
    
}
