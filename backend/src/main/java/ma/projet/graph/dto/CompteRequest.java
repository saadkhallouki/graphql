package ma.projet.graph.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.projet.graph.entities.TypeCompte;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteRequest {
    private double solde;
    private String dateCreation;
    private TypeCompte type;
}