package com.by.sasa.bistrovic.bookkeeping.management;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AOPRepository extends JpaRepository<AOP, String> {
    List<AOP> findByUserIdAndAopListYear(String userId, Integer aopListYear);
    AOP findByUserIdAndAopCodeAndAopListYear(String userId, String aopCode, Integer aopListYear);
    
    @Modifying
    @Query("""
        DELETE FROM AOP a
        WHERE a.userId = :userId
          AND a.aopListYear = :aopListYear
    """)
    int deleteByUserIdAndAopListYear(
            @Param("userId") String userId,
            @Param("aopListYear") Integer aopListYear
    );
}
