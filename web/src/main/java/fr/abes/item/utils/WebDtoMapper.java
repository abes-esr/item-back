package fr.abes.item.utils;

import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.entities.item.DemandeModif;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.utilitaire.UtilsMapper;
import fr.abes.item.dto.DemandeExempWebDto;
import fr.abes.item.dto.DemandeModifWebDto;
import fr.abes.item.dto.DemandeRecouvWebDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Component
public class WebDtoMapper {
    private final UtilsMapper mapper;

    public WebDtoMapper(UtilsMapper mapper) {
        this.mapper = mapper;
    }

    @Bean
    public void converterDemandeExempToDemandeExempDto() {
        Converter<DemandeExemp, DemandeExempWebDto> myConverter = new Converter<DemandeExemp, DemandeExempWebDto>() {
            @Override
            public DemandeExempWebDto convert(MappingContext<DemandeExemp, DemandeExempWebDto> mappingContext) {
                DemandeExemp source = mappingContext.getSource();
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String dateCreation = format.format(source.getDateCreation());
                String dateModification = format.format(source.getDateModification());
                return new DemandeExempWebDto(source.getId(), source.getRcr(), source.getIln(), source.getEtatDemande().getLibelle(), source.getCommentaire(), source.getPourcentageProgressionTraitement(), dateCreation, dateModification, source.getTypeExemp().getLibelle(), source.getIndexRecherche().getLibelle());
            }
        };
        mapper.addConverter(myConverter);
    }

    @Bean
    public void converterDemandeRecouvToDemandeRecouvDto() {
        Converter<DemandeRecouv, DemandeRecouvWebDto> myConverter = new Converter<DemandeRecouv, DemandeRecouvWebDto>() {
            @Override
            public DemandeRecouvWebDto convert(MappingContext<DemandeRecouv, DemandeRecouvWebDto> mappingContext) {
                DemandeRecouv source = mappingContext.getSource();
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String dateCreation = format.format(source.getDateCreation());
                String dateModification = format.format(source.getDateModification());
                return new DemandeRecouvWebDto(source.getId(), source.getRcr(), source.getIln(), source.getEtatDemande().getLibelle(), source.getCommentaire(), source.getPourcentageProgressionTraitement(), dateCreation, dateModification, ((DemandeRecouv) source).getIndexRecherche().getLibelle());
            }
        };
        mapper.addConverter(myConverter);
    }

    @Bean
    public void converterDemandeModifToDemandeModifDto() {
        Converter<DemandeModif, DemandeModifWebDto> myConverter = new Converter<DemandeModif, DemandeModifWebDto>() {
            @Override
            public DemandeModifWebDto convert(MappingContext<DemandeModif, DemandeModifWebDto> mappingContext) {
                DemandeModif source = mappingContext.getSource();
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String dateCreation = format.format(source.getDateCreation());
                String dateModification = format.format(source.getDateModification());
                return new DemandeModifWebDto(source.getId(), source.getRcr(), source.getIln(), source.getEtatDemande().getLibelle(), source.getCommentaire(), source.getPourcentageProgressionTraitement(), dateCreation, dateModification, source.getZone() + " " + source.getSousZone(), source.getTraitement().getLibelle());
            }
        };
        mapper.addConverter(myConverter);
    }
}

