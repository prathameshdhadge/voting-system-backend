package com.example.voting_system.controller;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.example.voting_system.entity.Candidate;
import com.example.voting_system.entity.Voter;
import com.example.voting_system.repository.CandidateRepository;
import com.example.voting_system.repository.VoterRepository;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/voters")
public class VoterController {

    private final VoterRepository voterRepository;
    private final CandidateRepository candidateRepository;

    public VoterController(VoterRepository voterRepository,
                           CandidateRepository candidateRepository) {
        this.voterRepository = voterRepository;
        this.candidateRepository = candidateRepository;
    }

    @GetMapping
    public List<Voter> getAllVoters() {
        return voterRepository.findAll();
    }

    @PostMapping
    public Voter addVoter(@RequestBody Voter voter) {
        return voterRepository.save(voter);
    }

    @PostMapping("/vote")
    public String vote(@RequestBody VoteRequest request) {

        Voter voter = voterRepository.findByVoterId(request.voterId)
                .orElseThrow(() -> new RuntimeException("Voter not found"));

        if (voter.isHasVoted()) {
            return "You have already voted!";
        }

        Candidate candidate = candidateRepository.findById(request.candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        candidate.setVotes(candidate.getVotes() + 1);
        voter.setHasVoted(true);

        candidateRepository.save(candidate);
        voterRepository.save(voter);

        return "Vote submitted successfully!";
    }

    public static class VoteRequest {

        public String voterId;
        public Long candidateId;

    }
}