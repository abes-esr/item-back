package fr.abes.item.core.repository.baseXml;

import fr.abes.item.core.configuration.BaseXMLConfiguration;
import fr.abes.item.core.entities.baseXml.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@BaseXMLConfiguration
public interface IUserProfileDao extends JpaRepository<UserProfile, Integer> {
}
