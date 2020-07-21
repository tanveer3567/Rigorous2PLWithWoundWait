import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class Rigorous2PLWithWoundWait {

	//Transaction table
	static List<TransactionDetails> transactionList = new ArrayList<TransactionDetails>();
	//lock table
	static List<Lock> lockList = new ArrayList<Lock>();
	
	//Execution starts here
	public static void main(String args[]) throws IOException, InterruptedException {

			try{
				if(args.length > 0) {
					Scanner scan = new Scanner(new File("input"+args[0]+".txt"));
					while (scan.hasNext()) {
						//scans each line of the input file
						String line = scan.nextLine();
						System.out.println("Operation: "+line);
						char[] charArray = removeSpaces(line.toLowerCase().toCharArray()).toCharArray();
						int tId = Character.getNumericValue(charArray[1]);
						route(charArray[0], tId, charArray);
					}
					scan.close();
				} else {
					System.out.println("Please provide valid input file number");
				}
			} catch (Exception e) {
				System.out.println("Please provide valid input file number");
			}
	}

	//removes any spaces from the input line.
	private static String removeSpaces(char[] charArray) {

		String finalOperation = "";
		for (int i = 0; i < charArray.length; i++) {
			if (charArray[i] != ' ') {
				finalOperation += charArray[i];
			}
		}
		return finalOperation;
	}

	//routes the operations of the transaction
	private static void route(char op, int tId, char[] charArray) {

		if (op == 'b') {
			beginTransactionId(tId);
		} else if (op == 'e') {
			endOrAbortTransaction(tId, "COMMITED");
		} else if (op == 'r') {
			char dataItem = charArray[3];
			readTransaction(tId, dataItem);
		} else if (op == 'w') {
			char dataItem = charArray[3];
			writeTransaction(tId, dataItem);
		}
		display();
	}

	//performs the write operation of the transaction.
	private static void writeTransaction(int tId, char dataItem) {

		TransactionDetails currentTDetails = null;
		for (TransactionDetails tDetails : transactionList) {
			if (tDetails.gettId() == tId) {
				currentTDetails = tDetails;
				break;
			}
		}
		if (currentTDetails != null) {
			String state = currentTDetails.getState();
			Lock lock = null;
			for (Lock tempLock : lockList) {
				if (tempLock.getDataItem() == dataItem) {
					lock = tempLock;
					break;
				}
			}
			String operation = "w" + tId + "(" + dataItem + ")";
			if (state.equalsIgnoreCase("ACTIVE")) {
				if (lock != null) {
					boolean flag = false;
					if (lock.getLockType().equals("Read_Lock")) {
						if (lock.getAccessingTIds().size() > 1) {
							Boolean finalFlag = true;
							while (true) {
								if (lock.getAccessingTIds().size() != 1) {
									for (int i = 0; i < lock.getAccessingTIds().size(); i++) {
										if (tId != lock.getAccessingTIds().get(i)) {
											flag = woundWait(tId, lock.getAccessingTIds().get(i), operation);
											if (!flag) {
												finalFlag = false;
												break;
											} else {
												break;
											}
										}
									}
								} else {
									break;
								}
								if (!finalFlag) {
									break;
								}
							}
							if (finalFlag) {
								lock.setLockType("Write_Lock");
								if (!lock.getAccessingTIds().contains(tId)) {
									lock.getAccessingTIds().add(tId);
								}
								if (!currentTDetails.getDataItemsHeld().contains(dataItem)) {
									currentTDetails.getDataItemsHeld().add(dataItem);
								}
							}
						} else if (lock.getAccessingTIds().size() == 1) {
							if (lock.getAccessingTIds().get(0) == tId) {
								lock.setLockType("Write_Lock");
							} else {
								flag = woundWait(tId, lock.getAccessingTIds().get(0), operation);
								if (flag) {
									lock.setLockType("Write_Lock");
									if (!lock.getAccessingTIds().contains(tId)) {
										lock.getAccessingTIds().add(tId);
									}
									if (!currentTDetails.getDataItemsHeld().contains(dataItem)) {
										currentTDetails.getDataItemsHeld().add(dataItem);
									}
								}
							}
						}
					} else if (lock.getLockType().equals("Write_Lock")) {
						woundWait(tId, lock.getAccessingTIds().get(0), operation);
						if (flag) {
							lock.setLockType("Write_Lock");
							if (!lock.getAccessingTIds().contains(tId)) {
								lock.getAccessingTIds().add(tId);
							}
							if (!currentTDetails.getDataItemsHeld().contains(dataItem)) {
								currentTDetails.getDataItemsHeld().add(dataItem);
							}
						}
					} else {
						lock.setLockType("Write_Lock");
						if (!lock.getAccessingTIds().contains(tId)) {
							lock.getAccessingTIds().add(tId);
						}
						if (!currentTDetails.getDataItemsHeld().contains(dataItem)) {
							currentTDetails.getDataItemsHeld().add(dataItem);
						}
					}
				} else {
					Lock newLock = new Lock(dataItem, "Wite_Lock", new ArrayList<Integer>(), new LinkedList<Integer>());
					newLock.getAccessingTIds().add(tId);
					lockList.add(newLock);
					if (!currentTDetails.getDataItemsHeld().contains(dataItem)) {
						currentTDetails.getDataItemsHeld().add(dataItem);
					}
				}
			} else if (state.equalsIgnoreCase("BLOCKED")) {
				currentTDetails.getWaitingOperations().add(operation);
				if (lock == null) {
					Lock newLock = new Lock(dataItem, "Unlock", new ArrayList<Integer>(),
							new LinkedList<Integer>());
					newLock.getWaitingTids().add(tId);
					lockList.add(newLock);
				} else {
					lock.getWaitingTids().add(tId);
				}
			}
		}
	}

	// perform the read operation  of the transaction
	private static void readTransaction(int tId, char dataItem) {

		TransactionDetails currentTDetails = null;
		for (TransactionDetails tDetails : transactionList) {
			if (tDetails.gettId() == tId) {
				currentTDetails = tDetails;
				break;
			}
		}
		if (currentTDetails != null) {
			String state = currentTDetails.getState();
			Lock lock = null;
			for (Lock tempLock : lockList) {
				if (tempLock.getDataItem() == dataItem) {
					lock = tempLock;
					break;
				}
			}
			String operation = "r" + tId + "(" + dataItem + ")";
			if (state.equalsIgnoreCase("ACTIVE")) {
				if (lock != null) {
					if (lock.getLockType().equals("Read_Lock")) {
						lock.getAccessingTIds().add(tId);
						if (!currentTDetails.getDataItemsHeld().contains(dataItem)) {
							currentTDetails.getDataItemsHeld().add(dataItem);
						}
					} else if (lock.getLockType().equals("Write_Lock")) {
						woundWait(tId, lock.getAccessingTIds().get(0), operation);
					} else {
						lock.setLockType("Read_Lock");
						lock.getAccessingTIds().add(tId);
						if (!currentTDetails.getDataItemsHeld().contains(dataItem)) {
							currentTDetails.getDataItemsHeld().add(dataItem);
						}
					}
				} else {
					Lock newLock = new Lock(dataItem, "Read_Lock", new ArrayList<Integer>(), new LinkedList<Integer>());
					newLock.getAccessingTIds().add(tId);
					lockList.add(newLock);
					if (!currentTDetails.getDataItemsHeld().contains(dataItem)) {
						currentTDetails.getDataItemsHeld().add(dataItem);
					}
				}
			} else if (state.equalsIgnoreCase("BLOCKED")) {
				currentTDetails.getWaitingOperations().add(operation);
				if (lock == null) {
					Lock newLock = new Lock(dataItem, "Unlock", new ArrayList<Integer>(), new LinkedList<Integer>());
					newLock.getWaitingTids().add(tId);
					lockList.add(newLock);
				} else {
					lock.getWaitingTids().add(tId);
				}
			}
		}
	}

	//Applies wound wait protocol on ti and tj and eiher ti waits or tj gets aborted
	private static boolean woundWait(int tIdi, int tIdj, String operation) {

		TransactionDetails currentTDetailsi = null;
		for (TransactionDetails tDetails : transactionList) {
			if (tDetails.gettId() == tIdi) {
				currentTDetailsi = tDetails;
				break;
			}
		}
		TransactionDetails currentTDetailsj = null;
		for (TransactionDetails tDetails : transactionList) {
			if (tDetails.gettId() == tIdj) {
				currentTDetailsj = tDetails;
				break;
			}
		}
		if (currentTDetailsi.getTimestamp().compareTo(currentTDetailsj.getTimestamp()) < 0) {
			endOrAbortTransaction(tIdj, "ABORTED");
			return true;
		} else {
			currentTDetailsj.getWaitingTIds().add(currentTDetailsi.gettId());
			currentTDetailsi.setState("BLOCKED");
			currentTDetailsi.getWaitingOperations().add(operation);
			for (Lock lock : lockList) {
				if (lock.getDataItem().equals(operation.charAt(operation.length() - 2))) {
					lock.getWaitingTids().add(tIdi);
					break;
				}
			}
			return false;
		}
	}
	
	//Either COMMITS or ABORTS the tranasaction
	private static void endOrAbortTransaction(int tId, String state) {

		TransactionDetails currentTDetails = null;
		for (TransactionDetails tDetails : transactionList) {
			if (tDetails.gettId() == tId) {
				currentTDetails = tDetails;
				break;
			}
		}
		if (currentTDetails != null) {
			if ((currentTDetails.getState().equalsIgnoreCase("ACTIVE") && state.equals("ABORTED"))
					|| (currentTDetails.getState().equalsIgnoreCase("BLOCKED") && state.equals("ABORTED"))
					|| (currentTDetails.getState().equalsIgnoreCase("ACTIVE") && state.equals("COMMITED"))) {
				currentTDetails.setState(state);
				currentTDetails.setDataItemsHeld(null);
				currentTDetails.setWaitingOperations(null);
				for (TransactionDetails localTDetails : transactionList) {
					localTDetails.getWaitingTIds().remove(tId);
				}
				for (Lock lock : lockList) {
					lock.getAccessingTIds().remove(new Integer(tId));
					lock.getWaitingTids().remove(new Integer(tId));
					if (lock.getAccessingTIds().size() == 0) {
						lock.setLockType("Unlock");
					}
				}
				while (true) {
					Queue<Integer> waitingTIds = currentTDetails.getWaitingTIds();
					if (waitingTIds.isEmpty()) {
						break;
					} else {
						Integer tempTId = waitingTIds.poll();
						TransactionDetails tempTDetails = null;
						for (TransactionDetails localTDetails : transactionList) {
							if (localTDetails.gettId() == tempTId) {
								tempTDetails = localTDetails;
								break;
							}
						}
						tempTDetails.setState("ACTIVE");
						while (!tempTDetails.getWaitingOperations().isEmpty()) {
							if (!tempTDetails.getState().equals("BLOCKED")) {
								String waitingOps = tempTDetails.getWaitingOperations().poll();
								for (Lock lock : lockList) {
									if (lock.getDataItem()
											.equals(waitingOps.toLowerCase().charAt(waitingOps.length() - 2))) {
										lock.getWaitingTids().remove(tempTDetails.gettId());
										break;
									}
								}
								System.out.println("As T"+tId+" is "+state+" waiting transaction on T"+tId+" that is T"+
								tempTDetails.gettId()+" is unblocked and tries to excecute its queued operation "+waitingOps);
								route(waitingOps.charAt(0), tempTDetails.gettId(),
										waitingOps.toLowerCase().toCharArray());
							} else {
								break;
							}
						}
					}

				}
			} else if (currentTDetails.getState().equalsIgnoreCase("BLOCKED") && state.equals("COMMITED")) {
				currentTDetails.getWaitingOperations().add("e" + tId + ";");
			}
		}
	}

	//Begins the tranasction
	private static void beginTransactionId(int tId) {
		transactionList.add(new TransactionDetails(tId, new Timestamp(System.currentTimeMillis()), "Active",
				new ArrayList<Character>(), new LinkedList<String>(), new LinkedList<Integer>()));
	}

	//prints snap shots of the Transaction table and Lock table on to the console.
	private static void display() {
		
		System.out.println(
				"*******************************************************************************************************************************");
		System.out.println();

		System.out.println("TRANSACTION TABLE :");
		System.out.println();
		System.out.println(
				"Id" + "	 " + "Timestamp" + "			" + "State" + "			"
						+ "Locked Data Items" + "		" + "Queued Operations");
		
		for (TransactionDetails tDetail : transactionList) {
			System.out.print(tDetail.gettId() + "	 ");
			System.out.print(tDetail.getTimestamp() + "	");
			System.out.print(tDetail.getState() + "			");
			System.out.print(tDetail.getDataItemsHeld() + "				");
			System.out.println(tDetail.getWaitingOperations());
		}

		System.out.println();
		System.out.println();

		System.out.println("LOCK TABLE :");
		System.out.println();
		System.out.println("Data Item" + "      " + "Type" + "                    "
				+ "Accessing Tid's" + "          " + "Waiting Tid's");
		for (Lock lock : lockList) {
			System.out.print(lock.getDataItem() + "              ");
			System.out.print(lock.getLockType() + "                       ");
			System.out.print(lock.getAccessingTIds() + "                   ");
			System.out.println(lock.getWaitingTids());
		}

		System.out.println();
		System.out.println();
	}
}
