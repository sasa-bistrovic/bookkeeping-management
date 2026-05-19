package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080", "https://bookkeeping-management-b16af486dd67.herokuapp.com"})
public class AccountingController {

    private final AccountingService service;
    private final AccountRepository accountRepo;
    private final AccountClassRepository accountClassRepo;
    private final AccountGroupRepository accountGroupRepo;
    private final AccountSyntheticRepository accountSyntheticRepo;
    private final AccountAnalyticalRepository accountAnayticalRepo;
    private final AccountSubAnalyticalRepository accountSubAnalyticalRepo;
    private final PartnerRepository partnerRepo;
    private final ItemAccountRepository itemAccountRepo;
    private final LedgerEntryRepository ledgerRepo;
    private final TaxRateRepository taxRepo;
    private final AOPRepository aopRepo;
    private final GiroAccountRepository giroAccountRepo;
    private final InvoiceRepository invoiceRepo;
    private final WarehouseItemRepository warehouseItemRepo;

    public AccountingController(AccountingService service,
                                AccountRepository accountRepo,
                                AccountClassRepository accountClassRepo,
                                AccountGroupRepository accountGroupRepo,
                                AccountSyntheticRepository accountSyntheticRepo,
                                AccountAnalyticalRepository accountAnayticalRepo,
                                AccountSubAnalyticalRepository accountSubAnalyticalRepo,
                                PartnerRepository partnerRepo,
                                ItemAccountRepository itemAccountRepo,
                                LedgerEntryRepository ledgerRepo,
                                TaxRateRepository taxRepo,
                                AOPRepository aopRepo,
                                GiroAccountRepository giroAccountRepo,
                                InvoiceRepository invoiceRepo,
                                WarehouseItemRepository warehouseItemRepo) {
        this.service = service;
        this.accountRepo = accountRepo;
        this.partnerRepo = partnerRepo;
        this.itemAccountRepo = itemAccountRepo;
        this.ledgerRepo = ledgerRepo;
        this.taxRepo = taxRepo;
        this.giroAccountRepo = giroAccountRepo;
        this.aopRepo = aopRepo;
        this.invoiceRepo = invoiceRepo;
        this.warehouseItemRepo = warehouseItemRepo;
        this.accountClassRepo = accountClassRepo;
        this.accountGroupRepo = accountGroupRepo;
        this.accountSyntheticRepo = accountSyntheticRepo;
        this.accountAnayticalRepo = accountAnayticalRepo;
        this.accountSubAnalyticalRepo = accountSubAnalyticalRepo;
    }

    @GetMapping("/accounts")
    public List<Account> getAccounts() {
        return accountRepo.findAll();
    }
    
    @GetMapping("/aop")
    public List<AOP> getAOPs() {
        return aopRepo.findAll();
    }

    @PostMapping("/accounts")
    public Account addAccount(@RequestBody Account acc) {
        return accountRepo.save(acc);
    }
    
    @PostMapping("/accounts/all/batch")
    public List<Account> addAccount(@RequestBody List<Account> list) {
        return accountRepo.saveAll(list);
    }                    
    
    @PostMapping("/aop")
    public AOP addAOP(@RequestBody AOP aop) {
        return aopRepo.save(aop);
    }

    @GetMapping("/partners")
    public List<Partner> getPartners() {
        return partnerRepo.findAll();
    }

    @PostMapping("/partners")
    public Partner addPartner(@RequestBody Partner p) {
        return partnerRepo.save(p);
    }

    @GetMapping("/tax-rates")
    public List<TaxRate> getTaxRates() {
        return taxRepo.findAll();
    }

    @GetMapping("/invoices")
    public List<Invoice> getInvoices() {
        return invoiceRepo.findAll();
    }

    @PostMapping("/invoices")
    public Invoice createInvoice(@RequestBody Invoice inv) {
        inv.setStatus(InvoiceStatus.PRE_LEDGER);
        return service.createInvoice(inv);
    }
    
    @PutMapping("/invoices/calc")
    public Invoice createCalc(@RequestBody Invoice inv) {
        Invoice invoice = invoiceRepo.findById(inv.getId()).orElseThrow();
        
        invoice.setPaidSoFar(inv.getPaidSoFar());
        invoice.setRemainToBePaid(inv.getRemainToBePaid());
        invoice.setPaymentDate(inv.getPaymentDate());
        
        return invoiceRepo.save(invoice);
    }
    
    @PostMapping("/invoices/all/batch")
    public List<Invoice> addInvoiceAll(@RequestBody List<Invoice> list) {
        return invoiceRepo.saveAll(list);
    }                        

    @PutMapping("/invoices/{id}")
    @Transactional
    public ResponseEntity<Invoice> updateInvoice(
            @PathVariable String id,
            @RequestBody Invoice updated) {

        Invoice inv = invoiceRepo.findById(id).orElseThrow();
        
        Invoice inv2 = inv;

        inv.setInvoiceNumber(updated.getInvoiceNumber());
        inv.setDescription(updated.getDescription());
        inv.setDate(updated.getDate());
        inv.setDueDate(updated.getDueDate());
        inv.setPartnerId(updated.getPartnerId());

        inv.setPaidSoFar(updated.getPaidSoFar());
        inv.setRemainToBePaid(updated.getRemainToBePaid());
        inv.setPaymentDate(updated.getPaymentDate());

        inv.setTotalBase(updated.getTotalBase());
        inv.setTotalTax(updated.getTotalTax());
        inv.setTotalAmount(updated.getTotalAmount());
        inv.setType(updated.getType());

        // FIX
        inv.getItems().clear();
        inv.getItems().addAll(updated.getItems());

        inv.setStatus(InvoiceStatus.PRE_LEDGER);

        Invoice saved = invoiceRepo.save(inv);

        String invoiceId = inv2.getId();
        
        if (invoiceId != null && !invoiceId.trim().isEmpty()) {

            List<LedgerEntry> entries2 =
                ledgerRepo.findByInvoiceNumberAndLedgerTypeAndUserId(
                    inv2.getInvoiceNumber(),
                    inv2.getType().name(),
                    inv2.getUserId()
                );

            if (!entries2.isEmpty()) {
                ledgerRepo.deleteAll(entries2);
            }
        }        
        
        service.generatePreLedgerEntries(saved);

        return ResponseEntity.ok(saved);
    }
    
    @PutMapping("/invoices/accounts/{id}")
    @Transactional
    public ResponseEntity<Invoice> updateInvoiceAccounts(
            @PathVariable String id,
            @RequestBody Invoice updated) {

        Invoice inv3 = invoiceRepo.findById(id).orElseThrow();        
        
        for (Invoice inv : invoiceRepo.findAll()) {
            if (inv.getAccount().equals(inv3.getAccount()) && inv.getDescription().equals(inv3.getDescription()) && inv.getOib().equals(inv3.getOib()) && inv.getCity().equals(inv3.getCity()) && inv.getUserId().equals(inv3.getUserId())) {
                List<Invoice> listInv = invoiceRepo.findByInvoiceNumberAndUserIdAndType(inv.getInvoiceNumber(), inv.getUserId(), inv.getType());
                for (Invoice inv2 : listInv) {
                    inv2.setDescription(updated.getDescription());
                    inv2.setOib(updated.getOib());
                    inv2.setCity(updated.getCity());
                }
                invoiceRepo.saveAll(listInv);    
            }
        }

        return ResponseEntity.ok(updated);
    }    
    
    @PutMapping("/giroaccounts/accounts/{id}")
    @Transactional
    public ResponseEntity<GiroAccount> updateGiroAccountAccounts(
            @PathVariable String id,
            @RequestBody GiroAccount updated) {

        GiroAccount giro3 = giroAccountRepo.findById(id).orElseThrow();        
        
        for (GiroAccount giro : giroAccountRepo.findAll()) {
            if (giro.getOutgoingAccount().equals(giro3.getOutgoingAccount()) && giro.getDescription().equals(giro3.getDescription()) && giro.getUserId().equals(giro3.getUserId())) {
                List<GiroAccount> listGiro = giroAccountRepo.findByInvoiceNumberAndDescriptionAndUserId(giro.getInvoiceNumber(), giro.getDescription(), giro.getUserId());
                for (GiroAccount giro2 : listGiro) {
                    giro2.setDescription(updated.getDescription());
                }
                giroAccountRepo.saveAll(listGiro);
            }
        }            

        return ResponseEntity.ok(updated);
    }        
    
    @PutMapping("/ledger/accounts/{id}")
    @Transactional
    public ResponseEntity<LedgerEntry> updateLedgerEntryAccounts(
            @PathVariable String id,
            @RequestBody LedgerEntry updated) {

        LedgerEntry ledger3 = ledgerRepo.findById(id).orElseThrow();        
        
        for (LedgerEntry ledger : ledgerRepo.findAll()) {
            if (ledger.getAccountCode().equals(ledger3.getAccountCode()) && ledger.getDescription().equals(ledger3.getDescription()) && ledger.getUserId().equals(ledger3.getUserId())) {
                List<LedgerEntry> listLedger = ledgerRepo.findByInvoiceNumberAndDescriptionAndUserId(ledger.getInvoiceNumber(), ledger.getDescription(), ledger.getUserId());
                for (LedgerEntry ledger2 : listLedger) {
                    ledger2.setDescription(updated.getDescription());
                }
                ledgerRepo.saveAll(listLedger);
            }
        }            

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/ledger")
    public List<LedgerEntry> ledger() {
        return service.getAllEntries();
    }

    @GetMapping("/ledger/balance")
    public double balance(@RequestParam String code, @RequestParam int year) {
        return service.getBalance(code, year);
    }

    @PostMapping("/ledger/manual")
    public LedgerEntry manual(@RequestBody LedgerEntry e) {
        return service.addManual(e);
    }
    
    @PostMapping("/ledger/all/batch")
    public List<LedgerEntry> addLedger(@RequestBody List<LedgerEntry> list) {
        return ledgerRepo.saveAll(list);
    }                        
    
    @GetMapping("/ledger/pre")
    public List<LedgerEntry> getPre() {
        return service.getPreLedger();
    }

    @GetMapping("/ledger/main")
    public List<LedgerEntry> getMain() {
        return service.getMainLedger();
    }    

    @PostMapping("/ledger/post")
    public void postToMainLedger(@RequestParam String date) {
        service.postToMainLedger(date);
    }

    @PutMapping("/accounts/{id}")
    public Account updateAccount(@PathVariable String id, @RequestBody Account acc) {
        acc.setId(id);
        
        Account acc2 = accountRepo.findById(id).get();
        
        if (acc.getCode().startsWith("120") && acc.getCode().length()<=4 ||
            acc.getCode().startsWith("121") && acc.getCode().length()<=4 ||    
            acc.getCode().startsWith("220") && acc.getCode().length()<=4 ||
            acc.getCode().startsWith("221") && acc.getCode().length()<=4) {
            
        } else {
            if (acc.getCode().startsWith("120") || acc.getCode().startsWith("121") || acc.getCode().startsWith("220") || acc.getCode().startsWith("121")) {
                for (Invoice inv : invoiceRepo.findAll()) {
                    if (inv.getAccount().equals(acc2.getCode())) {
                        List<Invoice> listInv = invoiceRepo.findByInvoiceNumberAndUserIdAndType(inv.getInvoiceNumber(), inv.getUserId(), inv.getType());
                        for (Invoice inv2 : listInv) {
                            if (inv2.getAccount().equals(acc2.getCode())) {
                                inv2.setAccount(acc.getCode());
                            }
                            inv2.setDescription(acc.getName());
                            inv2.setOib(acc.getOib());
                            inv2.setCity(acc.getGrad());
                        }
                        invoiceRepo.saveAll(listInv);
                    }
                }
                for (GiroAccount giro : giroAccountRepo.findAll()) {
                    if (giro.getOutgoingAccount().equals(acc2.getCode())) {
                        List<GiroAccount> listGiro = giroAccountRepo.findByInvoiceNumberAndDescriptionAndUserId(giro.getInvoiceNumber(), giro.getDescription(), giro.getUserId());
                        for (GiroAccount giro2 : listGiro) {
                            if (giro2.getOutgoingAccount().equals(acc2.getCode())) {
                                giro2.setOutgoingAccount(acc.getCode());
                            }
                            giro2.setDescription(acc.getName());
                        }
                        giroAccountRepo.saveAll(listGiro);
                    }
                }            
                for (LedgerEntry ledger : ledgerRepo.findAll()) {
                    if (ledger.getAccountCode().equals(acc2.getCode())) {
                        List<LedgerEntry> listLedger = ledgerRepo.findByInvoiceNumberAndLedgerTypeAndUserId(ledger.getInvoiceNumber(), ledger.getLedgerType(), ledger.getUserId());
                        for (LedgerEntry ledger2 : listLedger) {
                            if (ledger2.getAccountCode().equals(acc2.getCode())) {
                                ledger2.setAccountCode(acc.getCode());
                            }
                            ledger2.setDescription(acc.getName());
                        }
                        ledgerRepo.saveAll(listLedger);
                    }
                }                        
            } else 
            {
                for (Invoice inv : invoiceRepo.findAll()) {
                    if (inv.getAccount().equals(acc2.getCode())) {
                        List<Invoice> listInv = invoiceRepo.findByInvoiceNumberAndUserIdAndType(inv.getInvoiceNumber(), inv.getUserId(), inv.getType());
                        for (Invoice inv2 : listInv) {
                            if (inv2.getAccount().equals(acc2.getCode())) {
                                inv2.setAccount(acc.getCode());
                            }
                        }
                        invoiceRepo.saveAll(listInv);
                    }
                }
                for (GiroAccount giro : giroAccountRepo.findAll()) {
                    if (giro.getOutgoingAccount().equals(acc2.getCode())) {
                        List<GiroAccount> listGiro = giroAccountRepo.findByInvoiceNumberAndDescriptionAndUserId(giro.getInvoiceNumber(), giro.getDescription(), giro.getUserId());
                        for (GiroAccount giro2 : listGiro) {
                            if (giro2.getOutgoingAccount().equals(acc2.getCode())) {
                                giro2.setOutgoingAccount(acc.getCode());
                            }
                        }
                        giroAccountRepo.saveAll(listGiro);
                    }
                }            
                for (LedgerEntry ledger : ledgerRepo.findAll()) {
                    if (ledger.getAccountCode().equals(acc2.getCode())) {
                        List<LedgerEntry> listLedger = ledgerRepo.findByInvoiceNumberAndLedgerTypeAndUserId(ledger.getInvoiceNumber(), ledger.getLedgerType(), ledger.getUserId());
                        for (LedgerEntry ledger2 : listLedger) {
                            if (ledger2.getAccountCode().equals(acc2.getCode())) {
                                ledger2.setAccountCode(acc.getCode());
                            }
                        }
                        ledgerRepo.saveAll(listLedger);
                    }
                }                        
            }        
        }
        
        return accountRepo.save(acc);
    }

    @DeleteMapping("/accounts/{id}")
    public void deleteAccount(@PathVariable String id) {
        accountRepo.deleteById(id);
    }
    
    @PostMapping("/aop/all/batch")
    public List<AOP> addAOP(@RequestBody List<AOP> list) {
        return aopRepo.saveAll(list);
    }                    
    
    @PutMapping("/aop/{id}")
    public AOP updateAOP(@PathVariable String id, @RequestBody AOP aop) {
        aop.setId(id);
        return aopRepo.save(aop);
    }

    @DeleteMapping("/aop/{id}")
    public void deleteAOP(@PathVariable String id) {
        aopRepo.deleteById(id);
    }

    @PutMapping("/partners/{id}")
    public Partner updatePartner(@PathVariable String id, @RequestBody Partner p) {
        p.setId(id);
        return partnerRepo.save(p);
    }

    @DeleteMapping("/partners/{id}")
    public void deletePartner(@PathVariable String id) {
        partnerRepo.deleteById(id);
    }    
    
    @PutMapping("/ledger/{id}")
    public LedgerEntry updateLedger(@PathVariable String id, @RequestBody LedgerEntry e) {
        e.setId(id);
        return ledgerRepo.save(e);
    }

    @DeleteMapping("/ledger/{id}")
    public void deleteLedger(@PathVariable String id) {
        ledgerRepo.deleteById(id);
    }

    @GetMapping("/item-accounts")
    public List<ItemAccount> getItemAccounts() {
        return itemAccountRepo.findAll();
    }

    @PostMapping("/item-accounts")
    public ItemAccount addItemAccount(@RequestBody ItemAccount item) {
        return itemAccountRepo.save(item);
    }

    @PutMapping("/item-accounts/{id}")
    public ItemAccount updateItemAccount(@PathVariable String id, @RequestBody ItemAccount item) {
        item.setId(id);
        return itemAccountRepo.save(item);
    }

    @DeleteMapping("/item-accounts/{id}")
    public void deleteItemAccount(@PathVariable String id) {
        itemAccountRepo.deleteById(id);
    }
    
    @PostMapping("/tax-rates")
    public TaxRate addTaxRate(@RequestBody TaxRate item) {
        return taxRepo.save(item);
    }    
    
    @PutMapping("/tax-rates/{id}")
    public TaxRate updateTaxRate(@PathVariable String id, @RequestBody TaxRate t) {
        t.setId(id);
        return taxRepo.save(t);
    }

    @DeleteMapping("/tax-rates/{id}")
    public void deleteTaxRate(@PathVariable String id) {
        taxRepo.deleteById(id);
    }    

    @PutMapping("/ledger/batch")
    public List<LedgerEntry> updateBatch(@RequestBody List<LedgerEntry> entries) {

        // 1. safety check
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        // 2. uzmi ključ grupiranja (invoiceNumber + date)
        String invoiceNumber = entries.get(0).getInvoiceNumber();
        String date = entries.get(0).getDate();
        String type = entries.get(0).getLedgerType();
        String userId = entries.get(0).getUserId();

        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new IllegalArgumentException("invoiceNumber je null ili prazan");
        }

        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("date je null ili prazan");
        }

        // 3. nađi postojeće stavke po invoice + date
        List<LedgerEntry> existing = ledgerRepo.findByInvoiceNumberAndLedgerTypeAndUserId(invoiceNumber, type, userId);

        // 4. ako ne postoji ništa → samo spremi
        if (existing == null || existing.isEmpty()) {
            return ledgerRepo.saveAll(entries);
        }

        // 5. uzmi status iz prve postojeće stavke
        InvoiceStatus status = existing.get(0).getStatus();

        // 6. obriši stare stavke za taj invoice
        ledgerRepo.deleteAll(existing);

        // 7. postavi status na nove stavke
        for (LedgerEntry entry : entries) {
            InvoiceStatus finalStatus = (status != null)
                    ? status
                    : InvoiceStatus.PRE_LEDGER;

            entry.setStatus(finalStatus);
        }

        // 8. spremi nove
        return ledgerRepo.saveAll(entries);
    }
    
    @PostMapping("/ledger/transfer")
    public ResponseEntity<?> transfer(
            @RequestParam String invoiceNumber,
            @RequestParam String ledgerType,
            @RequestParam String userId
    ) {
        service.transferToMain(invoiceNumber, ledgerType, userId);
        return ResponseEntity.ok().build();
    }    
    
    @PostMapping("/giroaccounts")
    public GiroAccount create(@RequestBody GiroAccount giro) {
        
        List<GiroAccount> listGiro = giroAccountRepo.findByUserIdAndName(giro.getUserId(), giro.getName());
        
        for (GiroAccount giroAccount: listGiro) {
            giroAccount.setOpeningPayment(giro.getOpeningPayment());
            giroAccount.setOpeningDeposit(giro.getOpeningDeposit());
        }
        
        giroAccountRepo.saveAll(listGiro);
        
        return service.save(giro);
    }

    @GetMapping("/giroaccounts")
    public List<GiroAccount> getAll() {
        return service.findAll();
    }    
    
    @PostMapping("/giroaccounts/all/batch")
    public List<GiroAccount> addIGiroAccountsAll(@RequestBody List<GiroAccount> list) {
        return giroAccountRepo.saveAll(list);
    }                            
    
    @PutMapping("/giroaccounts/{id}")
    public GiroAccount update(@PathVariable String id, @RequestBody GiroAccount g) {
        g.setId(id);
        g.setStatus(InvoiceStatus.PRE_LEDGER);
        
        List<GiroAccount> listGiro = giroAccountRepo.findByUserIdAndName(g.getUserId(), g.getName());
        
        for (GiroAccount giroAccount: listGiro) {
            giroAccount.setOpeningPayment(g.getOpeningPayment());
            giroAccount.setOpeningDeposit(g.getOpeningDeposit());
        }
        
        giroAccountRepo.saveAll(listGiro);
        
        return service.save(g);
    }

    @DeleteMapping("/giroaccounts/{id}")
    public void delete(@PathVariable String id) {
        
        GiroAccount giro = giroAccountRepo.findById(id).orElse(null);
        
        List<LedgerEntry> preEntries =
                ledgerRepo.findByReferenceId(
                        giro.getId()
                );
        
        ledgerRepo.deleteAll(preEntries);        
        
        service.delete(id);
    }    

    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable String id) {

        // obriši ledger entries
        List<LedgerEntry> entries = ledgerRepo.findByReferenceId(id);
        ledgerRepo.deleteAll(entries);

        Invoice inv = invoiceRepo.findById(id).orElseThrow();

        inv.getItems().clear();

        invoiceRepo.save(inv);

        invoiceRepo.delete(inv);

        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/warehouse")
    public List<WarehouseItem> getWarehouseItemAll() {
        return warehouseItemRepo.findAll();
    }

    @PostMapping("/warehouse")
    public WarehouseItem addWarehouseItem(@RequestBody WarehouseItem w) {
        return warehouseItemRepo.save(w);
    }

    @PutMapping("/warehouse/{id}")
    public WarehouseItem updateWarehouseItem(@PathVariable String id, @RequestBody WarehouseItem w) {
        w.setId(id);
        return warehouseItemRepo.save(w);
    }

    @DeleteMapping("/warehouse/{id}")
    public void deleteWarehouseItem(@PathVariable String id) {
        warehouseItemRepo.deleteById(id);
    }

    @GetMapping("/account-classes")
    public List<AccountClass> getAccountClasses() {
        return accountClassRepo.findAll();
    }

    @PostMapping("/account-classes")
    public AccountClass addAccountClass(@RequestBody AccountClass c) {
        return accountClassRepo.save(c);
    }
    
    @PostMapping("/account-classes/batch")
    public List<AccountClass> addAccountClasses(@RequestBody List<AccountClass> list) {
        return accountClassRepo.saveAll(list);
    }    

    @PutMapping("/account-classes/{id}")
    public AccountClass updateAccountClass(@PathVariable String id, @RequestBody AccountClass c) {
        c.setId(id);
        return accountClassRepo.save(c);
    }

    @DeleteMapping("/account-classes/{id}")
    public void deleteAccountClass(@PathVariable String id) {
        accountClassRepo.deleteById(id);
    }    
   
    @GetMapping("/account-groups")
    public List<AccountGroup> getAccountGroups() {
        return accountGroupRepo.findAll();
    }

    @PostMapping("/account-groups")
    public AccountGroup addAccountGroup(@RequestBody AccountGroup g) {
        return accountGroupRepo.save(g);
    }
    
    @PostMapping("/account-groups/batch")
    public List<AccountGroup> addAccountGroups(@RequestBody List<AccountGroup> list) {
        return accountGroupRepo.saveAll(list);
    }    

    @PutMapping("/account-groups/{id}")
    public AccountGroup updateAccountGroup(@PathVariable String id, @RequestBody AccountGroup g) {
        g.setId(id);
        return accountGroupRepo.save(g);
    }

    @DeleteMapping("/account-groups/{id}")
    public void deleteAccountGroup(@PathVariable String id) {
        accountGroupRepo.deleteById(id);
    }
    
    @GetMapping("/account-synthetics")
    public List<AccountSynthetic> getAccountSynthetics() {
        return accountSyntheticRepo.findAll();
    }

    @PostMapping("/account-synthetics")
    public AccountSynthetic addAccountSynthetic(@RequestBody AccountSynthetic s) {
        return accountSyntheticRepo.save(s);
    }
    
    @PostMapping("/account-synthetics/batch")
    public List<AccountSynthetic> addAccountSyntetics(@RequestBody List<AccountSynthetic> list) {
        return accountSyntheticRepo.saveAll(list);
    }        

    @PutMapping("/account-synthetics/{id}")
    public AccountSynthetic updateAccountSynthetic(@PathVariable String id, @RequestBody AccountSynthetic s) {
        s.setId(id);
        return accountSyntheticRepo.save(s);
    }

    @DeleteMapping("/account-synthetics/{id}")
    public void deleteAccountSynthetic(@PathVariable String id) {
        accountSyntheticRepo.deleteById(id);
    }    
    
    @GetMapping("/account-analyticals")
    public List<AccountAnalytical> getAccountAnalyticals() {
        return accountAnayticalRepo.findAll();
    }

    @PostMapping("/account-analyticals")
    public AccountAnalytical addAccountAnalytical(@RequestBody AccountAnalytical a) {
        return accountAnayticalRepo.save(a);
    }
    
    @PostMapping("/account-analyticals/batch")
    public List<AccountAnalytical> addAccountAnalyticals(@RequestBody List<AccountAnalytical> list) {
        return accountAnayticalRepo.saveAll(list);
    }            

    @PutMapping("/account-analyticals/{id}")
    public AccountAnalytical updateAccountAnalytical(@PathVariable String id, @RequestBody AccountAnalytical a) {
        a.setId(id);
        return accountAnayticalRepo.save(a);
    }

    @DeleteMapping("/account-analyticals/{id}")
    public void deleteAccountAnalytical(@PathVariable String id) {
        accountAnayticalRepo.deleteById(id);
    }    
    
    @GetMapping("/account-sub-analyticals")
    public List<AccountSubAnalytical> getAccountSubAnalyticals() {
        return accountSubAnalyticalRepo.findAll();
    }

    @PostMapping("/account-sub-analyticals")
    public AccountSubAnalytical addAccountSubAnalytical(@RequestBody AccountSubAnalytical s) {
        return accountSubAnalyticalRepo.save(s);
    }
    
    @PostMapping("/account-sub-analyticals/batch")
    public List<AccountSubAnalytical> addAccountSubAnalyticals(@RequestBody List<AccountSubAnalytical> list) {
        return accountSubAnalyticalRepo.saveAll(list);
    }                

    @PutMapping("/account-sub-analyticals/{id}")
    public AccountSubAnalytical updateAccountSubAnalytical(@PathVariable String id, @RequestBody AccountSubAnalytical s) {
        s.setId(id);
        return accountSubAnalyticalRepo.save(s);
    }

    @DeleteMapping("/account-sub-analyticals/{id}")
    public void deleteAccountSubAnalytical(@PathVariable String id) {
        accountSubAnalyticalRepo.deleteById(id);
    }    
    
    @PostMapping("/warehouse/all/batch")
    public List<WarehouseItem> addWarehouseItemBatch(@RequestBody List<WarehouseItem> list) {
        return warehouseItemRepo.saveAll(list);
    }                    
    
    @DeleteMapping("/ledger/reference/{id}")
    public void deleteByReferenceId(@PathVariable String id) {

        LedgerEntry ledger =
            ledgerRepo.findById(id).orElseThrow();
        
        List<LedgerEntry> entries = ledgerRepo.findByInvoiceNumberAndLedgerType(ledger.getInvoiceNumber(), ledger.getLedgerType());

        ledgerRepo.deleteAll(entries);
    }
}