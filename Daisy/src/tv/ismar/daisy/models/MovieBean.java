package tv.ismar.daisy.models;

/**
 * 搜索结果实体
 * 
 * @author liuhao
 */
public class MovieBean {
	// 上映时间
	public String publish_date;
	// 详情预览图片
	public String poster_url;
	// 详细信息
	public String item_url;
	// 如果是电视剧则显示集数
	public String episode;
	// url
	public String url;
	// 1为低码流，电视不适用，2为流畅，3为高清，4为超清，5为1080P
	public String quality;
	// 标题
	public String title;

	public String focus;

	public String caption;

	public String position;
	// 类型产品类型:
	// movie 为电影
	// teleplay 为电视剧
	// variety 为综艺
	// documentary 为纪录片
	// entertainment 为娱乐
	// music 为音乐
	// comic 为喜剧
	public String content_model;

	public String adlet_url;

	public Boolean is_complex;

	public int pk;
	// 缩略图
	public String thumb_url;
	// 所属Item的唯一标识,仅当该条目为subitem时有意义
	public int item_pk;
	// item表示电影或电视剧 subitem表示电视剧的子剧集
	public String model_name;
	public Expense expense;
}
