package pro.datawiki.igaming.infra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.datawiki.igaming.infra.entity.CloudAccount;
import pro.datawiki.igaming.infra.repository.CloudAccountRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudAccountService {

    private final CloudAccountRepository repository;

    public List<CloudAccount> getAllAccounts() {
        return repository.findAll();
    }
    
    public List<CloudAccount> getActiveAccounts() {
        return repository.findByIsActiveTrueOrderByPriorityAsc();
    }

    @Transactional
    public CloudAccount saveAccount(CloudAccount account) {
        if (account.getId() == null) {
            account.setCreatedAt(LocalDateTime.now());
        }
        log.info("Saving CloudAccount: {}", account.getName());
        return repository.save(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        log.info("Deleting CloudAccount with ID: {}", id);
        repository.deleteById(id);
    }
}
