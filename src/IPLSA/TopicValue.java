package IPLSA;

/**
 * Stores the count for a topic 
 * @author yogi
 *
 */
public class TopicValue implements Comparable<TopicValue>{

	int topic;
	int count;
	
	TopicValue(int topic, int count) {
		this.topic = topic;
		this.count = count;
	}

	@Override
	public int compareTo(TopicValue o) {
		return this.count - o.count;
	}
	
	
}
