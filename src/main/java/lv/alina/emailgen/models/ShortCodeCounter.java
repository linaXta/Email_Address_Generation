package lv.alina.emailgen.models;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "short_code_counter", uniqueConstraints = { @UniqueConstraint( name = "unique_counter_company_shortcode", columnNames = {"company_id", "short_code_id"})})
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"company", "shortCode"})
public class ShortCodeCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    @Column(name = "counter_id", nullable = false, updatable = false)
    private Long counterId;
    
    @Column(name = "last_number", nullable = false)
    private int lastNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_counter_company"))
    private Company company;

    @ManyToOne(optional = false)
    @JoinColumn(name = "short_code_id", nullable = false, foreignKey = @ForeignKey(name = "fk_counter_shortcode"))
    private ShortCodes shortCode;

}
