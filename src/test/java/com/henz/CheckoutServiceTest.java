package com.henz;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedConstruction.Context;
import org.mockito.MockedConstruction.MockInitializer;
import org.mockito.stubbing.Answer;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/*
 * see also https://github.com/talisker18/HappyHotelApp
 * 
 * and /codecademy_simplilearn_training/src/test/java/codecademy_simplilearn_training/mockito/demo_mocking_constructors/ConstructorMockTest.java
 * 
 * for more mockito tests
 * 
 * */

public class CheckoutServiceTest {
	
	@Test
	public void testWithoutMock() {
		final CheckoutService s = new CheckoutService();
		
		BigDecimal expectedPriceOfMacBook = CheckoutService.Product.MAC_BOOK.getPrice();
		
		BigDecimal actualPriceOfMacBook = s.purchaseProduct(CheckoutService.Product.MAC_BOOK, "1");
		
		Assertions.assertThat(actualPriceOfMacBook).isEqualTo(expectedPriceOfMacBook);
	}
	
	@Test
	public void testOneWithMock() {
		
		//creation of PaymentProcessor objects are all mocked inside the try block. To control the behavior of the mocks, an MockInitializer is used
		//outside the block, the objects are NOT mocked
		
		try(MockedConstruction<PaymentProcessor> mockedConstruction = Mockito.mockConstruction(
				PaymentProcessor.class,
				new MockInitializer<PaymentProcessor>() { //for better understanding instantiation of anonymous class is used. lambda would be (mock, context) -> {...}

					@Override
					public void prepare(PaymentProcessor mock, Context context) throws Throwable {
						//control behavior of mocks by using stubbing. Allways return '10'
						when(mock.chargeCustomer(anyString(), any(BigDecimal.class))).thenReturn(BigDecimal.TEN);
					}
				})){//block of try with resource begins here. This will call .close method of MockedConstruction because MockedConstruction implements AutoClosable
			
			
			
			CheckoutService cut = new CheckoutService();
			
			//params for purchaseProduct method
			CheckoutService.Product product = CheckoutService.Product.MAC_BOOK;
			String userId = "42";
			 
		    BigDecimal result = cut.purchaseProduct(product, userId); // inside purchaseProduct(), a new mocked object of PaymentProcessor will be created and added to the colletion hold by MockedConstruction 
		 
		    Assertions.assertThat(BigDecimal.TEN).isEqualTo(result);
		    
		    //check if MockedConstruction holds 1 mock
		    Assertions.assertThat(mockedConstruction.constructed().size() == 1);
		    
		    //verify that the mocked object on index 0 has called 1 time the chargeCustomer() methods with params defined above
		    verify(mockedConstruction.constructed().get(0), times(1)).chargeCustomer(userId, product.getPrice());
			
		    //just create some more mocked objects to see if they were added to the MockedConstruction collection
		    PaymentProcessor p2 = new PaymentProcessor();
		    PaymentProcessor p3 = new PaymentProcessor();
		    PaymentProcessor p4 = new PaymentProcessor();
		    
		    Assertions.assertThat(mockedConstruction.constructed().size() == 4);
		}
	}
	
	@Test
	public void testWithAnswer() {
		
		try(MockedConstruction<PaymentProcessor> mockedConstruction = Mockito.mockConstructionWithAnswer(PaymentProcessor.class,
				new Answer<BigDecimal>() { //this is executed when the first mock (p1) calls a method that returns a BigDecimal
			//we have two methods of these: chargeCustomer and chargeCustomerTwo. Both methods are mocked and answered with the first Answer because
			//p1 is the first mock...and so on

					@Override
					public BigDecimal answer(InvocationOnMock invocation) throws Throwable {
						System.out.println("first mock invocation params size: "+invocation.getArguments().length);
						System.out.println(invocation.getMethod());
						System.out.println("print params: ");
						Arrays.asList(invocation.getArguments()).forEach(System.out::println);
						System.out.println("-------------------");
						return BigDecimal.valueOf(500);
					}
				},
				new Answer<BigDecimal>() { //second mock of PaymentProcessor (p2). Only allowed to mock methods that return BigDecimal

					@Override
					public BigDecimal answer(InvocationOnMock invocation) throws Throwable {
						System.out.println("second mock invocation params size: "+invocation.getArguments().length);
						System.out.println(invocation.getMethod());
						System.out.println("print params: ");
						Arrays.asList(invocation.getArguments()).forEach(System.out::println);
						System.out.println("-------------------");
						return BigDecimal.valueOf(800);
					}
					},
				
				//3rd mock, only allowed to mock methods that return Long
				new Answer<Long>() {

					@Override
					public Long answer(InvocationOnMock invocation) throws Throwable {
						System.out.println("third mock invocation params size: "+invocation.getArguments().length);
						System.out.println(invocation.getMethod());
						System.out.println("print params: ");
						Arrays.asList(invocation.getArguments()).forEach(System.out::println);
						System.out.println("-------------------");
						return 100L;
					}
				}
				)){
			
			PaymentProcessor p1 = new PaymentProcessor("test", BigDecimal.TEN);
			p1.chargeCustomer("99", BigDecimal.ONE);
			p1.chargeCustomerTwo("99", BigDecimal.TEN);
			p1.chargeCustomerTwo("99", BigDecimal.TEN);
			// p1.doSomething(); // --> wont work because we can only mock methods with return type BigDecimal for the 1ist mock
			
			PaymentProcessor p2 = new PaymentProcessor();
			p2.chargeCustomer("99", BigDecimal.valueOf(4587));
			
			PaymentProcessor p3 = new PaymentProcessor();
			p3.doSomething();
			// p3.chargeCustomer("99", BigDecimal.valueOf(4587)); // --> wont work because we can only mock methods with return type Long for the 3rd mock
		}
	}
	
	//also with mockConstructionWithAnswer, but lambda style
	@Test
	void testWithAnswerLambdaStyle() {
	  try (MockedConstruction<PaymentProcessor> mocked = Mockito.mockConstructionWithAnswer(PaymentProcessor.class,
	    // default answer for the first mock
	    invocation -> {
	    	System.out.println("first invoc");
	    	Arrays.asList(invocation.getArguments()).forEach(System.out::println);
	    	return new BigDecimal("500.00");
	    },
	    // additional answer for all further mocks
	    invocation -> new BigDecimal("42.00"))) {
	 
	    PaymentProcessor firstInstance = new PaymentProcessor();
	    PaymentProcessor secondInstance = new PaymentProcessor();
	    PaymentProcessor thirdInstance = new PaymentProcessor();
	    
	    Assertions.assertThat(new BigDecimal("500.00")).isEqualTo(firstInstance.chargeCustomer("42", BigDecimal.ZERO));
	    Assertions.assertThat(new BigDecimal("42.00")).isEqualTo(secondInstance.chargeCustomer("42", BigDecimal.ZERO));
	    Assertions.assertThat(new BigDecimal("42.00")).isEqualTo(thirdInstance.chargeCustomer("42", BigDecimal.ZERO));
	  }
	}

}
