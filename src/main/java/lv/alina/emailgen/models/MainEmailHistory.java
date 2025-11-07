package lv.alina.emailgen.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "main_email_history")
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"mainEmail", "generatedEmail"})
public class MainEmailHistory {
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    @Column(name = "main_email_history_id", nullable = false, updatable = false)
    private Long mainEmailHistoryId;

    @Column(name = "old_value", length = 320)
    private String oldValue;

    @Column(name = "new_value", length = 320)
    private String newValue;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "main_email_id", nullable = false, foreignKey = @ForeignKey(name = "fk_main_email_history_main_email"))
    private MainEmail mainEmail;

    @ManyToOne(optional = true)
    @JoinColumn(name = "generated_email_id", foreignKey = @ForeignKey(name = "fk_meh_generated_email"))
    private GeneratedEmail generatedEmail;    

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 32)
    private ActionType actionType;

    @PrePersist
    protected void onCreate() {
        if (eventTime == null) eventTime = LocalDateTime.now();
    }

    public enum ActionType {
        UPDATE,// Edito main email
        GENERATE, // uzģenerets apakšepasts
        DELETE_MAIN_ONLY, //izdzēsts tikai main email, ģenerētie paliek zem company
        DELETE_WITH_GENERATED, // izdzēst viss- epasts un apakšģenerācijas
        DELETED_GENERATION // izdzēst tikkai apakšepasts
    }

}
