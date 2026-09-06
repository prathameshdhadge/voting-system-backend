package com.example.voting_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.example.voting_system.entity.Voter;

public interface VoterRepository extends JpaRepository<Voter, Long> {
	 Optional<Voter> findByVoterId(String voterId);
}