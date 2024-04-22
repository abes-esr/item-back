package fr.abes.item.utils;

import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.utilitaire.UtilsMapper;
import fr.abes.item.dto.DemandeExempDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class WebDtoMapper {
    private final UtilsMapper mapper;

    public WebDtoMapper(UtilsMapper mapper) {
        this.mapper = mapper;
    }

    @Bean
    public void converterDemandeExempDtoToDemandeExemp() {
        Converter<DemandeExempDto, DemandeExemp> myConverter = new Converter<DemandeExempDto, DemandeExemp>() {
            @Override
            public DemandeExemp convert(MappingContext<DemandeExempDto, DemandeExemp> mappingContext) {
                DemandeExempDto source = mappingContext.getSource();
                DemandeExemp target = new DemandeExemp(source.getId());
                return target;
            }
        };
        mapper.addConverter(myConverter);
    }
}
