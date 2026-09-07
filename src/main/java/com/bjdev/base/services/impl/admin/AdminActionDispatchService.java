package com.bjdev.base.services.impl.admin;

import com.bjdev.base.dto.response.MessageResponse;
import com.bjdev.base.models.auth.PendingAdminAction;
import com.bjdev.base.services.interfaces.AdminInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confirms a pending admin action and carries it out. Kept separate from
 * AdminActionConfirmationService so that service can stay free of dependencies on the domain
 * services (AdminInvitationService, ...) that actually execute each action type — avoids a
 * circular dependency between "request confirmation" and "execute once confirmed".
 */
@Service
@RequiredArgsConstructor
public class AdminActionDispatchService {

    private final AdminActionConfirmationService confirmationService;
    private final AdminInvitationService adminInvitationService;

    @Transactional
    public MessageResponse confirmAndExecute(String token) {
        PendingAdminAction action = confirmationService.confirm(token);

        switch (action.getType()) {
            case CREATE_ADMIN_INVITATION ->
                    adminInvitationService.executeCreateInvitation(action.getRequestedBy(), action.getPayload());
        }

        return new MessageResponse("Acción confirmada y ejecutada correctamente.");
    }
}
