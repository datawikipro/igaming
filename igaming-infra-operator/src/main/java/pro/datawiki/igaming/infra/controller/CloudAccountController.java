package pro.datawiki.igaming.infra.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pro.datawiki.igaming.infra.entity.CloudAccount;
import pro.datawiki.igaming.infra.service.CloudAccountService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cloud-accounts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CloudAccountController {

    private final CloudAccountService cloudAccountService;

    @GetMapping
    public List<CloudAccount> getAllAccounts() {
        return cloudAccountService.getAllAccounts();
    }

    @GetMapping("/active")
    public List<CloudAccount> getActiveAccounts() {
        return cloudAccountService.getActiveAccounts();
    }

    @PostMapping
    public CloudAccount saveAccount(@RequestBody CloudAccount account) {
        return cloudAccountService.saveAccount(account);
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id) {
        cloudAccountService.deleteAccount(id);
    }
}
