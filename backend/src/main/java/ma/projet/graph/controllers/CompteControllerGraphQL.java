package ma.projet.graph.controllers;

import ma.projet.graph.dto.CompteRequest;
import ma.projet.graph.dto.TransactionRequest;
import ma.projet.graph.entities.Compte;
import ma.projet.graph.entities.Transaction;
import ma.projet.graph.entities.TypeTransaction;
import ma.projet.graph.repositories.CompteRepository;
import ma.projet.graph.repositories.TransactionRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Controller
public class CompteControllerGraphQL {
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public CompteControllerGraphQL(CompteRepository compteRepository, TransactionRepository transactionRepository) {
        this.compteRepository = compteRepository;
        this.transactionRepository = transactionRepository;
    }

    @QueryMapping
    public List<Compte> allComptes() {
        return compteRepository.findAll();
    }

    @QueryMapping
    public Compte compteById(@Argument Long id) {
        return compteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Compte %s not found", id)));
    }

    @MutationMapping
    public Compte saveCompte(@Argument("compte") CompteRequest compteRequest) {
        if (compteRequest == null) {
            throw new IllegalArgumentException("CompteRequest cannot be null");
        }
        try {
            Compte compte = new Compte();
            compte.setSolde(compteRequest.getSolde());
            compte.setDateCreation(dateFormat.parse(compteRequest.getDateCreation()));
            compte.setType(compteRequest.getType());
            return compteRepository.save(compte);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format. Please use yyyy-MM-dd", e);
        }
    }


    @QueryMapping
    public Map<String, Object> totalSolde() {
        long count = compteRepository.count();
        double sum = compteRepository.sumSoldes();
        double average = count > 0 ? sum / count : 0;

        return Map.of(
                "count", count,
                "sum", sum,
                "average", average
        );
    }

    @QueryMapping
    public List<Transaction> compteTransactions(@Argument Long id) {
        Compte compte = compteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte not found"));
        return transactionRepository.findByCompte(compte);
    }

    @QueryMapping
    public List<Transaction> allTransactions() {
        return transactionRepository.findAll();
    }

    @QueryMapping
    public Map<String, Object> transactionStats() {
        long count = transactionRepository.count();
        Double sumDepots = transactionRepository.sumByType(TypeTransaction.DEPOT);
        Double sumRetraits = transactionRepository.sumByType(TypeTransaction.RETRAIT);

        return Map.of(
                "count", count,
                "sumDepots", sumDepots != null ? sumDepots : 0.0,
                "sumRetraits", sumRetraits != null ? sumRetraits : 0.0
        );
    }

    @MutationMapping
    public Transaction addTransaction(@Argument("transaction") TransactionRequest transactionRequest) {
        if (transactionRequest == null) {
            throw new IllegalArgumentException("TransactionRequest cannot be null");
        }
        try {
            Compte compte = compteRepository.findById(transactionRequest.getCompteId())
                    .orElseThrow(() -> new RuntimeException("Compte not found"));

            Transaction transaction = new Transaction();
            transaction.setMontant(transactionRequest.getMontant());
            transaction.setDate(dateFormat.parse(transactionRequest.getDate()));
            transaction.setType(transactionRequest.getType());
            transaction.setCompte(compte);

            // Update account balance
            if (transactionRequest.getType() == TypeTransaction.DEPOT) {
                compte.setSolde(compte.getSolde() + transactionRequest.getMontant());
            } else {
                if (compte.getSolde() < transactionRequest.getMontant()) {
                    throw new RuntimeException("Insufficient funds");
                }
                compte.setSolde(compte.getSolde() - transactionRequest.getMontant());
            }

            compteRepository.save(compte);
            return transactionRepository.save(transaction);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format. Please use yyyy-MM-dd", e);
        }
    }

}