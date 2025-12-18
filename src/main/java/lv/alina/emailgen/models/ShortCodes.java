package lv.alina.emailgen.models;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "short_codes", uniqueConstraints = @UniqueConstraint(name = "unique_user_short_code",columnNames = {"user_id", "short_code"}))
@Entity
@Getter 
@Setter 
@NoArgsConstructor 
@ToString(exclude = {"user", "company", "counters"})
public class ShortCodes {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(value = AccessLevel.NONE)
    @Column(name = "short_code_id", nullable = false, updatable = false)
    private Long shortCodeId;

    @Column(name = "short_code", nullable = false, updatable = false, length = 16)
    private String shortCode;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_shortcode_user"))
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_shortcode_company"))
    private Company company;
    
    @OneToMany(mappedBy = "shortCode")
    private java.util.List<ShortCodeCounter> counters = new java.util.ArrayList<>();
    
    public ShortCodes(User user, Company company, String shortCode) {
        this.user = user;
        this.company = company;
        this.shortCode = shortCode;
    }
    
    public ShortCodes() {
    }

}
