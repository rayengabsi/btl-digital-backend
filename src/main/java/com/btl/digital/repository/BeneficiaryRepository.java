package com.btl.digital.repository;

import com.btl.digital.entity.Beneficiary;
import com.btl.digital.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByOwnerOrderByCreatedAtDesc(User owner);

    Optional<Beneficiary> findByIdAndOwner(Long id, User owner);

    boolean existsByOwnerAndBeneficiaryAccountNumber(User owner, String beneficiaryAccountNumber);
}
