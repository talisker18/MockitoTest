package com.henz;

import java.math.BigDecimal;

import lombok.Getter;

public class CheckoutService {

	  public BigDecimal purchaseProduct(Product product, String customerId) {
	    // any arbitrary implementation
	    PaymentProcessor paymentProcessor = new PaymentProcessor();

	    return paymentProcessor.chargeCustomer(customerId, product.getPrice());
	  }
	  
	  @Getter
	  public enum Product {
		  
		  MAC_BOOK("MacBook Pro", BigDecimal.valueOf(1500)),
		  SSD("some SSD", BigDecimal.valueOf(300));
		  
		  private String name;
		  private BigDecimal price;
		  
		  Product(String name, BigDecimal price){
			  this.name = name;
			  this.price = price;
		  }
		  
	  }
}
