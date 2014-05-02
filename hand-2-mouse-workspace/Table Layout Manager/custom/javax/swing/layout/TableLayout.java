/**
 * 
 */
package custom.javax.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * 
 * @author Chris N. Hartley
 */
public class TableLayout implements LayoutManager2 {

	private interface TableConstraints { }
	
	public static class CellSpan implements TableConstraints {
		public final int rowSpan;
		public final int colSpan;
		
		public CellSpan(int rowSpan, int colSpan) {
			this.rowSpan = rowSpan;
			this.colSpan = colSpan;
		}
	}
	
	public static class ColumnSpan extends CellSpan {
		
		public ColumnSpan(int colSpan) {
			super(0, colSpan);
		}
	}
	
	public static class RowSpan extends CellSpan {
		
		public RowSpan(int rowSpan) {
			super(rowSpan, 0);
		}
	}
	public static final ColumnSpan ColumnSpan(int cols) {
		return new ColumnSpan(cols);
	}
	
	
	public static final int LAYOUT_ROWS = 0x00;
	public static final int LAYOUT_COLUMNS = 0x01;
	
	/**
	 * The default spacing value for both the vertically and horizontally 
	 * adjacent cells within any implementation of this table layout manager.
	 */
	private static final int defaultGap = 4;
	
	
	// Private member data.
	private Dimension minSize;
	private Dimension maxSize;
	private Dimension prefSize;
	private int vgap;
	private int hgap;
	private final int rows;
	private final int cols;
	private final List<Column> columnSets;
	private final List<Row> rowSets;

	private boolean needsUpdating = true;
	private int layoutDirection = LAYOUT_ROWS;
	private LinkedList<Component> componentOrder = new LinkedList<Component>();
	private HashMap<Component,TableConstraints> componentConstraints =
			new HashMap<Component,TableConstraints>();
	
	
	
	
	/**
	 * Constructor to create a new instance of this layout manager with the 
	 * specified number of rows and columns. This constructor uses the default
	 * layout direction of laying out the rows. Example:
	 * {@code (row1, col1), (row1, col2) }
	 * 
	 * @param rows  the number of rows this table contains.
	 * @param cols  the number of columns this table contains.
	 * 
	 * @see #LAYOUT_ROWS
	 */
	public TableLayout(int rows, int cols) {
		this(rows, cols, defaultGap, defaultGap, LAYOUT_ROWS);
	}
	
	
	/**
	 * Constructor to create a new instance of this layout manager with the
	 * specified number of rows and columns and also the vertical and horizontal
	 * spacing for the table cells. This constructor uses the default layout 
	 * direction of laying out the rows. Example: 
	 * {@code (row1, col1), (row1, col2) }
	 * 
	 * @param rows  the number of rows this table contains.
	 * @param cols  the number of columns this table contains.
	 * @param vgap  the vertical spacing between adjacent cells.
	 * @param hgap  the horizontal spacing between adjacent cells.
	 * 
	 * @see #LAYOUT_ROWS
	 */
	public TableLayout(int rows, int cols, int vgap, int hgap) {
		this(rows, cols, vgap, hgap, LAYOUT_ROWS);
	}
	
	
	/**
	 * Constructor to create a new instance of this layout manager with the
	 * specified number of rows and columns and also the vertical and horizontal
	 * spacing for the table cells; as well as, specifies the layout direction
	 * for adding components to this layout manager.
	 * 
	 * @param rows  the number of rows this table contains.
	 * @param cols  the number of columns this table contains.
	 * @param vgap  the vertical spacing between adjacent cells.
	 * @param hgap  the horizontal spacing between adjacent cells.
	 * @param layout	the layout direction either {@link #LAYOUT_ROWS} or
	 * 					{@link #LAYOUT_COLUMNS}.
	 * 
	 * @see #LAYOUT_COLUMNS
	 * @see #LAYOUT_ROWS
	 */
	public TableLayout(int rows, int cols, int vgap, int hgap, int layout) {
		this.rows = rows;
		this.cols = cols;
		this.vgap = vgap;
		this.hgap = hgap;
		this.layoutDirection = layout;
		
		if (layoutDirection == LAYOUT_COLUMNS)
			;
		
		columnSets = new ArrayList<Column>(cols);	// new Column[cols];
		for (int i = 0; i < cols; i++)
			columnSets.add( new Column());			// [i] = new Column();
		
		rowSets = new ArrayList<Row>(rows);			// new Row[rows];
		for (int i = 0; i < rows; i++)
			rowSets.add( new Row() );				// [i] = new Row();
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
	 */
	@Override
	public void addLayoutComponent(String name, Component comp) {
		addLayoutComponentImpl(name, comp, null);
	}

	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
	 */
	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		addLayoutComponentImpl(comp.getName(), comp, constraints);
	}
	
	/**
	 * 
	 * @param name
	 * @param comp
	 * @param constraints
	 */
	private final void addLayoutComponentImpl(String name, Component comp,
			Object constraints)
	{
		if (name == null || name.length() < 1) {
			name = comp.getName();
			if (name == null)
				comp.setName(name = comp.toString());
		}
		else
			comp.setName(name);
		
		if (componentOrder.add(comp)) {
			if (constraints != null && constraints instanceof TableConstraints)
				componentConstraints.put(comp, (TableConstraints)constraints);
			else
				componentConstraints.put(comp, null);
			needsUpdating = true;
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 */
	@Override
	public void layoutContainer(Container parent) {
		if (needsUpdating)
			updateSizes(parent);

		final Insets ins = parent.getInsets();
		final int leftRight = ins.left + ins.right;
		final int topBottom = ins.top + ins.bottom;
		final int width = parent.getWidth() - leftRight;
		final int height = parent.getHeight() - topBottom;
		final Rectangle[] cells = new Rectangle[componentOrder.size()];
		
		//check if parent is smaller than minimum size...
		// if so, layout based on the column minimum size requirements.
		
		int col, row;
		for (int i = 0; i < cells.length; i++) {
			cells[i] = new Rectangle();
			row = i / cols;
			col = i % cols;
			
			if (width + leftRight <= minSize.width)
				cells[i].width = columnSets.get(col).minimumWidth;
			else
				cells[i].width = columnSets.get(col).preferredWidth;
			
			if (height + topBottom <= minSize.height)
				cells[i].height = rowSets.get(row).minimumHeight;
			else
				cells[i].height = rowSets.get(row).preferredHeight;
			
			cells[i].x = col <= 0 ? 0 :
					cells[i - 1].x + cells[i - 1].width + hgap;
			
			cells[i].y = row <= 0 ? 0 :
					cells[(row - 1) * cols].y + cells[(row - 1) * cols].height
					+ vgap;
		}

		//TODO: add code to actual layout the components contained within the
		//      specified parent container.
		double scaleWidth = 1d;
		double scaleHeight = 1d;
		
		if (prefSize.width < width + leftRight)
			scaleWidth = (double)width / (double)(prefSize.width - leftRight);
		if (prefSize.height < height + topBottom)
			scaleHeight = (double)height / (double)(prefSize.height - topBottom);

		Component comp;
		for (int i = 0; i < componentOrder.size(); i++) {
			comp = componentOrder.get(i);
			if (comp != null) {
				cells[i].x = (int)(scaleWidth * cells[i].x) + ins.left;
				cells[i].y = (int)(scaleHeight * cells[i].y) + ins.top;
				cells[i].width = (int)(scaleWidth * cells[i].width);
				cells[i].height = (int)(scaleHeight * cells[i].height);
				comp.setBounds(cells[i]);
				comp.setLocation(cells[i].getLocation());
				comp.setSize(cells[i].getSize());
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension minimumLayoutSize(Container parent) {
		if (needsUpdating)
			updateSizes(parent);
		
		return minSize;
	}

	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		if (needsUpdating)
			updateSizes(parent);
		
		return prefSize;
	}

	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 */
	@Override
	public void removeLayoutComponent(Component comp) {
		boolean hasChanged = false;
		if (componentOrder.contains(comp)) {
			for (Component tmp : componentOrder) {
				if (tmp.equals(comp)) {
					componentConstraints.remove(comp);
					componentOrder.remove(comp);
					hasChanged = true;
				}
			}
		}
		
		if (hasChanged)
			updateSizes(comp.getParent());
	}

	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
	 */
	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0.5f;
	}

	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
	 */
	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0.5f;
	}

	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
	 */
	@Override
	public void invalidateLayout(Container target) {
		needsUpdating = true;
	}

	
	/* (non-Javadoc)
	 * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension maximumLayoutSize(Container parent) {
		if (needsUpdating)
			updateSizes(parent);
		
		return maxSize;
	}
	
	
	/**
	 * 
	 */
	private final void updateSizes(Container parent) {
		int cell = -1;
		
		if (layoutDirection == LAYOUT_COLUMNS) {
			for (int col = 0; col < cols; col++) {
				for (int row = 0; row < rows; row++) {
					if (++cell >= componentOrder.size())
						continue;
					
					checkComponentSizes(componentOrder.get(cell),
							rowSets.get(row), columnSets.get(col));
				}
			}
		}
		else { // default layout direction or LAYOUT_ROWS
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
					if (++cell >= componentOrder.size())
						continue;
					
					checkComponentSizes(componentOrder.get(cell),
							rowSets.get(row), columnSets.get(col));
				}
			}
		}
		
		minSize = new Dimension(0, 0);
		maxSize = new Dimension(0, 0);
		prefSize = new Dimension(0, 0);
		
		Column tmpCol;
		for (int col = 0; col < cols; col++) {
			tmpCol = columnSets.get(col);
			minSize.width += tmpCol.minimumWidth + hgap;
			maxSize.width += tmpCol.maximumWidth + hgap;
			prefSize.width += tmpCol.preferredWidth + hgap;
		}
		
		Row tmpRow;
		for (int row = 0; row < rows; row++) {
			tmpRow = rowSets.get(row);
			minSize.height += tmpRow.minimumHeight + vgap;
			maxSize.height += tmpRow.maximumHeight + vgap;
			prefSize.height += tmpRow.preferredHeight + vgap;
		}
		
		// Include insets from border(s)...
		final Insets ins = parent.getInsets();
		final int leftRight = ins.left + ins.right - hgap;
		final int topBottom = ins.top + ins.bottom - vgap;
		minSize.width += leftRight;
		maxSize.width += leftRight;
		prefSize.width += leftRight;
		minSize.height += topBottom;
		maxSize.height += topBottom;
		prefSize.height += topBottom;
		
		needsUpdating = false;
	}
	
	
	public final int getHorizontalSpacing() {
		return hgap;
	}
	
	
	public final void setHorizontalSpacing(int hgap) {
		this.hgap = hgap;
		needsUpdating = true;
	}
	
	
	public final int getVerticalSpacing() {
		return vgap;
	}
	
	
	public final void setVerticalSpacing(int vgap) {
		this.vgap = vgap;
		needsUpdating = true;
	}
	
	
	private final void checkComponentSizes(Component comp, Row row, Column col)
	{
		if (comp == null)
			return;
		
		Dimension min = comp.getMinimumSize();
		Dimension max = comp.getMaximumSize();
		Dimension pref = comp.getPreferredSize();
		
		if (min.width < col.minimumWidth)
			col.minimumWidth = min.width;
		if (min.height < row.minimumHeight)
			row.minimumHeight = min.height;
		
		if (max.width > col.maximumWidth)
			col.maximumWidth = max.width;
		if (max.height > row.maximumHeight)
			row.maximumHeight = max.height;
		
		if (pref.width > col.preferredWidth)
			col.preferredWidth = pref.width;
		if (pref.height > row.preferredHeight)
			row.preferredHeight = pref.height;
	}
	
	
	private final class Row {
		private int minimumHeight = 0;
		private int maximumHeight = Integer.MAX_VALUE;
		private int preferredHeight = 0;
	}
	
	
	private final class Column {
		private int minimumWidth = 0;
		private int maximumWidth = Integer.MAX_VALUE;
		private int preferredWidth = 0;
	}

}
