package fr.abes.item.dao.baseXml;

import fr.abes.item.entities.baseXml.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserProfileDao extends JpaRepository<UserProfile, Integer> {
}
