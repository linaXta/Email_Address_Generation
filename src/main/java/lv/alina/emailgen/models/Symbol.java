package lv.alina.emailgen.models;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "symbols", uniqueConstraints = @UniqueConstraint(name = "unique_user_symbol", columnNames = {"user_id", "symbol"}))
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "user" )
public class Symbol {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    @Column(name = "symbol_id", nullable = false, updatable = false)
    private Long symbolId;
	
	@Column(name = "symbol", nullable = false, length = 5)
    private String symbol; 

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_symbol_user"))
    private User user;
    
    public Symbol(User user, String symbol) {
        this.user = user;
        this.symbol = symbol;
    }
    
    public Symbol() {
    }

}
