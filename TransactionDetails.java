import java.sql.Timestamp;
import java.util.List;
import java.util.Queue;

public class TransactionDetails {

	private int tId;
	private Timestamp timestamp;
	private String state;
	private List<Character> dataItemsHeld;
	private Queue<String> waitingOperations;
	private Queue<Integer> waitingTIds;
	
	public TransactionDetails(int tId, Timestamp timestamp, String state, List<Character> dataItemsHeld,
			Queue<String> waitingOperations, Queue<Integer> waitingTIds) {
		this.tId = tId;
		this.timestamp = timestamp;
		this.state = state;
		this.dataItemsHeld = dataItemsHeld;
		this.waitingOperations = waitingOperations;
		this.waitingTIds = waitingTIds;
	}
	
	public int gettId() {
		return tId;
	}
	public void settId(int tId) {
		this.tId = tId;
	}
	public Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public List<Character> getDataItemsHeld() {
		return dataItemsHeld;
	}
	public void setDataItemsHeld(List<Character> dataItemsHeld) {
		this.dataItemsHeld = dataItemsHeld;
	}
	public Queue<String> getWaitingOperations() {
		return waitingOperations;
	}
	public void setWaitingOperations(Queue<String> waitingOperations) {
		this.waitingOperations = waitingOperations;
	}
	public Queue<Integer> getWaitingTIds() {
		return waitingTIds;
	}
	public void setWaitingTIds(Queue<Integer> waitingTIds) {
		this.waitingTIds = waitingTIds;
	}
	
}
