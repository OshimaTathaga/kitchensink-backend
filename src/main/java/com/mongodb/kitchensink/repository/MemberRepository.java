package com.mongodb.kitchensink.repository;

import com.mongodb.kitchensink.document.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByEmail(String email);

    long deleteByEmail(String email);

    long count();
}
