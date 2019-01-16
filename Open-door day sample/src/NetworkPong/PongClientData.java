import java.io.Serializable;

public class PongClientData implements Serializable{
	
	private static final long serialVersionUID = -6243741163353349874L;
	
	private int x, y;
	
	public PongClientData(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
}