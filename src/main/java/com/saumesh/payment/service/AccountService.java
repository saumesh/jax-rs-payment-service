package com.saumesh.payment.service;

import com.saumesh.payment.controller.PaymentController;
import com.saumesh.payment.domain.Account;
import com.saumesh.payment.service.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/v1/accounts")
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private PaymentController paymentController;

    @Inject
    public AccountService(PaymentController paymentController) {
        this.paymentController = paymentController;
    }

    @GET
    @Path("/{account_number}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccount(@PathParam("account_number") long accountNumber) {
        logger.debug("Handling get account for {}", accountNumber);
        try {
            Optional<Account> optAcc = paymentController.getAccount(accountNumber);
            if (optAcc.isPresent()) {
                return Response.ok(optAcc.get()).build();
            }

            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new Error(404, "ACCOUNT_NOT_FOUND", "Account does not exists"))
                    .build();
        } catch (Exception excp) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(500, "INTERNAL_ERROR", "Internal server error occurred"))
                    .build();
        }
    }
}
