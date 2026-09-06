package com.example.voting_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.voting_system.entity.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

}