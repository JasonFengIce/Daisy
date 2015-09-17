package tv.ismar.daisy.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 大图切换（当在一个页面按左右键，切换到新的页面时，需要根据之前页面的焦点位置，切换到新页面的最合适位置上）
 * 
 * @author lion
 * 
 */
public class BigPicSwitch {
	/**
	 * 左滑动
	 */
	public static final int ACTION_LEFT = 0;
	/**
	 * 右滑动
	 */
	public static final int ACTION_RIGHT = 1;

	/**
	 * 聚焦视图
	 * 
	 * @param originalView
	 *            原始视图（移动前焦点所在视图）
	 * @param action
	 *            移动动作
	 * @param views
	 *            移动后的视图列表
	 * @return 需要聚焦的视图
	 */
	public static ViewEntity focus(ViewEntity originalView, int action, List<ViewEntity> views) {
		List<ViewEntity> edgeViews = BigPicSwitch.getEdgeViews(action, views);
		if (edgeViews != null && edgeViews.size() > 0) {
			return BigPicSwitch.getFitView(originalView, edgeViews);
		}

		// 假设算法存在bug，确保系统不出错，给出默认位置
		return views.get(0);
	}

	/**
	 * 获取边缘视图列表
	 * 
	 * @param action
	 *            移动动作
	 * @param views
	 *            移动后的视图列表
	 * @return
	 */
	private static List<ViewEntity> getEdgeViews(int action, List<ViewEntity> views) {
		// 算法：
		// 1、先获取最边缘的视图列表，按照左右移动方向，获取x轴最大，或最小的一组view

		List<ViewEntity> edgeViews = new ArrayList<ViewEntity>();

		int pos = BigPicSwitch.getEdgePos(action, views);

		for (ViewEntity view : views) {
			switch (action) {
			case BigPicSwitch.ACTION_LEFT:
				if (pos == view.getPosx() + view.getWidth()) {
					edgeViews.add(view);
				}
				break;
			case BigPicSwitch.ACTION_RIGHT:
				if (pos == view.getPosx()) {
					edgeViews.add(view);
				}
				break;
			}
		}

		return edgeViews;
	}

	/**
	 * 获取x轴最大或最小值
	 * 
	 * @param action
	 * @param views
	 * @return
	 */
	private static int getEdgePos(int action, List<ViewEntity> views) {
		int pos = 0;

		// 设置初始化值用于比较
		switch (action) {
		case BigPicSwitch.ACTION_LEFT:
			pos = 0;
			break;
		case BigPicSwitch.ACTION_RIGHT:
			pos = 999999999;
			break;
		}

		for (ViewEntity view : views) {
			switch (action) {
			case BigPicSwitch.ACTION_LEFT:
				if (pos < view.getPosx() + view.getWidth()) {
					pos = view.getPosx() + view.getWidth();
				}
				break;
			case BigPicSwitch.ACTION_RIGHT:
				if (pos > view.getPosx()) {
					pos = view.getPosx();
				}
				break;
			}
		}

		return pos;
	}

	/**
	 * 获取合适聚焦的视图
	 * 
	 * @param originalView
	 *            原始视图（移动前焦点所在视图）
	 * @param edgeViews
	 *            边缘视图列表
	 * @return
	 */
	private static ViewEntity getFitView(ViewEntity originalView, List<ViewEntity> edgeViews) {
		// 算法：
		// 2、根据原始视图的y轴和height，得出原始视图的y轴区间。以此去匹配合适的视图位置。匹配方式为通过每个视图的居中算法来判断。
		// a、若原始的y轴中心能在某个视图，则使用此视图，否则，则看原始的y轴up在哪个视图，则使用此视图，否则，则看原始的y轴down在哪个视图，则使用此视图。

		int originalViewPosyUp = originalView.getPosy();
		int originalViewPosyDown = originalView.getPosy() - originalView.getHeight();
		int originalViewPosyCenter = originalViewPosyUp - originalView.getHeight() / 2;

		// 判断y轴中心
		for (ViewEntity viewEntity : edgeViews) {
			if (originalViewPosyCenter <= viewEntity.getPosy() &&
					originalViewPosyCenter >= viewEntity.getPosy() - viewEntity.getHeight()) {
				return viewEntity;
			}
		}

		// 判断y轴up
		for (ViewEntity viewEntity : edgeViews) {
			if (originalViewPosyUp <= viewEntity.getPosy() &&
					originalViewPosyUp >= viewEntity.getPosy() - viewEntity.getHeight()) {
				return viewEntity;
			}
		}

		// 判断y轴down
		for (ViewEntity viewEntity : edgeViews) {
			if (originalViewPosyDown <= viewEntity.getPosy() &&
					originalViewPosyDown >= viewEntity.getPosy() - viewEntity.getHeight()) {
				return viewEntity;
			}
		}

		// 假设算法存在bug，确保系统不出错，给出默认位置
		return edgeViews.get(0);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		List<ViewEntity> views = new ArrayList<ViewEntity>();

		ViewEntity viewEntity = new ViewEntity();
		viewEntity.setId("2_1");
		viewEntity.setPosx(500);
		viewEntity.setPosy(500);
		viewEntity.setWidth(100);
		viewEntity.setHeight(50);
		views.add(viewEntity);

		viewEntity = new ViewEntity();
		viewEntity.setId("2_2");
		viewEntity.setPosx(500);
		viewEntity.setPosy(300);
		viewEntity.setWidth(100);
		viewEntity.setHeight(50);
		views.add(viewEntity);

		viewEntity = new ViewEntity();
		viewEntity.setId("2_3");
		viewEntity.setPosx(500);
		viewEntity.setPosy(100);
		viewEntity.setWidth(100);
		viewEntity.setHeight(50);
		views.add(viewEntity);

		viewEntity = new ViewEntity();
		viewEntity.setId("1_1");
		viewEntity.setPosx(100);
		viewEntity.setPosy(500);
		viewEntity.setWidth(100);
		viewEntity.setHeight(50);
		views.add(viewEntity);

		viewEntity = new ViewEntity();
		viewEntity.setId("1_2");
		viewEntity.setPosx(100);
		viewEntity.setPosy(100);
		viewEntity.setWidth(100);
		viewEntity.setHeight(50);
		views.add(viewEntity);

		// List<ViewEntity> edgeViews =
		// BigPicSwitch.getEdgeViews(BigPicSwitch.ACTION_LEFT, views);
		// for (ViewEntity viewEntity2 : edgeViews) {
		// System.out.println(viewEntity2.getId());
		// }

		ViewEntity originalView = new ViewEntity();
		originalView.setId("0_0");
		originalView.setPosx(100);
		originalView.setPosy(500);
		originalView.setWidth(100);
		originalView.setHeight(50);
		ViewEntity v = BigPicSwitch.focus(originalView, BigPicSwitch.ACTION_RIGHT, views);
		System.out.println(v.getId());
	}
}
