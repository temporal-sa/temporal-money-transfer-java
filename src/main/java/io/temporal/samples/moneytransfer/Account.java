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

  public void setBalance(int balance) {
    this.balance = balance;
  }

  @Override
  public String toString() {
    return accountId + " ($" + balance + ")";
  }
}
