package com.example.voting_system.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.voting_system.entity.vote;
import com.example.voting_system.entity.Voter;

public interface VoteRepository extends JpaRepository<vote, Long> {

    Optional<vote> findByVoter(Voter voter);

    boolean existsByVoter(Voter voter);
}