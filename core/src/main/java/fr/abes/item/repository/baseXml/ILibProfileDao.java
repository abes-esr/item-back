package fr.abes.item.repository.baseXml;

import fr.abes.item.configuration.BaseXMLConfiguration;
import fr.abes.item.entities.baseXml.LibProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@BaseXMLConfiguration
public interface ILibProfileDao extends JpaRepository<LibProfile, String> {
    @Query("select l from LibProfile l where l.rcr in (:listRcr)")
    List<LibProfile> getShortnameAndIlnFromRcr(@Param("listRcr") List<String> listRcr);
}
