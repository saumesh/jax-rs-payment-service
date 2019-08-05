package com.saumesh.payment.config;

import com.saumesh.payment.persistenance.AccountRepository;
import com.saumesh.payment.persistenance.KeyValueStore;
import com.saumesh.payment.persistenance.memory.InMemoryAccountRepository;
import com.saumesh.payment.persistenance.memory.InMemoryKeyValueStore;
import com.saumesh.payment.service.dto.TransferDTO;
import com.saumesh.payment.controller.BasicPaymentController;
import com.saumesh.payment.controller.PaymentController;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.hk2.utilities.binding.ServiceBindingBuilder;

import javax.inject.Singleton;

public class AppBinder extends AbstractBinder {

    @Override
    protected void configure() {
        TypeLiteral<KeyValueStore<String, TransferDTO>> literal = new TypeLiteral<KeyValueStore<String, TransferDTO>>(){};
        ServiceBindingBuilder<InMemoryKeyValueStore> bindingBuilder = (ServiceBindingBuilder<InMemoryKeyValueStore>)bind(InMemoryKeyValueStore.class).to(literal).in(Singleton.class);

        bind(InMemoryAccountRepository.class).to(AccountRepository.class).in(Singleton.class);
        bind(BasicPaymentController.class).to(PaymentController.class).in(Singleton.class);
    }
}
