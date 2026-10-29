
package com.example.voting_system.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.voting_system.entity.Candidate;
import com.example.voting_system.entity.Voter;
import com.example.voting_system.entity.vote;
import com.example.voting_system.repository.CandidateRepository;
import com.example.voting_system.repository.VoterRepository;
import com.example.voting_system.repository.VoteRepository;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/voters")
public class VoterController {

    private final VoterRepository voterRepository;
    private final CandidateRepository candidateRepository;
    private final VoteRepository voteRepository;

    public VoterController(
            VoterRepository voterRepository,
            CandidateRepository candidateRepository,
            VoteRepository voteRepository) {

        this.voterRepository = voterRepository;
        this.candidateRepository = candidateRepository;
        this.voteRepository = voteRepository;
    }

    // Get all voters - Election Authority only
    @GetMapping
    public ResponseEntity<List<Voter>> getAllVoters() {
        return ResponseEntity.ok(voterRepository.findAll());
    }

    // Voter login
    @GetMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam("voterId") String voterId) {

        if (voterId == null || voterId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Please enter Voter ID.");
        }

        String id = voterId.trim().toUpperCase();

        Voter voter = voterRepository.findByVoterId(id)
                .orElse(null);

        if (voter == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Voter ID is not registered.");
        }

        if (voter.isHasVoted()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("You have already voted.");
        }

        return ResponseEntity.ok(
                "Login successful. You can vote.");
    }

    // Add voter - Election Authority only
    @PostMapping("/add")
    public ResponseEntity<?> addVoter(
            @RequestBody Voter voter) {

        if (voter == null
                || voter.getVoterId() == null
                || voter.getVoterId().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Voter ID is required.");
        }

        String id = voter.getVoterId().trim().toUpperCase();

        // Validate Voter ID format: ABC1234567
        if (!id.matches("^[A-Z]{3}[0-9]{7}$")) {
            return ResponseEntity.badRequest()
                    .body("Invalid format. Enter 3 letters followed by 7 digits.");
        }

        // Check duplicate Voter ID
        if (voterRepository.findByVoterId(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Voter ID already registered.");
        }

        voter.setVoterId(id);
        voter.setHasVoted(false);

        Voter savedVoter = voterRepository.save(voter);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedVoter);
    }

    // Cast vote
    @Transactional
    @PostMapping("/vote")
    public ResponseEntity<String> castVote(
            @RequestBody VoteRequest request) {

        if (request == null
                || request.voterId == null
                || request.voterId.trim().isEmpty()
                || request.candidateId == null) {

            return ResponseEntity.badRequest()
                    .body("Voter ID and Candidate ID are required.");
        }

        String id = request.voterId.trim().toUpperCase();

        Voter voter = voterRepository.findByVoterId(id)
                .orElse(null);

        if (voter == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Voter is not registered.");
        }

        // Prevent duplicate voting
        if (voter.isHasVoted()
                || voteRepository.existsByVoter(voter)) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("You have already voted.");
        }

        Candidate candidate = candidateRepository
                .findById(request.candidateId)
                .orElse(null);

        if (candidate == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Candidate not found.");
        }

        // Save voting history
        vote record = new vote(voter, candidate);
        record.setVotedAt(LocalDateTime.now());
        voteRepository.save(record);

        // Update candidate vote count
        candidate.setVotes(candidate.getVotes() + 1);
        candidateRepository.save(candidate);

        // Mark voter as voted
        voter.setHasVoted(true);
        voterRepository.save(voter);

        return ResponseEntity.ok(
                "Your vote has been submitted successfully.");
    }

    // Vote request data
    public static class VoteRequest {
        public String voterId;
        public Long candidateId;
    }

    // Get all voting records - Election Authority only
    @GetMapping("/vote-records")
    public List<vote> getAllVoteRecords() {
        return voteRepository.findAll();
    }

    // Reset election - Election Authority only
    @Transactional
    @PostMapping("/reset-election")
    public ResponseEntity<String> resetElection() {

        // Delete voting history
        voteRepository.deleteAllInBatch();

        // Reset candidate vote counts
        List<Candidate> candidates = candidateRepository.findAll();

        for (Candidate candidate : candidates) {
            candidate.setVotes(0);
        }

        candidateRepository.saveAll(candidates);

        // Allow registered voters to vote again
        List<Voter> voters = voterRepository.findAll();

        for (Voter voter : voters) {
            voter.setHasVoted(false);
        }

        voterRepository.saveAll(voters);

        return ResponseEntity.ok(
                "Election reset successfully. All votes have been cleared.");
    }

    // Delete all voters - Election Authority only
    @Transactional
    @DeleteMapping("/delete-all")
    public ResponseEntity<String> deleteAllVoters() {

        // Delete voting history first
        voteRepository.deleteAllInBatch();

        // Reset candidate vote counts
        List<Candidate> candidates = candidateRepository.findAll();

        for (Candidate candidate : candidates) {
            candidate.setVotes(0);
        }

        candidateRepository.saveAll(candidates);

        // Delete registered voters
        voterRepository.deleteAllInBatch();

        return ResponseEntity.ok(
                "All voters and voting records have been deleted successfully.");
    }
}

