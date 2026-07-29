package com.by.sasa.bistrovic.bookkeeping.management;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
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

    // ---------------- INVOICE ----------------

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
}
