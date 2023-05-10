package io.temporal.samples.moneytransfer;

public class Account {

  private String accountId;
  private int balance;

  public Account() {}

  public Account(String accountId, int balance) {
    this.accountId = accountId;
    this.balance = balance;
  }

  public String getAccountId() {
    return accountId;
  }

  public int getBalance() {
    return balance;
  }

  public String toString() {
    return accountId + " balance: " + balance;
  }
}
