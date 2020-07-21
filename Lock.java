import java.util.List;
import java.util.Queue;

public class Lock {

	private Character dataItem;
	private String lockType;
	private List<Integer> accessingTIds;
	private Queue<Integer> waitingTids;
	
	public Lock(Character dataItem, String lockType, List<Integer> accessingTIds, Queue<Integer> waitingTids) {
		this.dataItem = dataItem;
		this.lockType = lockType;
		this.accessingTIds = accessingTIds;
		this.waitingTids = waitingTids;
	}
	
	public Character getDataItem() {
		return dataItem;
	}
	public void setDataItem(Character dataItem) {
		this.dataItem = dataItem;
	}
	public String getLockType() {
		return lockType;
	}
	public void setLockType(String lockType) {
		this.lockType = lockType;
	}
	public List<Integer> getAccessingTIds() {
		return accessingTIds;
	}
	public void setAccessingTIds(List<Integer> accessingTIds) {
		this.accessingTIds = accessingTIds;
	}
	public Queue<Integer> getWaitingTids() {
		return waitingTids;
	}
	public void setWaitingTids(Queue<Integer> waitingTids) {
		this.waitingTids = waitingTids;
	}
}
