package jorge.matias.plantilla.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jorge.matias.plantilla.model.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID>{
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
}
