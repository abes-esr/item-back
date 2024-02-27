package fr.abes.item.repository.item;

import fr.abes.item.configuration.ItemConfiguration;
import fr.abes.item.entities.item.TypeExemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ItemConfiguration
public interface ITypeExempDao extends JpaRepository<TypeExemp, Integer> {
}
