package com.saumesh.payment.service.integration.test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.saumesh.payment.domain.Account;
import com.saumesh.payment.service.dto.Error;
import com.saumesh.payment.service.dto.TransferDTO;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentServiceIntegrationTest extends IntegrationTestBase {

    @Test
    public void testTransfer() {
        TransferDTO dto = new TransferDTO();
        dto.setAmount(20);
        dto.setSourceAccountNumber(1234);
        dto.setTargetAccountNumber(12345);

        Entity<TransferDTO> requestBody = Entity.entity(dto, MediaType.APPLICATION_JSON);

        Response response = target("/v1/payments/transfer").request().post(requestBody);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        TransferDTO responseDto = response.readEntity(TransferDTO.class);
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getSourceAccountNumber()).isEqualTo(dto.getSourceAccountNumber());
        assertThat(responseDto.getTargetAccountNumber()).isEqualTo(dto.getTargetAccountNumber());
        assertThat(responseDto.getAmount()).isEqualTo(dto.getAmount());
        assertThat(responseDto.getTimestamp()).isNotNull();
        assertThat(responseDto.getTransactionId()).isNotNull();
    }

    @Test
    public void testTransferAndVerifyBalance() {
        long srcAccountNumber = 1234;
        long tarAccountNumber = 12345;

        //1- Get Source and Target account balance
        // Source Account
        Response srcAccountResponse = target("/v1/accounts/" + srcAccountNumber).request().get(Response.class);
        assertThat(srcAccountResponse).isNotNull();
        assertThat(srcAccountResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(srcAccountResponse.getEntity()).isNotNull();
        Account srcAccount = srcAccountResponse.readEntity(Account.class);
        assertThat(srcAccount).isNotNull();
        assertThat(srcAccount.accountNumber()).isEqualTo(srcAccountNumber);

        // Target Account
        Response tarAccountResponse = target("/v1/accounts/" + tarAccountNumber).request().get(Response.class);
        assertThat(tarAccountResponse).isNotNull();
        assertThat(tarAccountResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(tarAccountResponse.getEntity()).isNotNull();
        Account tarAccount = tarAccountResponse.readEntity(Account.class);
        assertThat(tarAccount).isNotNull();
        assertThat(tarAccount.accountNumber()).isEqualTo(tarAccountNumber);


        //2- Transfer amount from Source to target
        double transferAmount = 25;
        TransferDTO dto = new TransferDTO();
        dto.setAmount(transferAmount);
        dto.setSourceAccountNumber(srcAccountNumber);
        dto.setTargetAccountNumber(tarAccountNumber);

        Entity<TransferDTO> requestBody = Entity.entity(dto, MediaType.APPLICATION_JSON);

        Response response = target("/v1/payments/transfer").request().post(requestBody);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        TransferDTO responseDto = response.readEntity(TransferDTO.class);
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getSourceAccountNumber()).isEqualTo(dto.getSourceAccountNumber());
        assertThat(responseDto.getTargetAccountNumber()).isEqualTo(dto.getTargetAccountNumber());
        assertThat(responseDto.getAmount()).isEqualTo(dto.getAmount());
        assertThat(responseDto.getTimestamp()).isNotNull();
        assertThat(responseDto.getTransactionId()).isNotNull();


        //3- Get Source and Target account balance after transfer
        // Source Account
        Response srcAccountRsp = target("/v1/accounts/" + srcAccountNumber).request().get(Response.class);
        assertThat(srcAccountRsp).isNotNull();
        assertThat(srcAccountRsp.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(srcAccountRsp.getEntity()).isNotNull();
        Account source = srcAccountRsp.readEntity(Account.class);
        assertThat(source).isNotNull();
        assertThat(source.accountNumber()).isEqualTo(srcAccountNumber);
        assertThat(source.getBalance()).isNotEqualTo(srcAccount.getBalance());
        assertThat(source.getBalance()).isEqualTo(srcAccount.getBalance() - transferAmount);

        // Target Account
        Response tarAccountRsp = target("/v1/accounts/" + tarAccountNumber).request().get(Response.class);
        assertThat(tarAccountRsp).isNotNull();
        assertThat(tarAccountRsp.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(tarAccountRsp.getEntity()).isNotNull();
        Account target = tarAccountRsp.readEntity(Account.class);
        assertThat(target).isNotNull();
        assertThat(target.accountNumber()).isEqualTo(tarAccountNumber);
        assertThat(target.getBalance()).isNotEqualTo(tarAccount.getBalance());
        assertThat(target.getBalance()).isEqualTo(tarAccount.getBalance() + transferAmount);
    }

    @Test
    public void testTransferInvalidAccount() {
        TransferDTO dto = new TransferDTO();
        dto.setAmount(20);
        dto.setSourceAccountNumber(0);
        dto.setTargetAccountNumber(12345);

        Entity<TransferDTO> requestBody = Entity.entity(dto, MediaType.APPLICATION_JSON);

        Response response = target("/v1/payments/transfer").request().post(requestBody);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        Error error = response.readEntity(Error.class);
        assertThat(error).isNotNull();
        assertThat(error.getCode()).isEqualTo("INVALID_ACCOUNT");
    }

    @Test
    public void testTransferAccountNotFound() {
        TransferDTO dto = new TransferDTO();
        dto.setAmount(20);
        dto.setSourceAccountNumber(234556789);
        dto.setTargetAccountNumber(12345);

        Entity<TransferDTO> requestBody = Entity.entity(dto, MediaType.APPLICATION_JSON);

        Response response = target("/v1/payments/transfer").request().post(requestBody);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        Error error = response.readEntity(Error.class);
        assertThat(error).isNotNull();
        assertThat(error.getCode()).isEqualTo("INVALID_ACCOUNT");
    }

    @Test
    public void testTransferSameSourceAndTargetAccount() throws Exception {
        TransferDTO sameAccounts = new TransferDTO();
        sameAccounts.setSourceAccountNumber(12345);
        sameAccounts.setTargetAccountNumber(12345);
        sameAccounts.setAmount(25);

        Entity<TransferDTO> requestBody = Entity.entity(sameAccounts, MediaType.APPLICATION_JSON);

        Response response = target("/v1/payments/transfer").request().post(requestBody);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        Error error = response.readEntity(Error.class);
        assertThat(error).isNotNull();
        assertThat(error.getCode()).isEqualTo("INVALID_ACCOUNT");
    }

    @Test
    public void testTransferInvalidAmount() {
        TransferDTO dto = new TransferDTO();
        dto.setAmount(-45);
        dto.setSourceAccountNumber(1234);
        dto.setTargetAccountNumber(12345);

        Entity<TransferDTO> requestBody = Entity.entity(dto, MediaType.APPLICATION_JSON);

        Response response = target("/v1/payments/transfer").request().post(requestBody);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        Error error = response.readEntity(Error.class);
        assertThat(error).isNotNull();
        assertThat(error.getCode()).isEqualTo("INVALID_AMOUNT");
    }

    @Test
    public void testTransferInsufficientBalance() {
        TransferDTO dto = new TransferDTO();
        dto.setAmount(5000);
        dto.setSourceAccountNumber(1234);
        dto.setTargetAccountNumber(12345);

        Entity<TransferDTO> requestBody = Entity.entity(dto, MediaType.APPLICATION_JSON);

        Response response = target("/v1/payments/transfer").request().post(requestBody);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        Error error = response.readEntity(Error.class);
        assertThat(error).isNotNull();
        assertThat(error.getCode()).isEqualTo("INSUFFICIENT_BALANCE");
    }
}

