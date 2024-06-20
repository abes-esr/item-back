package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UtilisateurWebDto {
    @JsonProperty("userId")
    private Integer userId;
    @JsonProperty("email")
    private String email;
}
