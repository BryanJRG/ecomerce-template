package com.bjdev.base.services.interfaces;

import com.bjdev.base.dto.request.AcceptAdminInvitationRequest;
import com.bjdev.base.dto.request.CreateAdminInvitationRequest;
import com.bjdev.base.dto.request.RejectAdminInvitationRequest;
import com.bjdev.base.dto.response.AdminActionResult;
import com.bjdev.base.dto.response.AdminInvitationResponse;
import com.bjdev.base.dto.response.MessageResponse;
import com.bjdev.base.models.user.User;

import java.util.List;

public interface AdminInvitationService {

    AdminActionResult<AdminInvitationResponse> createInvitation(CreateAdminInvitationRequest request);

    /**
     * Actually creates the invitation, bypassing the email-confirmation gate. Called directly by
     * {@link #createInvitation} when the gate is disabled, and by AdminActionDispatchService once a
     * pending confirmation for this action type has been confirmed.
     */
    AdminInvitationResponse executeCreateInvitation(User creator, String invitedEmail);

    List<AdminInvitationResponse> listInvitations();

    void validateInvitation(String token);

    MessageResponse acceptInvitation(AcceptAdminInvitationRequest request);

    MessageResponse rejectInvitation(RejectAdminInvitationRequest request);

    AdminInvitationResponse resendInvitation(Long invitationId);

    AdminInvitationResponse cancelInvitation(Long invitationId);
}
