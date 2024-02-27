package fr.abes.item.entities.item;

import fr.abes.item.entities.GenericEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="ROLE")
public class Role implements Serializable, GenericEntity<Integer> {
	private static final long serialVersionUID = 1L;
	
	@Getter	@Setter @Id @Column(name="NUM_ROLE")
	private Integer numRole;
	@Getter @Setter @Column(name="USER_GROUP")
	private String userGroup;
	@Getter @Setter @Column(name="LIBELLE") 
	private String libelle;

	@Override
    public Integer getId() { return numRole; }

}
