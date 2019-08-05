package com.saumesh.payment.service;

import com.saumesh.payment.controller.BasicPaymentController;
import com.saumesh.payment.controller.PaymentController;
import com.saumesh.payment.domain.Account;
import com.saumesh.payment.domain.InsufficientBalanceException;
import com.saumesh.payment.persistenance.KeyValueStore;
import com.saumesh.payment.persistenance.memory.InMemoryKeyValueStore;
import com.saumesh.payment.service.dto.Error;
import com.saumesh.payment.service.dto.TransferDTO;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {
    private PaymentService paymentService;
    private PaymentController paymentController;
    private KeyValueStore<String, TransferDTO> transactionsStore;

    private long sourceAccountNumber;
    private long targetAccountNumber;
    private double transferAmount;
    private Optional<Account> sourceAccount;
    private Optional<Account> targetAccount;
    private TransferDTO dto;
    private String txnKey;

    @Before
    public void setup() {
        sourceAccountNumber = 123456;
        double sourceBalance = 500;
        targetAccountNumber = 654321;
        long targetBalance = 200;

        transferAmount = 50;
        sourceAccount = Optional.of(new Account(sourceAccountNumber, sourceBalance));
        targetAccount = Optional.of(new Account(targetAccountNumber, targetBalance));

        dto = new TransferDTO();
        dto.setSourceAccountNumber(sourceAccountNumber);
        dto.setTargetAccountNumber(targetAccountNumber);
        dto.setAmount(transferAmount);

        txnKey = sourceAccountNumber + ":" + targetAccountNumber + ":" + transferAmount;

        paymentController = mock(BasicPaymentController.class);
        transactionsStore = new InMemoryKeyValueStore<>();

        paymentService = new PaymentService(paymentController, transactionsStore);
    }

    @Test
    public void testTransfer() throws Exception {
        when(paymentController.getAccount(sourceAccountNumber)).thenReturn(sourceAccount);
        when(paymentController.getAccount(targetAccountNumber)).thenReturn(targetAccount);
        when(paymentController.transfer(sourceAccount.get(), targetAccount.get(), transferAmount)).thenReturn(true);

        Response response = paymentService.transfer(dto);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        TransferDTO entity = (TransferDTO)response.getEntity();
        assertThat(entity).isNotNull();
        assertThat(entity.getSourceAccountNumber()).isEqualTo(dto.getSourceAccountNumber());
        assertThat(entity.getTargetAccountNumber()).isEqualTo(dto.getTargetAccountNumber());
        assertThat(entity.getAmount()).isEqualTo(dto.getAmount());
        assertThat(entity.getTimestamp()).isNotNull();
        assertThat(entity.getTransactionId()).isNotNull();
    }

    @Test
    public void testTransferDuplicate() throws Exception {
        dto.setTransactionId(UUID.randomUUID());
        dto.setTimestamp(new Date());
        transactionsStore.add(txnKey, dto);

        TransferDTO duplicate = new TransferDTO();
        duplicate.setSourceAccountNumber(dto.getSourceAccountNumber());
        duplicate.setTargetAccountNumber(dto.getTargetAccountNumber());
        duplicate.setAmount(dto.getAmount());

        verify(paymentController, times(0)).getAccount(sourceAccountNumber);
        verify(paymentController, times(0)).getAccount(targetAccountNumber);
        verify(paymentController, times(0)).transfer(sourceAccount.get(), targetAccount.get(), transferAmount);

        Response response = paymentService.transfer(duplicate);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        TransferDTO entity = (TransferDTO)response.getEntity();
        assertThat(entity).isNotNull();
        assertThat(entity.getSourceAccountNumber()).isEqualTo(dto.getSourceAccountNumber());
        assertThat(entity.getTargetAccountNumber()).isEqualTo(dto.getTargetAccountNumber());
        assertThat(entity.getAmount()).isEqualTo(dto.getAmount());
        assertThat(entity.getTimestamp()).isNotNull();
        assertThat(entity.getTransactionId()).isNotNull();
    }

    @Test
    public void testTransferAccountNotFound() throws Exception {
        when(paymentController.getAccount(sourceAccountNumber)).thenReturn(Optional.empty());
        when(paymentController.getAccount(targetAccountNumber)).thenReturn(targetAccount);
        when(paymentController.transfer(sourceAccount.get(), targetAccount.get(), transferAmount)).thenReturn(true);

        verify(paymentController, times(0)).transfer(sourceAccount.get(), targetAccount.get(), transferAmount);

        Response response = paymentService.transfer(dto);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        Error err = (Error) response.getEntity();
        assertThat(err).isNotNull();
        assertThat(err.getCode()).isNotEmpty();
    }

    @Test
    public void testTransferSameSourceAndTargetAccount() throws Exception {
        TransferDTO sameAccounts = new TransferDTO();
        sameAccounts.setSourceAccountNumber(12345);
        sameAccounts.setTargetAccountNumber(12345);
        sameAccounts.setAmount(dto.getAmount());

        verify(paymentController, times(0)).getAccount(sourceAccountNumber);
        verify(paymentController, times(0)).getAccount(targetAccountNumber);
        verify(paymentController, times(0)).transfer(sourceAccount.get(), targetAccount.get(), transferAmount);

        Response response = paymentService.transfer(sameAccounts);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        Error err = (Error) response.getEntity();
        assertThat(err).isNotNull();
        assertThat(err.getCode()).isNotEmpty();
    }

    @Test
    public void testTransferFailed() throws Exception {
        when(paymentController.getAccount(sourceAccountNumber)).thenReturn(sourceAccount);
        when(paymentController.getAccount(targetAccountNumber)).thenReturn(targetAccount);
        when(paymentController.transfer(sourceAccount.get(), targetAccount.get(), transferAmount)).thenReturn(false);

        Response response = paymentService.transfer(dto);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        Error err = (Error) response.getEntity();
        assertThat(err).isNotNull();
        assertThat(err.getCode()).isNotEmpty();
    }

    @Test
    public void testTransferInsufficientBalance() throws Exception {
        when(paymentController.getAccount(sourceAccountNumber)).thenReturn(sourceAccount);
        when(paymentController.getAccount(targetAccountNumber)).thenReturn(targetAccount);
        when(paymentController.transfer(sourceAccount.get(), targetAccount.get(), transferAmount))
                .thenThrow(new InsufficientBalanceException("Insufficient balance in source account"));

        Response response = paymentService.transfer(dto);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        Error err = (Error) response.getEntity();
        assertThat(err).isNotNull();
        assertThat(err.getCode()).isNotEmpty();
    }

    @Test
    public void testTransferException() throws Exception {
        when(paymentController.getAccount(sourceAccountNumber)).thenReturn(sourceAccount);
        when(paymentController.getAccount(targetAccountNumber)).thenReturn(targetAccount);
        when(paymentController.transfer(sourceAccount.get(), targetAccount.get(), transferAmount))
                .thenThrow(new RuntimeException("Runtime exception occurred"));

        Response response = paymentService.transfer(dto);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        Error err = (Error) response.getEntity();
        assertThat(err).isNotNull();
        assertThat(err.getCode()).isNotEmpty();
    }
}
