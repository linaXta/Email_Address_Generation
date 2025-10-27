package lv.alina.emailgen.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Table(name = "main_email_history")
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "mainEmail")
public class MainEmailHistory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Setter(value = AccessLevel.NONE)
	@Column(name = "main_email_history_id", nullable = false, updatable = false)
	private Long mainEmailHistoryId;
	
	@ManyToOne
	@JoinColumn(name = "main_email_id", nullable = false)
	private MainEmail mainEmail;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 10)
	private ActionType actionType;
	
	@Column(name = "old_value", length = 320)
	private String oldValue;

    @Column(name = "new_value", length = 320)
    private String newValue;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum ActionType {
        UPDATE,
        DELETE
    }

}
