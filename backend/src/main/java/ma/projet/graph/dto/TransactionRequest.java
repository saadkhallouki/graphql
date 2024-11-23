package ma.projet.graph.dto;

import lombok.Data;
import ma.projet.graph.entities.TypeTransaction;

@Data
public class TransactionRequest {
    private Long compteId;
    private double montant;
    private String date;
    private TypeTransaction type;
}