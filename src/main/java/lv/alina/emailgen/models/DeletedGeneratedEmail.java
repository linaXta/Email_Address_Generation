package lv.alina.emailgen.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "deleted_generated_emails")
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "mainEmail")
public class DeletedGeneratedEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    @Column(name = "deleted_id", nullable = false, updatable = false)
    private Long deletedId;

    @Column(name = "email_address", nullable = false, length = 320)
    private String emailAddress;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "main_email_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_deleted_ge_main_email"))
    private MainEmail mainEmail;
    
    @PrePersist
    protected void onCreate() {
        if (deletedAt == null) deletedAt = LocalDateTime.now();
    }

}
