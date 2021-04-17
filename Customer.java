/*
 * Bushra Hameed
 * A bank is simulated by using threads and semaphores to model customer and employee behavior.
 */
  
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Customer implements Runnable {
	private int task, i, num;
	
	//global semaphores
	public static Semaphore maxCustomers = new Semaphore(5, true);
	public static Semaphore queueNotEmpty = new Semaphore(0, true);
	public static Semaphore queue1NotEmpty = new Semaphore(0, true);
	public static Semaphore[] banktellerRequest = new Semaphore[] {new Semaphore(0), new Semaphore(0)};
	public static Semaphore[] customerDepositReceipt = new Semaphore[] {new Semaphore(0), new Semaphore(0)};
	public static Semaphore[] customerDepositComplete = new Semaphore[] {new Semaphore(0), new Semaphore(0)};
	public static Semaphore[] withdrawalReceipt = new Semaphore[] {new Semaphore(0), new Semaphore(0)};
	public static Semaphore[] withdrawalComplete = new Semaphore[] {new Semaphore(0), new Semaphore(0)};
	public static Semaphore[] tellerReady = new Semaphore[]{ new Semaphore(0), new Semaphore(0), new Semaphore(0), new Semaphore(0),new Semaphore(0)};
	public static Semaphore[] loanOfficerReady = new Semaphore[]{ new Semaphore(0), new Semaphore(0), new Semaphore(0), new Semaphore(0),new Semaphore(0)};
	public static Semaphore loanOfficerRequest = new Semaphore(0, true);
	public static Semaphore loanOfficerReceipt = new Semaphore(0, true);
	public static Semaphore loanTransactionComplete = new Semaphore(0, true);
	public static Semaphore mutex= new Semaphore(1, true);
	public static Semaphore mutex1= new Semaphore(1, true);
	
	//global queues
	public static Queue<Integer> bankTellQueue = new LinkedList<Integer>(); 
	public static Queue<Integer> loanOfficerQueue = new LinkedList<Integer>(); 
		
	//global arrays
	public static int[] customerTasks = new int[10]; 
	public static int[] servingTeller = new int[10]; 
	public static int[] customerDeposit = new int[10]; 
	public static int[] withdraw = new int[10]; 
	public static int[] customerBalance = new int[10]; 
	public static int[] loanAmt = new int[10]; 
	public static int[] loanTotal = new int[10]; 
	
	
	Customer(int num) {
		//initialize customer instances
		this.num = num;
		customerBalance[num] = 1000;
		loanTotal[num] = 0;
	}
	
	public void run() {
		for(i = 0; i < 3; i++) {
			try {
				maxCustomers.acquire(); //limit the max number of threads that are running 
				task = randTaskAssign(); 
				customerTasks[num] = task;
				
				//if customerDeposited task
				if(task == 1) {
					mutex.acquire(); //critical section 
					bankTellQueue.add(num);
					queueNotEmpty.release(); //teller is notified that queue is not empty 
					mutex.release();
					tellerReady[num].acquire(); 
					customerDeposit[num] = 100 * (1 + (int)(Math.random() * 5));
					System.out.println("Customer "+ num + " requests from the teller " + servingTeller[num] + " to make a customerDeposit of $" + customerDeposit[num]);
					Thread.sleep(100);
					banktellerRequest[servingTeller[num]].release(); //teller processes customerDeposit 
					customerDepositReceipt[servingTeller[num]].acquire(); //wait for teller to finish processing customerDeposit
					Thread.sleep(100);
					System.out.println("Customer "+ num + " gets their receipt from the teller " + servingTeller[num]);
					customerDepositComplete[servingTeller[num]].release(); //onto the next customer
				}
				
				//withdrawal being made
				if(task == 2) {
					mutex.acquire();
					bankTellQueue.add(num);
					queueNotEmpty.release();
					mutex.release();
					tellerReady[num].acquire();
					withdraw[num] = 100 * (1 + (int)(Math.random()*5));
					System.out.println("Customer "+ num + " requests from the teller " + servingTeller[num] + " to make a withdrawal of $" + withdraw[num]);
					Thread.sleep(100);
					banktellerRequest[servingTeller[num]].release();
					withdrawalReceipt[servingTeller[num]].acquire();
					Thread.sleep(100);
					System.out.println("Customer "+ num + " gets their cash and receipt from the teller " + servingTeller[num]);
					withdrawalComplete[servingTeller[num]].release();
				}
				
				//loan task
				if(task == 3) {
					mutex1.acquire(); //critical section
					loanOfficerQueue.add(num);
					queue1NotEmpty.release(); //queue is not empty
					mutex1.release();
					loanOfficerReady[num].acquire(); //wait until ready for thread
					loanAmt[num] = 100 * (1 + (int)(Math.random()*5));
					System.out.println("Customer "+ num + " requests from the Loan Officer to apply for a loan of $" + loanAmt[num]);
					Thread.sleep(100);
					loanOfficerRequest.release(); //begin loan processing
					loanOfficerReceipt.acquire(); //wait until done processing
					Thread.sleep(100);
					System.out.println("Customer "+ num + " gets their loan from the Loan Officer");
					loanTransactionComplete.release(); //onto the next customer 
				}
				maxCustomers.release();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Customer "+ num +" exits the bank");
	}
	
	//randomly assign tasks 
	private int randTaskAssign() {
		int randNum;
		randNum = 1 + (int)(Math.random()*3);
		return randNum;
	}
	
	public static void main(String args[]) {
		int i = 0, currentBalance = 0, currentLoan = 0;
		final int num_Customers = 5;
		
		//Loan Officer thread   
		LoanOfficer officer = new LoanOfficer();   
		Thread myThread2 = new Thread();
		myThread2 = new Thread(officer);
		myThread2.setDaemon(true);
		myThread2.start();
		System.out.println("Loan Officer created ");
		
		//Bank Teller thread
		Bankteller teller[] = new Bankteller[2];
		Thread myThread1[] = new Thread[2];
		
		for(i = 0; i < 2; ++i) {
			teller[i] = new Bankteller(i);
			myThread1[i] = new Thread( teller[i] );
			myThread1[i].setDaemon(true);
			myThread1[i].start();
			
			System.out.println("Teller "+ i +" created ");
		}

  
		//Customers thread
		Customer cust[] = new Customer[num_Customers];
		Thread myThread[] = new Thread[num_Customers];
		
		for(i = 0; i < num_Customers; ++i) {
			cust[i] = new Customer(i);
			myThread[i] = new Thread( cust[i] );
			myThread[i].start();
			
			System.out.println("Customer "+ i +" created");
		}
		
		//join Customers thread
		for(i = 0; i < num_Customers; ++i) {
			try {
				myThread[i].join();
				System.out.println("Customer "+ i +" joined by main");
			}
			catch (InterruptedException e) {
			}
		}
		
		//print summary
		System.out.println("\n\t Bank Simulation Summary\n");
		System.out.println("\t\tEnding Balance \tLoan Amount\n");
		
		for(i = 0; i < 5; i++) {
			System.out.println("Customer "+i+"\t"+customerBalance[i]+"\t\t"+loanTotal[i]);
			currentBalance = currentBalance + customerBalance[i];
			currentLoan = currentLoan + loanTotal[i];
		}   
		System.out.println("\nTotals\t\t"+currentBalance+"\t\t"+ currentLoan);
	}
}

//Bank teller class implements the thread 
class Bankteller implements Runnable {
	private int nextcustomer, nextcustomertask;
	private int num;
	
	Bankteller(int num) {
		this.num = num;	
	}
	
	public void run() {
		while(true) {
			try {
				Customer.queueNotEmpty.acquire(); //wait until not empty
				Customer.mutex.acquire(); //critical section
				nextcustomer = Customer.bankTellQueue.remove();
				Customer.mutex.release();
				nextcustomertask = Customer.customerTasks[nextcustomer];
				Customer.servingTeller[nextcustomer] = num;
				System.out.println( "Teller " + num + " Begins serving Customer "+ nextcustomer);
				Customer.tellerReady[nextcustomer].release(); //teller is ready for the next customer
				
				//handling customerDeposits
				if(nextcustomertask == 1) {
					Customer.banktellerRequest[num].acquire(); 
					System.out.println( "Teller " + num + " processes customerDeposit for Customer "+ nextcustomer);
					Thread.sleep(400);
					Customer.customerBalance[nextcustomer] = Customer.customerBalance[nextcustomer] + Customer.customerDeposit[nextcustomer];
					Customer.customerDepositReceipt[num].release(); //processing complete
					Customer.customerDepositComplete[num].acquire(); 
				}
				
				//handling withdrawal 
				if(nextcustomertask == 2) {
					Customer.banktellerRequest[num].acquire();
					System.out.println( "Teller " + num + " processes withdrawal for Customer "+ nextcustomer);
					Thread.sleep(400);
					
					Customer.customerBalance[nextcustomer] = Customer.customerBalance[nextcustomer] - Customer.withdraw[nextcustomer];
					Customer.withdrawalReceipt[num].release();
					Customer.withdrawalComplete[num].acquire();
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

//Loan Officer implements the thread
class LoanOfficer implements Runnable {
	private int nextcustomer;
	private int nextcustomertask;
	public void run() {
		while(true) {
			try {
				Customer.queue1NotEmpty.acquire(); //wait until queue is not empty
				Customer.mutex1.acquire(); //critical section
				nextcustomer = Customer.loanOfficerQueue.remove();
				Customer.mutex1.release();
				nextcustomertask = Customer.customerTasks[nextcustomer];
				System.out.println( "Loan Officer Begins serving Customer "+ nextcustomer);
				Customer.loanOfficerReady[nextcustomer].release(); //ready for next customer
				
				if(nextcustomertask == 3) {
					Customer.loanOfficerRequest.acquire(); 
					System.out.println( "Loan Officer approves loan for Customer "+ nextcustomer);
					Thread.sleep(400);
					
					Customer.loanTotal[nextcustomer] = Customer.loanTotal[nextcustomer] + Customer.loanAmt[nextcustomer];
					Customer.loanOfficerReceipt.release(); //processing complete
					Customer.loanTransactionComplete.acquire(); 
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
