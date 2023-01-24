package fr.abes.item.service.impl;

import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.baseXml.LibProfile;
import fr.abes.item.entities.baseXml.UserProfile;
import fr.abes.item.entities.item.Utilisateur;
import fr.abes.item.service.IUtilisateurService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Service
public class UtilisateurService implements IUtilisateurService {
    @Autowired @Getter
    DaoProvider dao;

    @Override
    public Utilisateur findById(Integer id) {
        Optional<Utilisateur> user = dao.getUtilisateur().findById(id);
        if (user.isPresent()) {
            Utilisateur utilisateur = user.get();
            setIlnOnUtilisateur(utilisateur);
            return utilisateur;
        }
        return null;
    }

    @Override
    public String findRcrById(String id){
        return dao.getUserProfile().findAllByUserNum(Integer.parseInt(id)).getLibrary();
    }

    @Override
    public Utilisateur save(Utilisateur utilisateur) {
        return dao.getUtilisateur().save(utilisateur);
    }

    @Override
    public void deleteById(Integer id) {
        dao.getUtilisateur().deleteById(id);
    }

    @Override
    public void setIlnOnUtilisateur(Utilisateur utilisateur) {
        Optional<UserProfile> userProfile = getDao().getUserProfile().findById(utilisateur.getId());
        if (userProfile.isPresent()) {
            String rcr = userProfile.get().getLibrary().trim();
            Optional<LibProfile> libProfile = getDao().getLibProfile().findById(rcr);
            if (libProfile.isPresent())
                utilisateur.setIln(libProfile.get().getIln());
        }
    }

    @Override
    public boolean isAdmin(Utilisateur utilisateur) {
        Optional<UserProfile> userProfile = getDao().getUserProfile().findById(utilisateur.getId());
        if (userProfile.isPresent()) {
            if (userProfile.get().getUserGroup().equals(getDao().getRole().findById(1).get().getUserGroup())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String findEmailById(Integer id) {
        Optional<Utilisateur> utilisateurOpt = dao.getUtilisateur().findById(id);
        return utilisateurOpt.map(Utilisateur::getEmail).orElse(null);
    }
}
