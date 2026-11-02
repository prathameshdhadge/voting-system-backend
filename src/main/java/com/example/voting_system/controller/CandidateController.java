package com.example.voting_system.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

// GET ALL CANDIDATES - PUBLIC

@GetMapping
public List<Candidate> getAllCandidates() {
    return repository.findAll();
}

// ADD CANDIDATE - ELECTION AUTHORITY ONLY

@PreAuthorize("hasRole('ADMIN')")
@PostMapping
public Candidate addCandidate(@RequestBody Candidate candidate) {

    candidate.setVotes(0);

    return repository.save(candidate);
}

// DELETE CANDIDATE - ELECTION AUTHORITY ONLY

@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public String deleteCandidate(@PathVariable Long id) {

    Candidate candidate = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Candidate not found"
            ));

    if (candidate.getVotes() > 0) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This candidate has received votes and cannot be deleted."
        );
    }

    try {
        repository.delete(candidate);
        return "Candidate deleted successfully.";

    } catch (org.springframework.dao.DataIntegrityViolationException e) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Candidate cannot be deleted because voting records are linked to this candidate."
        );
    }
}

// VOTE - CURRENTLY PUBLIC

@PostMapping("/{id}/vote")
public Candidate vote(@PathVariable Long id) {

    Candidate candidate = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Candidate not found"
            ));

    candidate.setVotes(candidate.getVotes() + 1);

    return repository.save(candidate);
}

// WINNER - ELECTION AUTHORITY ONLY

@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/winner")
public Candidate getWinner() {

    return repository.findAll()
            .stream()
            .max((c1, c2) ->
                    Integer.compare(
                            c1.getVotes(),
                            c2.getVotes()
                    ))
            .orElse(null);
}


}
