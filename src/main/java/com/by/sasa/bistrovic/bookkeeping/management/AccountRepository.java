package com.by.sasa.bistrovic.bookkeeping.management;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, String> {

    Account findByCode(String code);
    
    List<Account> findByUserIdAndAopListYear(String userId, Integer aopListYear);
    
    List<Account> findByUserIdAndAopCodeAndAopListYear(String userId, String aopCode, Integer aopListYear);

    Optional<Account> findByUserIdAndCodeAndAopListYear(
            String userId,
            String code,
            Integer aopListYear
    );
    
    List<Account> findByUserIdAndAopListYearIsNull(
            String userId
    );
    
    @Modifying
    @Query("""
        DELETE FROM Account a
        WHERE a.userId = :userId
          AND a.aopListYear = :aopListYear
    """)
    int deleteByUserIdAndAopListYear(
            @Param("userId") String userId,
            @Param("aopListYear") Integer aopListYear
    );
}
