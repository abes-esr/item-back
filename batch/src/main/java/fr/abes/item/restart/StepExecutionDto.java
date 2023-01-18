package fr.abes.item.restart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class StepExecutionDto {
    private String stepName;
    private Date endTime;
    private Date lastUpdated;

}
