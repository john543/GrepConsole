package krasa.grepconsole.gui.table.column;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * @author jonathan - 3/23/17.
 */
public class TextInputJavaBeanColumnInfo<T> extends JavaBeanColumnInfo<T, String>
{

	public TextInputJavaBeanColumnInfo(String name, String propertyName)
	{
		super(name, propertyName);
	}

	@Nullable
	@Override
	public TableCellRenderer getRenderer(Object o)
	{
		final DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
		defaultTableCellRenderer.setToolTipText(getTooltipText());
		return defaultTableCellRenderer;
	}

	@Nullable
	@Override
	public TableCellEditor getEditor(Object o)
	{
		return new DefaultCellEditor(new JTextField(o.toString()));
	}
}
