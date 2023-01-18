package fr.abes.item.web.impl;

import fr.abes.item.web.AbstractRestService;
import fr.abes.item.web.IInformationApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class InformationApplicationService extends AbstractRestService implements IInformationApplicationService {
    @Value("${application.version.back}")
    private String applicationVersionBack;
    @Value("${application.version.front}")
    private String applicationVersionFront;

    @Override
    public Map<String, String> getApplicationDetails() {
        Map<String, String> map = new HashMap<>();
        map.put("FRONTVERSION", this.applicationVersionFront);
        map.put("BACKVERSION", this.applicationVersionBack);
        return map;
    }

    @Override
    public Map<String, Boolean> getStatutServices() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("STATUT CBS", getService().getStatus().getCbsConnectionStatus());
        map.put("STATUT BASE_XML", getService().getStatus().getXmlConnectionStatus());
        map.put("STATUT BASE_ITEM", getService().getStatus().getKopyaDataBaseStatus());
        return map;
    }
}
