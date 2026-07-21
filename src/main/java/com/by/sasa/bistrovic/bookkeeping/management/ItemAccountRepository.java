package com.by.sasa.bistrovic.bookkeeping.management;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemAccountRepository extends JpaRepository<ItemAccount, String> {

    ItemAccount findByName(String name);
}