/**
 * 
 */
package project.util.logger;

/**
 * @author Chris Hartley
 *
 */
public enum LogItemType {

	NORMAL(1, "normal"),
	DEBUG(2, "debug"),
	WARNING(4, "warning"),
	ERROR(8, "error");
	
	public final int id;
	private final String name;

	private LogItemType(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	
	@Override
	public String toString() {
		return name;
	}
	
}
