package com.saumesh.payment.service.integration.test;

import javax.ws.rs.core.Response;
import com.saumesh.payment.domain.Account;
import com.saumesh.payment.service.dto.Error;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountServiceIntegrationTest extends IntegrationTestBase {

    @Test
    public void testGetAccount() {
        long accountNumber = 123457;
        Response response = target("/v1/accounts/" + accountNumber).request().get(Response.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isNotNull();

        Account account = response.readEntity(Account.class);
        assertThat(account).isNotNull();
        assertThat(account.accountNumber()).isEqualTo(accountNumber);
    }

    @Test
    public void testGetAccountNotFound() {
        Response response = target("/v1/accounts/12344567789").request().get(Response.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        assertThat(response.getEntity()).isNotNull();   // Error entity

        Error error = response.readEntity(Error.class);
        assertThat(error).isNotNull();
        assertThat(error.getCode()).isNotNull();
    }
}
