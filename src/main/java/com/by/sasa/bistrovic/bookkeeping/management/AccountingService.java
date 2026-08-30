package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AccountingService {

    private final InvoiceRepository invoiceRepo;
    private final LedgerEntryRepository ledgerRepo;
    private final PartnerRepository partnerRepo;
    private final ItemAccountRepository itemAccountRepo;
    private final TaxRateRepository taxRepo;
    private final AOPRepository aopRepo;
    private final GiroAccountRepository giroAccountRepo;
    private final AccountRepository accountRepo;

    public AccountingService(InvoiceRepository invoiceRepo,
                             LedgerEntryRepository ledgerRepo,
                             PartnerRepository partnerRepo,
                             ItemAccountRepository itemAccountRepo,
                             TaxRateRepository taxRepo,
                             AOPRepository aopRepo,
                             GiroAccountRepository giroAccountRepo,
                             AccountRepository accountRepo) {
        this.invoiceRepo = invoiceRepo;
        this.ledgerRepo = ledgerRepo;
        this.partnerRepo = partnerRepo;
        this.itemAccountRepo = itemAccountRepo;
        this.taxRepo = taxRepo;
        this.aopRepo = aopRepo;
        this.giroAccountRepo = giroAccountRepo;
        this.accountRepo = accountRepo;
    }

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Invoice createInvoice(Invoice inv) {
        generatePreLedgerEntries(inv);
        Invoice saved = invoiceRepo.save(inv);
        return saved;
    }

    public void generatePreLedgerEntries(Invoice inv) {
        List<LedgerEntry> entries = new ArrayList<>();
        int year = LocalDate.parse(inv.getDate()).getYear();
        
        String invoiceId = inv.getId();
                
        if (invoiceId != null && !invoiceId.trim().isEmpty()) {

            Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

            List<LedgerEntry> entries2 =
                ledgerRepo.findByInvoiceNumberAndLedgerTypeAndUserId(
                    invoice.getInvoiceNumber(),
                    invoice.getType().name(),
                    invoice.getUserId()
                );

            if (!entries2.isEmpty()) {
                ledgerRepo.deleteAll(entries2);
            }
        }

        if (inv.getType().name().startsWith("URA")) {

            entries.add(createEntry(inv, year,
                    inv.getAccount(),
                    0, inv.getTotalAmount(),
                    inv.getDescription()
            ));

            inv.getItems().forEach(item -> {
                TaxRate tax = taxRepo.findById(item.getTaxRateId()).orElse(null);
                
                //ItemAccount itemAccount = itemAccountRepo.findByName(item.getDescription());

                entries.add(createEntry(inv, year,
                        item.getBaseAccount(),
                        item.getBaseAmount(), 0,
                        inv.getDescription()
                ));

                if (item.getTaxAmount() > 0 && tax != null) {
                    entries.add(createEntry(inv, year,
                            item.getTaxAccount(),
                            item.getTaxAmount(), 0,
                            inv.getDescription()
                    ));
                }
            });

        } else {

            entries.add(createEntry(inv, year,
                    inv.getAccount(),
                    inv.getTotalAmount(), 0,
                    inv.getDescription()
            ));

            inv.getItems().forEach(item -> {
                TaxRate tax = taxRepo.findById(item.getTaxRateId()).orElse(null);
                
                //ItemAccount itemAccount = itemAccountRepo.findByName(item.getDescription());

                entries.add(createEntry(inv, year,
                        item.getBaseAccount(),
                        0, item.getBaseAmount(),
                        inv.getDescription()
                ));

                if (item.getTaxAmount() > 0 && tax != null) {
                    entries.add(createEntry(inv, year,
                            item.getTaxAccount(),
                            0, item.getTaxAmount(),
                            inv.getDescription()
                    ));
                }
            });
        }

        ledgerRepo.saveAll(entries);
    }

    private LedgerEntry createEntry(Invoice inv, int year, String account,
                                   double debit, double credit, String desc) {

        LedgerEntry e = new LedgerEntry();
        e.setId(UUID.randomUUID().toString());
        e.setDate(inv.getDate());
        e.setYear(year);
        e.setAccountCode(account);
        e.setDebit(debit);
        e.setCredit(credit);
        e.setDescription(desc);
        e.setPartnerId(inv.getPartnerId());
        e.setPosted(false);
        e.setStatus(InvoiceStatus.PRE_LEDGER);
        e.setLedgerType(inv.getType().name());
        e.setReferenceId(inv.getId());
        e.setInvoiceNumber(inv.getInvoiceNumber());
        e.setUserId(inv.getUserId());
        
        return e;
    }

    // ---------------- LEDGER ----------------

    public List<LedgerEntry> getAllEntries() {
        return ledgerRepo.findAll();
    }

    public double getBalance(String code, int year) {
        List<LedgerEntry> entries = ledgerRepo.findAll();

        Account acc = accountRepo.findByCode(code);
        if (acc == null) return 0;

        double debit = entries.stream()
                .filter(e -> e.getAccountCode().equals(code))
                .filter(e -> e.getYear() <= year)
                .filter(e -> e.getStatus() == InvoiceStatus.MAIN_LEDGER)
                .mapToDouble(LedgerEntry::getDebit)
                .sum();

        double credit = entries.stream()
                .filter(e -> e.getAccountCode().equals(code))
                .filter(e -> e.getYear() <= year)
                .filter(e -> e.getStatus() == InvoiceStatus.MAIN_LEDGER)
                .mapToDouble(LedgerEntry::getCredit)
                .sum();

        if (acc.getType().name().equals("AKTIVA") || acc.getType().name().equals("RASHOD")) {
            return debit - credit;
        } else {
            return credit - debit;
        }
    }

    public LedgerEntry addManual(LedgerEntry e) {
        return ledgerRepo.save(e);
    }
    
    @Transactional
    public void postToMainLedger(String untilDate) {

        LocalDate until = LocalDate.parse(untilDate);

        List<LedgerEntry> preEntries = ledgerRepo.findByPostedFalse()
                .stream()
                .filter(e -> LocalDate.parse(e.getDate()).isBefore(until.plusDays(1)))
                .toList();

        for (LedgerEntry e : preEntries) {
            e.setStatus(InvoiceStatus.MAIN_LEDGER);
            e.setPosted(true);
        }

        ledgerRepo.saveAll(preEntries);

        List<Invoice> invoices = invoiceRepo.findAll();

        for (Invoice invoice : invoices) {
            invoice.setStatus(InvoiceStatus.MAIN_LEDGER);
        }

        invoiceRepo.saveAll(invoices);
    }
    
    public List<LedgerEntry> getPreLedger() {
        List<LedgerEntry> ledgerEntryList = ledgerRepo.findAll();
        List<LedgerEntry> ledgerEntryNew = new ArrayList<>();

        for (LedgerEntry ledgerEntry : ledgerEntryList) {
            if (ledgerEntry.getStatus() == InvoiceStatus.PRE_LEDGER) {
                ledgerEntryNew.add(ledgerEntry);
            }
        }

        return ledgerEntryNew;
    }

    public List<LedgerEntry> getMainLedger() {
        List<LedgerEntry> ledgerEntryList = ledgerRepo.findAll();
        List<LedgerEntry> ledgerEntryNew = new ArrayList<>();

        for (LedgerEntry ledgerEntry : ledgerEntryList) {
            if (ledgerEntry.getStatus() == InvoiceStatus.MAIN_LEDGER) {
                ledgerEntryNew.add(ledgerEntry);
            }
        }

        return ledgerEntryNew;
    }
    
    @Transactional
    public void transferToMain(String invoiceNumber, String ledgerType, String userId) {

        List<LedgerEntry> preEntries =
                ledgerRepo.findByInvoiceNumberAndLedgerTypeAndUserId(
                        invoiceNumber,
                        ledgerType,
                        userId
                );

        List<LedgerEntry> mainEntries = preEntries.stream()
                .map(e -> {
                    LedgerEntry m = new LedgerEntry();

                    m.setAccountCode(e.getAccountCode());
                    m.setDebit(e.getDebit());
                    m.setCredit(e.getCredit());
                    m.setDescription(e.getDescription());

                    m.setDate(e.getDate());
                    m.setInvoiceNumber(e.getInvoiceNumber());
                    m.setPartnerId(e.getPartnerId());

                    m.setStatus(InvoiceStatus.MAIN_LEDGER);
                    
                    m.setPosted(true);
                    
                    m.setYear(e.getYear());
                    
                    m.setReferenceId(e.getReferenceId());
                    
                    m.setLedgerType(e.getLedgerType());
                    
                    m.setUserId(e.getUserId());
                    
                    if (e.getLedgerType().contains("URA") || e.getLedgerType().contains("IRA")) {
                        Invoice invoice = invoiceRepo.findByInvoiceNumber(e.getInvoiceNumber());
                        
                        invoice.setStatus(InvoiceStatus.MAIN_LEDGER);
                        
                        invoiceRepo.save(invoice);
                    }
                    
                    if (e.getLedgerType().contains("ŽIRORAČUN")) {
                        GiroAccount giroAccount = giroAccountRepo.findByInvoiceNumber(e.getInvoiceNumber());
                        
                        giroAccount.setStatus(InvoiceStatus.MAIN_LEDGER);
                        
                        giroAccountRepo.save(giroAccount);
                    }

                    return m;
                })
                .toList();

        ledgerRepo.saveAll(mainEntries);

        // opcionalno: obriši iz PRE
        ledgerRepo.deleteAll(preEntries);
    }    
    
    public GiroAccount save(GiroAccount g) {
        
        GiroAccount giro = giroAccountRepo.findById(g.getId()).orElse(null);
        
        String oldInvoiceNumber = giro.getInvoiceNumber();
        String oldUserId = giro.getUserId();
        
        GiroAccount savedGiroAccount = giroAccountRepo.save(g);
        
        if (giro!=null) {        
            List<LedgerEntry> preEntries =
                    ledgerRepo.findByInvoiceNumberAndLedgerTypeAndUserId(oldInvoiceNumber, "ŽIRORAČUN", oldUserId);

            ledgerRepo.deleteAll(preEntries);        
        }
        
        LedgerEntry e1 = new LedgerEntry();
        e1.setId(UUID.randomUUID().toString());
        e1.setDate(g.getDate());
        LocalDate d = LocalDate.parse(e1.getDate());
        int year = d.getYear();
        e1.setYear(year);
        e1.setAccountCode(g.getOutgoingAccount());
        if (g.getAccountType().equals("Izlazni")) {
            e1.setDebit(g.getTotalAmount());
            e1.setCredit(0);
        } else {
            e1.setDebit(0);
            e1.setCredit(g.getTotalAmount());            
        }
        e1.setPartnerId(g.getPartnerId());
        e1.setPosted(false);
        e1.setStatus(InvoiceStatus.PRE_LEDGER);
        e1.setLedgerType("ŽIRORAČUN");
        e1.setInvoiceNumber(g.getInvoiceNumber());        
        e1.setUserId(g.getUserId());                
        e1.setDescription(g.getDescription());                
        e1.setReferenceId(savedGiroAccount.getId());                
        
        LedgerEntry e2 = new LedgerEntry();
        e2.setId(UUID.randomUUID().toString());
        e2.setDate(g.getDate());
        e2.setYear(year);
        e2.setAccountCode(g.getInputAccount());
        if (g.getAccountType().equals("Izlazni")) {
            e2.setDebit(0);
            e2.setCredit(g.getTotalAmount());
        } else {
            e2.setDebit(g.getTotalAmount());
            e2.setCredit(0);            
        }
        e2.setPartnerId(g.getPartnerId());
        e2.setPosted(false);
        e2.setStatus(InvoiceStatus.PRE_LEDGER);
        e2.setLedgerType("ŽIRORAČUN");
        e2.setInvoiceNumber(g.getInvoiceNumber());                
        e2.setDescription(g.getDescription());                
        e2.setUserId(g.getUserId());                
        e2.setReferenceId(savedGiroAccount.getId());                
        
        ledgerRepo.save(e1);
        ledgerRepo.save(e2);
        
        return savedGiroAccount;
    }

    public List<GiroAccount> findAll() {
        return giroAccountRepo.findAll();
    }  
    
    public void delete(String id) {
        giroAccountRepo.deleteById(id);
    }    
    
    @Transactional
    public void mapBalances(String userId, Integer aopListYear) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId ne smije biti prazan.");
        }

        if (aopListYear == null) {
            throw new IllegalArgumentException("aopListYear ne smije biti null.");
        }

        int previousYear = aopListYear - 1;
        
        // Postavljanje raspona tako da prihvaća i "YYYY-MM-DD" i "YYYY-MM-DD HH:mm:ss"
        String fromDate = previousYear + "-01-01";
        String toDate = previousYear + "-12-31 23:59:59";

        // 1. Učitaj sve ledger zapise za prethodnu godinu
        List<LedgerEntry> ledgerEntries = ledgerRepo.findByUserIdAndDateBetween(
                userId,
                fromDate,
                toDate
        );

        // 2. Učitaj račune za prethodnu godinu
        List<Account> previousYearAccounts = accountRepo.findByUserIdAndAopListYear(
                userId,
                previousYear
        );

        if (previousYearAccounts.isEmpty()) {
            previousYearAccounts = accountRepo.findByUserIdAndAopListYearIsNull(userId);
        }

        // Izračun ukupnih prihoda i rashoda iz prethodne godine (saldo računa + ledger unosi)
        double ukupnoPrihodiOdPrethodneGodine = previousYearAccounts.stream()
                .filter(acc -> isType(acc, "PRIHOD"))
                .mapToDouble(acc -> {
                    double ledgerCreditSum = ledgerEntries.stream()
                            .filter(e -> acc.getCode() != null && acc.getCode().equals(e.getAccountCode()))
                            .mapToDouble(LedgerEntry::getCredit)
                            .sum();
                    return acc.getBalanceCredit() + ledgerCreditSum;
                })
                .sum();

        double ukupnoRashodiOdPrethodneGodine = previousYearAccounts.stream()
                .filter(acc -> isType(acc, "RASHOD"))
                .mapToDouble(acc -> {
                    double ledgerDebitSum = ledgerEntries.stream()
                            .filter(e -> acc.getCode() != null && acc.getCode().equals(e.getAccountCode()))
                            .mapToDouble(LedgerEntry::getDebit)
                            .sum();
                    return acc.getBalanceDebit() + ledgerDebitSum;
                })
                .sum();

        // 3. Prolaz kroz račune iz prethodne godine
        for (Account previousAccount : previousYearAccounts) {

            String accountCode = previousAccount.getCode();

            if (accountCode == null || accountCode.isBlank()) {
                continue;
            }

            // Izračunaj sume potražuje/duguje iz ledgera direktno za trenutni accountCode
            double ledgerDebit = ledgerEntries.stream()
                    .filter(e -> accountCode.equals(e.getAccountCode()))
                    .mapToDouble(LedgerEntry::getDebit)
                    .sum();

            double ledgerCredit = ledgerEntries.stream()
                    .filter(e -> accountCode.equals(e.getAccountCode()))
                    .mapToDouble(LedgerEntry::getCredit)
                    .sum();

            // Pronađi ili kreiraj račun za ciljnu AOP godinu
            Account targetAccount = accountRepo.findByUserIdAndCodeAndAopListYear(userId, accountCode, aopListYear)
                    .orElseGet(() -> {
                        Account newAcc = new Account();
                        newAcc.setUserId(userId);
                        newAcc.setCode(previousAccount.getCode());
                        newAcc.setName(previousAccount.getName());
                        newAcc.setType(previousAccount.getType());
                        newAcc.setAopCode(previousAccount.getAopCode());
                        newAcc.setAopName(previousAccount.getAopName());
                        newAcc.setAopListCode(previousAccount.getAopListCode());
                        newAcc.setAopListName(previousAccount.getAopListName());
                        newAcc.setDescription(previousAccount.getDescription());
                        newAcc.setOpis(previousAccount.getOpis());
                        newAcc.setOib(previousAccount.getOib());
                        newAcc.setGrad(previousAccount.getGrad());
                        newAcc.setAopListYear(aopListYear);
                        return newAcc;
                    });

            // Aktiva
            if (isType(previousAccount, "AKTIVA")) {
                double previousBalanceDebit = previousAccount.getBalanceDebit();
                double previousBalanceCredit = previousAccount.getBalanceCredit();

                double newBalanceDebit = (previousBalanceDebit - previousBalanceCredit) + (ledgerDebit - ledgerCredit);

                targetAccount.setBalanceDebit(newBalanceDebit);
                targetAccount.setBalanceCredit(0.0);
            }
            // Pasiva
            else if (isType(previousAccount, "PASIVA")) {
                double previousBalanceCredit = previousAccount.getBalanceCredit();
                double previousBalanceDebit = previousAccount.getBalanceDebit();

                double newBalanceCredit;

                // Posebna logika za konto 8040
                if ("8040".equals(accountCode)) {
                    newBalanceCredit = (previousBalanceCredit - previousBalanceDebit) 
                             + (ledgerCredit - ledgerDebit)
                             + (ukupnoPrihodiOdPrethodneGodine - ukupnoRashodiOdPrethodneGodine);
                } else {
                    newBalanceCredit = (previousBalanceCredit - previousBalanceDebit) + (ledgerCredit - ledgerDebit);
                }

                targetAccount.setBalanceCredit(newBalanceCredit);
                targetAccount.setBalanceDebit(0.0);
            }
            // Rashod / Prihod
            else if (isType(previousAccount, "RASHOD") || isType(previousAccount, "PRIHOD")) {
                targetAccount.setBalanceDebit(0.0);
                targetAccount.setBalanceCredit(0.0);
            }

            // Spremanje u bazu
            accountRepo.save(targetAccount);
        }
    }

    /**
     * Provjera tipa Account-a.
     */
    private boolean isType(
            Account account,
            String expectedType) {

        if (account == null) {
            return false;
        }

        if (account.getType() == null) {
            return false;
        }

        return account.getType()
                .name()
                .equalsIgnoreCase(expectedType);
    }

    /**
     * Klasa za privremeno sumiranje
     * debit / credit prometa iz ledger-a.
     */
    private static class BalanceData {

        private double debit = 0.0;

        private double credit = 0.0;
    }
    
    @Transactional
    public void moveAopToNextYear(
            String userId,
            Integer aopListYear) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(
                    "userId ne smije biti prazan."
            );
        }

        if (aopListYear == null) {
            throw new IllegalArgumentException(
                    "aopListYear ne smije biti null."
            );
        }

        /*
         * Ciljna godina je godina + 1.
         *
         * Primjer:
         *
         * 2025 -> 2026
         */
        int nextYear = aopListYear + 1;

        /*
         * ---------------------------------------------------------
         * 1. UZMI SVE ACCOUNTE IZ ZADANE AOP GODINE
         * ---------------------------------------------------------
         */
        List<AOP> sourceAccounts =
                aopRepo.findByUserIdAndAopListYear(
                        userId,
                        aopListYear
                );

        /*
         * Ako nema podataka, nema što kopirati.
         */
        if (sourceAccounts.isEmpty()) {
            return;
        }

        /*
         * ---------------------------------------------------------
         * 2. PROĐI KROZ SVE ACCOUNTE
         * ---------------------------------------------------------
         */
        for (AOP source : sourceAccounts) {

            String code = source.getAopCode();

            if (code == null || code.isBlank()) {
                continue;
            }

            /*
             * -----------------------------------------------------
             * 3. PROVJERI POSTOJI LI ACCOUNT U NOVOJ GODINI
             * -----------------------------------------------------
             *
             * userId + code + nextYear
             */
            AOP target =
                    aopRepo
                            .findByUserIdAndAopCodeAndAopListYear(
                                    userId,
                                    code,
                                    nextYear
                            );

            /*
             * -----------------------------------------------------
             * 4. AKO NE POSTOJI -> NOVI ACCOUNT
             * -----------------------------------------------------
             */
            if (target == null) {

                target = new AOP();

                target.setUserId(userId);

                target.setAopCode(
                        source.getAopCode()
                );
            }

            /*
             * -----------------------------------------------------
             * 5. PREBACI AOP PODATKE
             * -----------------------------------------------------
             */

            target.setAopName(
                    source.getAopName()
            );

            target.setAopCode(
                    source.getAopCode()
            );

            target.setAopName(
                    source.getAopName()
            );


            target.setAopListName(
                    source.getAopListName()
            );

            /*
             * -----------------------------------------------------
             * 6. POSTAVI NOVU AOP GODINU
             * -----------------------------------------------------
             *
             * 2025 -> 2026
             */
            target.setAopListYear(
                    nextYear
            );

            /*
             * -----------------------------------------------------
             * 7. SPREMI
             * -----------------------------------------------------
             *
             * CREATE ako ne postoji.
             *
             * UPDATE ako već postoji.
             */
            aopRepo.save(target);
        }
    }    

    @Transactional
    public void deleteByAopListYear(
            String userId,
            Integer aopListYear) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(
                    "userId ne smije biti prazan."
            );
        }

        if (aopListYear == null) {
            throw new IllegalArgumentException(
                    "aopListYear ne smije biti null."
            );
        }

        aopRepo.deleteByUserIdAndAopListYear(
                userId,
                aopListYear
        );
    }
    
    @Transactional
    public void deleteAccountByAopListYear(
            String userId,
            Integer aopListYear) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(
                    "userId ne smije biti prazan."
            );
        }

        if (aopListYear == null) {
            throw new IllegalArgumentException(
                    "aopListYear ne smije biti null."
            );
        }

        accountRepo.deleteByUserIdAndAopListYear(
                userId,
                aopListYear
        );
    }    
}
