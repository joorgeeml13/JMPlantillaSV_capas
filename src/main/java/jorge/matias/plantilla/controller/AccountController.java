package jorge.matias.plantilla.controller;

import jorge.matias.plantilla.service.AccountService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jorge.matias.plantilla.controller.dto.request.account.ChangePasswordRequest;
import jorge.matias.plantilla.model.entity.Account;
import jorge.matias.plantilla.model.entity.AccountPrincipal;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping(value="/account")
public class AccountController {
    

    private final AccountService accountService;

    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PutMapping("/password")
    public ResponseEntity<?> putMethodName(
        @PathVariable ChangePasswordRequest request,
        @AuthenticationPrincipal AccountPrincipal currentUser
    ) {
        accountService.changePassword(request.oldPaswword(), request.newPassword(), currentUser.getUsername());
        
        return ResponseEntity.ok().build();
    }
}
