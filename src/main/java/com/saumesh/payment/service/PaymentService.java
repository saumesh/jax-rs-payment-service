package com.saumesh.payment.service;

import com.saumesh.payment.service.dto.Error;
import com.saumesh.payment.service.dto.TransferDTO;
import com.saumesh.payment.controller.PaymentController;
import com.saumesh.payment.domain.Account;
import com.saumesh.payment.domain.InsufficientBalanceException;
import com.saumesh.payment.persistenance.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/v1/payments")
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private PaymentController paymentController;
    private KeyValueStore<String, TransferDTO> transactionsStore;      // Transactions cache to check in progress transfers, and transfers idempotent

    @Inject
    public PaymentService(PaymentController paymentController, KeyValueStore<String, TransferDTO> transactionsStore) {
        this.paymentController = paymentController;
        this.transactionsStore = transactionsStore;
    }

    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response transfer(TransferDTO transfer) {
        // validate request body
        Optional<Response> optErrors = validate(transfer);
        if(optErrors.isPresent()) {
            logger.info("Transfer request is not valid");
            return optErrors.get();
        }

        // Check if duplicate transfer request
        String txnCacheKey = transfer.getSourceAccountNumber() + ":" + transfer.getTargetAccountNumber() + ":" + transfer.getAmount();
        TransferDTO cachedTxn = transactionsStore.get(txnCacheKey);
        if(cachedTxn != null) {
            // There is already transfer in-progress between Source and Target account for same amount
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(cachedTxn)
                    .build();
        }

        try {
            transfer.setTimestamp(new Date());
            transfer.setTransactionId(UUID.randomUUID());
            transactionsStore.add(txnCacheKey, transfer);

            Optional<Account> srcAcc = paymentController.getAccount(transfer.getSourceAccountNumber());
            Optional<Account> tarAcc = paymentController.getAccount(transfer.getTargetAccountNumber());

            if(!srcAcc.isPresent() || !tarAcc.isPresent()) {
                logger.info("Invalid account number for source or target");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new Error(404, "INVALID_ACCOUNT", "Invalid account details in transfer request"))
                        .build();
            }

            if(paymentController.transfer(srcAcc.get(), tarAcc.get(), transfer.getAmount())) {
                logger.info("Successfully transferred amount {} from account: {} to account: {}", transfer.getAmount(),
                        transfer.getSourceAccountNumber(), transfer.getTargetAccountNumber());
                return Response.ok(transfer).build();
            } else {
                logger.info("Could not transfer amount {} from account: {} to account: {}", transfer.getAmount(),
                        transfer.getSourceAccountNumber(), transfer.getTargetAccountNumber());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new Error(500, "COULD_NOT_TRANSFER", "Could not transfer amount because of internal system issue"))
                        .build();
            }
        } catch (InsufficientBalanceException excp) {
            logger.error("Could not transfer amount", excp);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(500, "INSUFFICIENT_BALANCE", "Insufficient balance in source account"))
                    .build();
        } catch (Exception excp) {
            logger.error("Could not transfer amount", excp);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Error(500, "INTERNAL_TRANSFER_ERROR", "Internal server error occurred while transferring amount"))
                    .build();
        } finally {
            transactionsStore.delete(txnCacheKey);
        }
    }

    private Optional<Response> validate(TransferDTO transferDTO) {
        Response response = null;
       if(0 >= transferDTO.getSourceAccountNumber()) {
            response = Response.status(Response.Status.BAD_REQUEST)
                    .entity(new Error(400, "INVALID_ACCOUNT", "Source account information is not correct"))
                    .build();
        } else if(0 >= transferDTO.getTargetAccountNumber()) {
            response = Response.status(Response.Status.BAD_REQUEST)
                    .entity(new Error(400, "INVALID_ACCOUNT", "Target account information is not correct"))
                    .build();
        } else if(0 >= transferDTO.getAmount()) {
            response = Response.status(Response.Status.BAD_REQUEST)
                    .entity(new Error(400, "INVALID_AMOUNT", "Invalid amount"))
                    .build();
        } else if(transferDTO.getSourceAccountNumber() == transferDTO.getTargetAccountNumber()) {
           response = Response.status(Response.Status.BAD_REQUEST)
                   .entity(new Error(400, "INVALID_ACCOUNT", "Source and target account numbers can not be same"))
                   .build();
       }

        return Optional.ofNullable(response);
    }
}
