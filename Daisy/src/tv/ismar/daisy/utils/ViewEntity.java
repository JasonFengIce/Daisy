package tv.ismar.daisy.utils;


/**
 * 视图实体
 * 
 * @author lion
 * 
 */
public class ViewEntity {
	/**
	 * 视图唯一标识
	 */
	private String id;
	/**
	 * 视图左上角x轴位置
	 */
	private int posx;
	/**
	 * 视图左上角y轴位置
	 */
	private int posy;
	/**
	 * 宽
	 */
	private int width;
	/**
	 * 高
	 */
	private int height;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPosx() {
		return posx;
	}

	public void setPosx(int posx) {
		this.posx = posx;
	}

	public int getPosy() {
		return posy;
	}

	public void setPosy(int posy) {
		this.posy = posy;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
