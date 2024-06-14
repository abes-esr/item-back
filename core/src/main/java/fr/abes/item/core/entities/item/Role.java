package fr.abes.item.core.entities.item;

import fr.abes.item.core.entities.GenericEntity;
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
@Getter @Setter
public class Role implements Serializable, GenericEntity<Integer> {
	@Id @Column(name="NUM_ROLE")
	private Integer numRole;
	@Column(name="USER_GROUP")
	private String userGroup;
	@Column(name="LIBELLE")
	private String libelle;

	@Override
    public Integer getId() { return numRole; }

}
