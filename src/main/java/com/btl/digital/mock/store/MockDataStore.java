package com.btl.digital.mock.store;

import com.btl.digital.mock.model.MockAccount;
import com.btl.digital.mock.model.MockBeneficiary;
import com.btl.digital.mock.model.MockClientUser;
import com.btl.digital.mock.model.MockCurrencyRate;
import com.btl.digital.mock.model.MockCustomer;
import com.btl.digital.mock.model.MockLoginChallenge;
import com.btl.digital.mock.model.MockPermission;
import com.btl.digital.mock.model.MockRegistrationSession;
import com.btl.digital.mock.model.MockSubscription;
import com.btl.digital.mock.model.MockTransaction;
import com.btl.digital.mock.model.MockTransferDemand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The in-memory fake banking database. Holds all mock "T24" data plus the
 * mock client users, registration sessions, login challenges and transfer
 * demands. Real T24, SMS and email providers are replaced by this store and
 * the mocked providers logging to the console.
 */
@Component
public class MockDataStore {

    private static final Logger log = LoggerFactory.getLogger(MockDataStore.class);

    private final PasswordEncoder passwordEncoder;

    private final AtomicLong clientUserIdSeq = new AtomicLong(1);
    private final AtomicLong beneficiaryIdSeq = new AtomicLong(1);
    private final AtomicLong transferDemandIdSeq = new AtomicLong(1);
    private final AtomicLong subscriptionIdSeq = new AtomicLong(1);
    private final AtomicLong permissionIdSeq = new AtomicLong(1);
    private final AtomicLong challengeIdSeq = new AtomicLong(1);

    private final Map<String, MockCustomer> customersByPhone = new LinkedHashMap<>();
    private final Map<Long, MockClientUser> clientUsersById = new ConcurrentHashMap<>();
    private final Map<String, MockClientUser> clientUsersByEmail = new ConcurrentHashMap<>();

    private final Map<String, MockAccount> accountsByIban = new LinkedHashMap<>();
    private final Map<String, MockAccount> accountsByAccountId = new LinkedHashMap<>();
    private final Map<String, List<MockTransaction>> transactionsByIban = new LinkedHashMap<>();

    private final Map<String, MockBeneficiary> beneficiariesById = new ConcurrentHashMap<>();
    private final Map<Long, List<MockBeneficiary>> beneficiariesByClient = new ConcurrentHashMap<>();

    private final Map<Long, MockTransferDemand> transferDemandsById = new ConcurrentHashMap<>();

    private final Map<String, MockRegistrationSession> registrationSessions = new ConcurrentHashMap<>();
    private final Map<Long, MockLoginChallenge> loginChallenges = new ConcurrentHashMap<>();

    private final Map<Long, MockSubscription> subscriptionsById = new ConcurrentHashMap<>();
    private final Map<Long, MockPermission> permissionsById = new LinkedHashMap<>();

    private final Map<String, List<MockCurrencyRate>> currencyRates = new LinkedHashMap<>();

    public MockDataStore(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        seed();
    }

    // ------------------------------------------------------------------
    // Seed data
    // ------------------------------------------------------------------

    private void seed() {
        seedPermissions();
        seedCustomers();
        seedClientUsers();
        seedSubscriptions();
        seedCurrencies();
    }

    private void seedPermissions() {
        registerPermission(1L, "MAKE_INTRA_TRANSFER");
        registerPermission(2L, "SET_SUBSCRIPTION");
        registerPermission(3L, "SELECT_SUBSCRIPTION");
        registerPermission(4L, "MANAGE_SUBSCRIPTION");
        registerPermission(5L, "MANAGE_PERMISSION");
    }

    private void registerPermission(long id, String code) {
        permissionsById.put(id, MockPermission.builder().id(id).code(code).build());
    }

    private void seedCustomers() {
        MockCustomer amina = MockCustomer.builder()
                .customerId("TN0000001")
                .firstName("Amina")
                .lastName("Ben Ali")
                .idType("CIN")
                .idNumber("12345678")
                .dateOfBirth("19900101")
                .phoneNumber(PHONE_NUMBER_AMINA)
                .email("amina@example.com")
                .registered(true)
                .build();

        MockCustomer sami = MockCustomer.builder()
                .customerId("TN0000002")
                .firstName("Sami")
                .lastName("Ben Salah")
                .idType("CIN")
                .idNumber("87654321")
                .dateOfBirth("19850615")
                .phoneNumber("21650223344")
                .email("sami@example.com")
                .registered(false)
                .build();

        MockCustomer karim = MockCustomer.builder()
                .customerId("TN0000003")
                .firstName("Karim")
                .lastName("Trabelsi")
                .idType("PASSPORT")
                .idNumber("P987654")
                .dateOfBirth("19780222")
                .phoneNumber("21655312233")
                .email("karim@example.com")
                .registered(false)
                .build();

        // Development / manual-test customer matching the Flutter registration
        // demo credentials (POST /api/v1/registration/verify-client):
        //   First Name: Rayen | Last Name: Gabsi
        //   ID Type: CIN | ID Number: 13516553
        //   Date of Birth (YYYYMMDD): 20031106
        //   Phone: 53852323   (SMS_1 registered number, sent without the 216 prefix)
        MockCustomer rayen = MockCustomer.builder()
                .customerId("TN0000004")
                .firstName("Rayen")
                .lastName("Gabsi")
                .idType("CIN")
                .idNumber("13516553")
                .dateOfBirth("20031106")
                .phoneNumber("53852323")
                .email("rayen.gabsi@mock.dev")
                .registered(false)
                .build();

        customersByPhone.put(PHONE_NUMBER_AMINA, amina);
        customersByPhone.put("21650223344", sami);
        customersByPhone.put("21655312233", karim);
        customersByPhone.put("53852323", rayen);
    }

    private void seedClientUsers() {
        MockCustomer amina = customersByPhone.get(PHONE_NUMBER_AMINA);
        MockClientUser aminaUser = MockClientUser.builder()
                .id(clientUserIdSeq.getAndIncrement())
                .email("amina@example.com")
                .passwordHash(passwordEncoder.encode(PASSWORD_AMINA))
                .firstName("Amina")
                .lastName("Ben Ali")
                .t24CustomerId(amina.getCustomerId())
                .phoneNumber(PHONE_NUMBER_AMINA)
                .emailAddress("amina@example.com")
                .subscriptionId(1L)
                .status("ACTIVE")
                .firstLoginRequired(false)
                .phoneVerified(true)
                .emailVerified(true)
                .build();
        amina.setRegistered(true);
        amina.setClientUserId(aminaUser.getId());
        addClientUser(aminaUser);
        seedAccounts(aminaUser);

        // Deterministic mock test user used by the non-auth mock banking
        // endpoints (they fall back to this user when no JWT is presented).
        // Note: the T24 customer is left unregistered so the registration
        // demo (POST /api/v1/registration/verify-client) keeps working.
        MockCustomer rayen = customersByPhone.get(PHONE_NUMBER_RAYEN);
        MockClientUser rayenUser = MockClientUser.builder()
                .id(clientUserIdSeq.getAndIncrement())
                .email("rayen.gabsi@mock.dev")
                .passwordHash(passwordEncoder.encode(PASSWORD_RAYEN))
                .firstName("Rayen")
                .lastName("Gabsi")
                .t24CustomerId(rayen.getCustomerId())
                .phoneNumber(PHONE_NUMBER_RAYEN)
                .emailAddress("rayen.gabsi@mock.dev")
                .subscriptionId(2L) // Premium: all mock permissions
                .status("ACTIVE")
                .firstLoginRequired(false)
                .phoneVerified(true)
                .emailVerified(true)
                .build();
        addClientUser(rayenUser);
        seedRayenAccounts(rayenUser);
        addBeneficiary(MockBeneficiary.builder()
                .id(beneficiaryIdSeq.getAndIncrement())
                .clientUserId(rayenUser.getId())
                .beneficiaryName("Sami Ben Salah")
                .ribNumber("01000000000000000105")
                .createdAt(LocalDateTime.now())
                .build());
    }

    private void seedRayenAccounts(MockClientUser rayen) {
        addAccount(MockAccount.builder()
                .accountId("2089021249357")
                .arrangementId("AA26086WN1AZ")
                .productName("COMPTE PERSONNE")
                .iban("TN5926028000890212493571")
                .currencyId("TND")
                .availableBalance(new BigDecimal("75000.000"))
                .workingBalance(new BigDecimal("75000.000"))
                .ownerClientUserId(rayen.getId())
                .build());

        addAccount(MockAccount.builder()
                .accountId("2089021207514")
                .arrangementId("AA26086WN1BY")
                .productName("COMPTE PERSONNE")
                .iban("TN5926028000890207513565")
                .currencyId("TND")
                .availableBalance(new BigDecimal("25000.000"))
                .workingBalance(new BigDecimal("25000.000"))
                .ownerClientUserId(rayen.getId())
                .build());
    }

    private void seedAccounts(MockClientUser amina) {
        addAccount(MockAccount.builder()
                .accountId("1089021249356")
                .arrangementId("AA26086WN1AX")
                .productName("COMPTE PERSONNE")
                .iban("TN5926028000890212493554")
                .currencyId("TND")
                .availableBalance(new BigDecimal("48731.000"))
                .workingBalance(new BigDecimal("48731.000"))
                .ownerClientUserId(amina.getId())
                .build());

        addAccount(MockAccount.builder()
                .accountId("1089021207513")
                .arrangementId("AA26086WN1AY")
                .productName("COMPTE PERSONNE")
                .iban("TN5926028000890207513564")
                .currencyId("TND")
                .availableBalance(new BigDecimal("12000.000"))
                .workingBalance(new BigDecimal("12000.000"))
                .ownerClientUserId(amina.getId())
                .build());

        seedTransactions();
    }

    private void seedTransactions() {
        addTransaction("1089021249356", MockTransaction.builder()
                .accountId("1089021249356")
                .transactionReference("FT260866NR7C")
                .transactionCode("Transfer Out")
                .bookingDate(LocalDate.of(2026, 3, 27))
                .valueDate(LocalDate.of(2026, 3, 27))
                .closingBalance(new BigDecimal("48731.000"))
                .debitAmount(new BigDecimal("100.000"))
                .creditAmount(null)
                .narratives(List.of("To Acc.No.- 1089021207513"))
                .build());

        addTransaction("1089021249356", MockTransaction.builder()
                .accountId("1089021249356")
                .transactionReference("FT100866NR7D")
                .transactionCode("Credit Salary")
                .bookingDate(LocalDate.of(2026, 3, 26))
                .valueDate(LocalDate.of(2026, 3, 26))
                .closingBalance(new BigDecimal("48831.000"))
                .debitAmount(null)
                .creditAmount(new BigDecimal("1500.000"))
                .narratives(List.of("Salary March 2026"))
                .build());

        addTransaction("1089021249356", MockTransaction.builder()
                .accountId("1089021249356")
                .transactionCode("Purchase")
                .bookingDate(LocalDate.of(2026, 3, 25))
                .valueDate(LocalDate.of(2026, 3, 25))
                .closingBalance(new BigDecimal("47331.000"))
                .debitAmount(new BigDecimal("220.000"))
                .creditAmount(null)
                .narratives(List.of("POS Carrefour Tunis"))
                .build());
    }

    private void seedSubscriptions() {
        long standardId = subscriptionIdSeq.getAndIncrement();
        MockSubscription standard = MockSubscription.builder()
                .id(standardId)
                .name("Standard")
                .description("Standard subscription")
                .permissionIds(new LinkedHashSet<>(Set.of(1L)))
                .build();
        subscriptionsById.put(standardId, standard);

        long premiumId = subscriptionIdSeq.getAndIncrement();
        MockSubscription premium = MockSubscription.builder()
                .id(premiumId)
                .name("Premium")
                .description("Premium subscription")
                .permissionIds(new LinkedHashSet<>(Set.of(1L, 2L, 3L, 4L, 5L)))
                .build();
        subscriptionsById.put(premiumId, premium);
    }

    private void seedCurrencies() {
        currencyRates.put("AED", List.of(
                MockCurrencyRate.builder().currencyId("AED").displayName("United Arab Emirates Dirhams")
                        .market("10").sellRate(new BigDecimal("8.60")).buyRate(new BigDecimal("8.34")).build(),
                MockCurrencyRate.builder().currencyId("AED").displayName("United Arab Emirates Dirhams")
                        .market("1").sellRate(new BigDecimal("8.55")).buyRate(new BigDecimal("8.38")).build()));
        currencyRates.put("BHD", List.of(
                MockCurrencyRate.builder().currencyId("BHD").displayName("Bahraini Dinar")
                        .market("1").sellRate(new BigDecimal("8.504")).buyRate(new BigDecimal("8.103")).build(),
                MockCurrencyRate.builder().currencyId("BHD").displayName("Bahraini Dinar")
                        .market("10").sellRate(new BigDecimal("8.60")).buyRate(new BigDecimal("8.15")).build()));
        currencyRates.put("EUR", List.of(
                MockCurrencyRate.builder().currencyId("EUR").displayName("Euro")
                        .market("1").sellRate(new BigDecimal("3.480")).buyRate(new BigDecimal("3.314")).build()));
        currencyRates.put("USD", List.of(
                MockCurrencyRate.builder().currencyId("USD").displayName("US Dollar")
                        .market("1").sellRate(new BigDecimal("3.220")).buyRate(new BigDecimal("3.080")).build()));
        currencyRates.put("GBP", List.of(
                MockCurrencyRate.builder().currencyId("GBP").displayName("British Pound")
                        .market("1").sellRate(new BigDecimal("4.100")).buyRate(new BigDecimal("3.910")).build()));
        currencyRates.put("SAR", List.of(
                MockCurrencyRate.builder().currencyId("SAR").displayName("Saudi Riyal")
                        .market("1").sellRate(new BigDecimal("0.858")).buyRate(new BigDecimal("0.820")).build()));
    }

    // ------------------------------------------------------------------
    // id sequences
    // ------------------------------------------------------------------

    public long nextBeneficiaryId() { return beneficiaryIdSeq.getAndIncrement(); }
    public long nextTransferDemandId() { return transferDemandIdSeq.getAndIncrement(); }
    public long nextSubscriptionId() { return subscriptionIdSeq.getAndIncrement(); }
    public long nextPermissionId() { return permissionIdSeq.getAndIncrement(); }
    public long nextClientUserId() { return clientUserIdSeq.getAndIncrement(); }
    public long nextLoginChallengeId() { return challengeIdSeq.getAndIncrement(); }

    // ------------------------------------------------------------------
    // accounts + transactions
    // ------------------------------------------------------------------

    public void addAccount(MockAccount account) {
        accountsByIban.put(account.getIban(), account);
        accountsByAccountId.put(account.getAccountId(), account);
    }

    public void addTransaction(String accountId, MockTransaction txn) {
        transactionsByIban.computeIfAbsent(accountId, k -> new ArrayList<>()).add(txn);
    }

    public Optional<MockAccount> accountByIban(String iban) {
        return Optional.ofNullable(accountsByIban.get(iban));
    }

    public Optional<MockAccount> accountByAccountId(String accountId) {
        return Optional.ofNullable(accountsByAccountId.get(accountId));
    }

    public List<MockAccount> accountsForClient(Long clientUserId) {
        return accountsByAccountId.values().stream()
                .filter(a -> clientUserId.equals(a.getOwnerClientUserId()))
                .toList();
    }

    public List<MockTransaction> transactionsForAccount(String accountId) {
        List<MockTransaction> txns = transactionsByIban.get(accountId);
        return txns == null ? List.of() : txns;
    }

    // ------------------------------------------------------------------
    // clients
    // ------------------------------------------------------------------

    public void addClientUser(MockClientUser user) {
        clientUsersById.put(user.getId(), user);
        clientUsersByEmail.put(user.getEmail(), user);
    }

    public Optional<MockClientUser> clientById(Long id) {
        return Optional.ofNullable(clientUsersById.get(id));
    }

    public Optional<MockClientUser> clientByEmail(String email) {
        return Optional.ofNullable(clientUsersByEmail.get(email.toLowerCase()));
    }

    public boolean emailInUse(String email) {
        return clientUsersByEmail.containsKey(email.toLowerCase());
    }

    public Optional<MockCustomer> customerByPhone(String normalizedPhone) {
        return Optional.ofNullable(customersByPhone.get(normalizedPhone));
    }

    public Optional<MockCustomer> findCustomer(String idType, String idNumber, String dateOfBirth, String phone) {
        return customersByPhone.values().stream()
                .filter(c -> c.getIdType().equalsIgnoreCase(idType))
                .filter(c -> c.getIdNumber().equals(idNumber))
                .filter(c -> c.getDateOfBirth().equals(dateOfBirth))
                .filter(c -> normalizePhone(c.getPhoneNumber()).equals(normalizePhone(phone)))
                .findFirst();
    }

    public static String normalizePhone(String phone) {
        return phone.replaceAll("\\D", "");
    }

    public Optional<MockCustomer> customerByT24Id(String customerId) {
        return customersByPhone.values().stream()
                .filter(c -> c.getCustomerId().equals(customerId))
                .findFirst();
    }

    public String customerByPhoneNullable(String customerId) {
        return customerByT24Id(customerId).map(MockCustomer::getPhoneNumber).orElse(null);
    }

    public void markCustomerRegistered(String customerId, Long clientUserId) {
        customerByT24Id(customerId).ifPresent(c -> {
            c.setRegistered(true);
            c.setClientUserId(clientUserId);
        });
    }

    // ------------------------------------------------------------------
    // beneficiaries
    // ------------------------------------------------------------------

    public void addBeneficiary(MockBeneficiary b) {
        beneficiariesById.put(String.valueOf(b.getId()), b);
        beneficiariesByClient.computeIfAbsent(b.getClientUserId(), k -> new ArrayList<>()).add(b);
    }

    public Optional<MockBeneficiary> beneficiaryById(Long id) {
        return Optional.ofNullable(beneficiariesById.get(String.valueOf(id)));
    }

    public List<MockBeneficiary> beneficiariesForClient(Long clientUserId) {
        return beneficiariesByClient.getOrDefault(clientUserId, List.of());
    }

    public boolean clientHasRib(Long clientUserId, String ribNumber) {
        return beneficiariesByClient.getOrDefault(clientUserId, List.of()).stream()
                .anyMatch(b -> b.getRibNumber().equals(ribNumber));
    }

    public boolean clientOwnsAccountIban(Long clientUserId, String iban) {
        return accountsForClient(clientUserId).stream().anyMatch(a -> a.getIban().equals(iban));
    }

    public void removeBeneficiary(Long clientUserId, Long id) {
        beneficiariesById.remove(String.valueOf(id));
        List<MockBeneficiary> list = beneficiariesByClient.getOrDefault(clientUserId, new ArrayList<>());
        list.removeIf(b -> b.getId().equals(id));
    }

    public void updateBeneficiary(MockBeneficiary updated) {
        beneficiariesById.put(String.valueOf(updated.getId()), updated);
        beneficiariesByClient.computeIfAbsent(updated.getClientUserId(), k -> new ArrayList<>())
                .replaceAll(b -> b.getId().equals(updated.getId()) ? updated : b);
    }

    // ------------------------------------------------------------------
    // transfer demands
    // ------------------------------------------------------------------

    public void addTransferDemand(MockTransferDemand d) {
        transferDemandsById.put(d.getId(), d);
    }

    public Optional<MockTransferDemand> transferDemandById(Long id) {
        return Optional.ofNullable(transferDemandsById.get(id));
    }

    public List<MockTransferDemand> transferDemandsForClient(Long clientUserId) {
        return transferDemandsById.values().stream()
                .filter(d -> clientUserId.equals(d.getClientUserId()))
                .toList();
    }

    // ------------------------------------------------------------------
    // registration + login
    // ------------------------------------------------------------------

    public void addRegistrationSession(MockRegistrationSession s) {
        registrationSessions.put(s.getToken(), s);
    }

    public Optional<MockRegistrationSession> registrationSessionByToken(String token) {
        return Optional.ofNullable(registrationSessions.get(token));
    }

    public void addLoginChallenge(MockLoginChallenge c) {
        loginChallenges.put(c.getId(), c);
    }

    public Optional<MockLoginChallenge> loginChallenge(Long id) {
        return Optional.ofNullable(loginChallenges.get(id));
    }

    // ------------------------------------------------------------------
    // subscriptions + permissions
    // ------------------------------------------------------------------

    public void addSubscription(MockSubscription s) {
        subscriptionsById.put(s.getId(), s);
    }

    public Optional<MockSubscription> subscriptionById(Long id) {
        return Optional.ofNullable(subscriptionsById.get(id));
    }

    public List<MockSubscription> allSubscriptions() {
        return new ArrayList<>(subscriptionsById.values());
    }

    public void removeSubscription(Long id) {
        subscriptionsById.remove(id);
    }

    public Optional<MockPermission> permissionById(Long id) {
        return Optional.ofNullable(permissionsById.get(id));
    }

    public List<MockPermission> allPermissions() {
        return new ArrayList<>(permissionsById.values());
    }

    public void addPermission(MockPermission p) {
        permissionsById.put(p.getId(), p);
    }

    public Set<String> permissionsGrantedToClient(Long clientUserId) {
        MockClientUser user = clientUsersById.get(clientUserId);
        if (user == null || user.getSubscriptionId() == null) {
            return Set.of();
        }
        MockSubscription sub = subscriptionsById.get(user.getSubscriptionId());
        if (sub == null) {
            return Set.of();
        }
        return sub.getPermissionIds().stream()
                .map(permissionsById::get)
                .filter(java.util.Objects::nonNull)
                .map(MockPermission::getCode)
                .collect(java.util.stream.Collectors.toSet());
    }

    public PasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }

    public List<MockCurrencyRate> currencyRates(String currencyId) {
        return currencyRates.getOrDefault(currencyId, List.of());
    }

    private static final String PHONE_NUMBER_AMINA = "21655123456";
    private static final String PASSWORD_AMINA = "Password1@";
    private static final String PHONE_NUMBER_RAYEN = "53852323";
    private static final String PASSWORD_RAYEN = "Password1@";
}