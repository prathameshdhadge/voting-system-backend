package com.example.voting_system.controller;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.example.voting_system.entity.Candidate;
import com.example.voting_system.repository.CandidateRepository;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/candidates")
public class CandidateController {

    private final CandidateRepository repository;

    public CandidateController(CandidateRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Candidate> getAllCandidates() {
        return repository.findAll();
    }

    @PostMapping
    public Candidate addCandidate(@RequestBody Candidate candidate) {
        return repository.save(candidate);
    }
    @PostMapping("/{id}/vote")
    public Candidate vote(@PathVariable Long id) {

        Candidate candidate = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        candidate.setVotes(candidate.getVotes() + 1);

        return repository.save(candidate);
    }
    @GetMapping("/winner")
    public Candidate getWinner() {

        return repository.findAll()
                .stream()
                .max((c1, c2) -> Integer.compare(c1.getVotes(), c2.getVotes()))
                .orElse(null);
    }
}