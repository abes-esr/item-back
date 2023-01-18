package fr.abes.item.restart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JobExecutionDto {
    private Long jobExecutionId;
    private String context;
}
