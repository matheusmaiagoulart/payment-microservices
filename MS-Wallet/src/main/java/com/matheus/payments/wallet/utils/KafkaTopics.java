package com.matheus.payments.wallet.utils;


public class KafkaTopics {

    public static final String WALLET_CREATED_EVENT_TOPIC = "wallet-created";
    public static final String WALLET_CREATION_FAILED_TOPIC = "wallet-creation-failed";
    public static final String USER_CREATED_EVENT_TOPIC = "user-created";

    public static final String DEPOSIT_TOPIC = "deposit-created";
    public static final String DEPOSIT_EXECUTED_TOPIC = "deposit-executed";
    public static final String DEPOSIT_FAILED_TOPIC = "deposit-failed";
}
