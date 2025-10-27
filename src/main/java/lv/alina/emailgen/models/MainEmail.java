package lv.alina.emailgen.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;



@Table(name = "main_emails")
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "user")
public class MainEmail {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Setter(value = AccessLevel.NONE)
	@Column(name = "main_email_id", nullable = false, updatable = false)
	private Long mainEmailId;
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@Column(name = "main_email", nullable = false, length = 64)
	private String mainEmail;
	
	@Column(name = "crated_at" , nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();	
	
	@Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "generation_count")
    private Integer generationCount = 0;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
	
}
