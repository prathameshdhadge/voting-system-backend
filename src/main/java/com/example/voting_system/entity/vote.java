package com.example.voting_system.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
name = "vote_record",
uniqueConstraints = {
@UniqueConstraint(
name = "uk_vote_voter",
columnNames = "voter_id"
)
}
)
public class vote {


@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne(optional = false)
@JoinColumn(name = "voter_id", nullable = false)
private Voter voter;

@ManyToOne(optional = false)
@JoinColumn(name = "candidate_id", nullable = false)
private Candidate candidate;

@Column(name = "voted_at")
private LocalDateTime votedAt;

public vote() {
}

public vote(Voter voter, Candidate candidate) {
    this.voter = voter;
    this.candidate = candidate;
    this.votedAt = LocalDateTime.now();
}

public Long getId() {
    return id;
}

public Voter getVoter() {
    return voter;
}

public void setVoter(Voter voter) {
    this.voter = voter;
}

public Candidate getCandidate() {
    return candidate;
}

public void setCandidate(Candidate candidate) {
    this.candidate = candidate;
}

public LocalDateTime getVotedAt() {
    return votedAt;
}

public void setVotedAt(LocalDateTime votedAt) {
    this.votedAt = votedAt;
}


}
