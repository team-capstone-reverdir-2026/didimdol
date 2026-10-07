package com.didimdol.domain.counsel.repository;

import com.didimdol.domain.counsel.entity.Counsel;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CounselRepository extends JpaRepository<Counsel, Long> {

    @Query("select coalesce(max(c.counselNo), 0) from Counsel c where c.member.id = :memberId")
    int findMaxCounselNo(@Param("memberId") Long memberId);
}