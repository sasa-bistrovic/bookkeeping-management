package com.by.sasa.bistrovic.bookkeeping.management;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {

    Account findByCode(String code);
}
