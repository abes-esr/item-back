package fr.abes.item.dao.item;

import fr.abes.item.entities.item.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRoleDao extends JpaRepository<Role, Integer> {
}
