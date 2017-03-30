package krasa.grepconsole.gui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.JBColor;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.tree.TreeUtil;
import krasa.grepconsole.gui.table.CheckboxTreeTable;
import krasa.grepconsole.gui.table.GrepExpressionGroupTreeNode;
import krasa.grepconsole.gui.table.GrepExpressionItemTreeNode;
import krasa.grepconsole.gui.table.TableUtils;
import krasa.grepconsole.gui.table.column.CommandColumn;
import krasa.grepconsole.gui.table.column.SoundColumn;
import krasa.grepconsole.model.ConsoleCommand;
import krasa.grepconsole.model.GrepColor;
import krasa.grepconsole.model.GrepExpressionGroup;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.model.Sound;
import krasa.grepconsole.plugin.DefaultState;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.PluginState;
import krasa.grepconsole.tail.TailIntegrationForm;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import static krasa.grepconsole.Cloner.deepClone;

public class SettingsDialog {
	public static final Icon COMMAND_OFF = IconLoader.getIcon("Cog1.png", CommandColumn.class);
	public static final Icon COMMAND_ON = IconLoader.getIcon("Cog2.png", CommandColumn.class);
	public static final Icon SOUND_OFF = IconLoader.getIcon("soundOff.gif", SoundColumn.class);
	public static final Icon SOUND_ON = IconLoader.getIcon("soundOn.gif", SoundColumn.class);
	private static final Logger log = Logger.getInstance(SettingsDialog.class);
	private final SettingsContext settingsContext;
	private JPanel rootComponent;
	private CheckboxTreeTable table;
	private JButton addNewButton;
	private JButton resetToDefaultButton;
	private JCheckBox enableHighlightingCheckBox;
	private JFormattedTextField maxLengthToMatch;
	private JCheckBox enableMaxLength;
	private JButton duplicateButton;
	private JButton deleteButton;
	private JCheckBox enableFiltering;
	private JCheckBox multilineOutput;
	private JCheckBox showStatsInConsole;
	private JCheckBox showStatsInStatusBar;
	private JButton fileTailSettings;
	private JButton addNewGroup;
	private JLabel contextSpecificText;
	private JCheckBox enableFoldings;
	private JFormattedTextField maxProcessingTime;
	private JCheckBox filterOutBeforeGreppingToASubConsole;
	private JTextField minCompilationTime;
	private JCheckBox claimFocusAfterBuildCheckBox;
	private JButton playSoundButton;
	private JButton commandButton;
	// private JCheckBox synchronous;
	private PluginState settings;

	public SettingsDialog(PluginState settings) {
		this(settings, SettingsContext.NONE);
	}

	public SettingsDialog(PluginState settings, SettingsContext settingsContext) {
		// int version = Integer.parseInt(ApplicationInfo.getInstance().getMajorVersion());
		// if (version < 163) {
		// synchronous.setVisible(false);
		// }
		this.settingsContext = settingsContext;
		this.settings = settings;
		playSoundButton.addActionListener(new AddSoundAction());
		commandButton.addActionListener(new AddCommandAction());
		setSoundIcon();
		setCommandIcon();
		addNewButton.addActionListener(new AddNewItemAction());
		addNewGroup.addActionListener(new AddNewGroupAction());
		resetToDefaultButton.addActionListener(new ResetToDefaultAction());
		duplicateButton.addActionListener(new DuplicateAction());
		deleteButton.addActionListener(new DeleteAction());
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object source = e.getSource();
					if (source instanceof DefaultListSelectionModel) {
						setSelectedRow(((DefaultListSelectionModel) source).getLeadSelectionIndex());
					}

				}
			}
		});
		table.addMouseListener(rightClickMenu());
		table.addKeyListener(new DeleteListener());
		disableCopyDeleteButton();

		fileTailSettings.addActionListener(new FileTailSettings());

		if (settingsContext == SettingsContext.CONSOLE) {
			contextSpecificText.setText("Select items for which statistics should be displayed ('"
					+ SettingsTableBuilder.CONSOLE_COUNT + "' column)");
		} else if (settingsContext == SettingsContext.STATUS_BAR) {
			contextSpecificText.setText("Select items for which statistics should be displayed ('"
					+ SettingsTableBuilder.STATUS_BAR_COUNT + "' column)");
		} else {
			contextSpecificText.setVisible(false);
		}
	}

	private void setSoundIcon()
	{
		if (getProfile().getSound().isEnabled())
		{
			playSoundButton.setIcon(SOUND_ON);
		}
		else
		{
			playSoundButton.setIcon(SOUND_OFF);
		}

	}

	private void setCommandIcon()
	{
		if (getProfile().getCommand().isEnabled())
		{
			commandButton.setIcon(COMMAND_ON);
		}
		else
		{
			commandButton.setIcon(COMMAND_OFF);
		}

	}

	public MouseAdapter rightClickMenu() {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
				} else if (SwingUtilities.isRightMouseButton(e)) {
					if (getSelectedNode() == null) {
						return;
					}
					JPopupMenu popup = new JBPopupMenu();
					GrepExpressionItem selectedGrepExpressionItem = getSelectedGrepExpressionItem();
					if (selectedGrepExpressionItem != null) {
						popup.add(getConvertAction(selectedGrepExpressionItem));
					}
					popup.add(newMenuItem("Add New Item", new AddNewItemAction()));
					popup.add(newMenuItem("Duplicate", new DuplicateAction()));
					popup.add(newMenuItem("Delete", new DeleteAction()));
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			private JMenuItem newMenuItem(String name, ActionListener l) {
				final JMenuItem item = new JMenuItem(name);
				item.addActionListener(l);
				return item;
			}

			private JMenuItem getConvertAction(final GrepExpressionItem item) {
				final boolean highlightOnlyMatchingText = item.isHighlightOnlyMatchingText();
				final JMenuItem convert = new JMenuItem(highlightOnlyMatchingText ? "Convert to whole line"
						: "Convert to words only");

				try {
					convert.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (!highlightOnlyMatchingText) {
								getSelectedGrepExpressionItem().setHighlightOnlyMatchingText(true);
								getSelectedGrepExpressionItem().setContinueMatching(true);
								if (item.getGrepExpression().startsWith(".*")) {
									item.grepExpression(item.getGrepExpression().substring(2));
								}
								if (item.getGrepExpression().endsWith(".*")) {
									item.grepExpression(item.getGrepExpression().substring(0,
											item.getGrepExpression().length() - 2));
								}
							} else {
								getSelectedGrepExpressionItem().setHighlightOnlyMatchingText(false);
								getSelectedGrepExpressionItem().setContinueMatching(false);
								if (!item.getGrepExpression().startsWith(".*")) {
									item.grepExpression(".*" + item.getGrepExpression());
								}
								if (!item.getGrepExpression().endsWith(".*")) {
									item.grepExpression(item.getGrepExpression() + ".*");
								}
							}
							reloadNode(SettingsDialog.this.getSelectedNode());
						}

					});
				} catch (Exception e) {
					e.printStackTrace();
				}
				return convert;
			}
		};
	}

	private void reloadNode(final DefaultMutableTreeNode selectedNode) {
		DefaultTreeModel model = (DefaultTreeModel) table.getTree().getModel();
		model.nodeChanged(selectedNode);
	}

	private GrepExpressionItem getSelectedGrepExpressionItem() {
		DefaultMutableTreeNode selectedNode = getSelectedNode();
		GrepExpressionItem item = null;
		if (selectedNode instanceof GrepExpressionItemTreeNode) {
			item = (GrepExpressionItem) selectedNode.getUserObject();
		}
		return item;
	}

	public DefaultMutableTreeNode getSelectedNode() {
		return (DefaultMutableTreeNode) table.getTree().getLastSelectedPathComponent();
	}

	private void disableCopyDeleteButton() {
		deleteButton.setEnabled(false);
		duplicateButton.setEnabled(false);
	}

	private void setSelectedRow(Integer selectedRow) {
		deleteButton.setEnabled(selectedRow != null && selectedRow >= 0);
		duplicateButton.setEnabled(selectedRow != null && selectedRow >= 0);
	}

	public JPanel getRootComponent() {
		return rootComponent;
	}

	public PluginState getSettings() {
		getData(getProfile());
		return settings;
	}

	public Profile getProfile() {
		return settings.getDefaultProfile();
	}

	public void importFrom(PluginState settings) {
		this.settings = settings;
		setData(settings.getDefaultProfile());
		resetTreeModel();
	}

	private void resetTreeModel() {
		CheckedTreeNode root = (CheckedTreeNode) table.getTree().getModel().getRoot();
		root.removeAllChildren();
		for (GrepExpressionGroup group : getProfile().getGrepExpressionGroups()) {
			GrepExpressionGroupTreeNode newChild = new GrepExpressionGroupTreeNode(group);
			for (GrepExpressionItem grepExpressionItem : group.getGrepExpressionItems()) {
				newChild.add(new GrepExpressionItemTreeNode(grepExpressionItem));
			}
			root.add(newChild);
		}
		TableUtils.reloadTree(table);
		TreeUtil.expandAll(table.getTree());
	}

	public boolean isSettingsModified(PluginState data) {
		getData(getProfile());
		return !this.settings.equals(data);
	}

	private void createUIComponents() {
		fileTailSettings = new JButton();
		NumberFormatter numberFormatter = new NumberFormatter();
		numberFormatter.setMinimum(0);
		maxLengthToMatch = new JFormattedTextField(numberFormatter);
		maxProcessingTime = new JFormattedTextField(numberFormatter);
		minCompilationTime = new JFormattedTextField(numberFormatter);
		table = new SettingsTableBuilder(this).getTable();
	}

	private GrepExpressionItem newItem() {
		GrepExpressionItem item = new GrepExpressionItem();
		item.setGrepExpression("foo");
		item.setEnabled(true);
		item.setContinueMatching(true);
		item.setHighlightOnlyMatchingText(true);
		item.getStyle().setBackgroundColor(new GrepColor(true, JBColor.CYAN));
		return item;
	}

	private GrepExpressionGroup getGrepExpressionGroup(DefaultMutableTreeNode selectedNode) {
		return (GrepExpressionGroup) selectedNode.getUserObject();
	}

	private GrepExpressionItem getSelectedGrepExpressionItem(DefaultMutableTreeNode selectedNode) {
		return (GrepExpressionItem) selectedNode.getUserObject();
	}

	public void rebuildProfile() {
		List<GrepExpressionGroup> grepExpressionGroups = getProfile().getGrepExpressionGroups();
		grepExpressionGroups.clear();

		DefaultMutableTreeNode model = (DefaultMutableTreeNode) table.getTree().getModel().getRoot();
		Enumeration children = model.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode o = (DefaultMutableTreeNode) children.nextElement();
			if (o instanceof GrepExpressionGroupTreeNode) {
				GrepExpressionGroup grepExpressionGroup = ((GrepExpressionGroupTreeNode) o).getGrepExpressionGroup();
				grepExpressionGroup.getGrepExpressionItems().clear();
				Enumeration children1 = o.children();
				while (children1.hasMoreElements()) {
					Object o1 = children1.nextElement();
					if (o1 instanceof GrepExpressionItemTreeNode) {
						GrepExpressionItem grepExpressionItem = ((GrepExpressionItemTreeNode) o1).getGrepExpressionItem();
						grepExpressionGroup.add(grepExpressionItem);
					} else {
						throw new IllegalStateException("unexpected tree node" + o1);
					}
				}
				grepExpressionGroups.add(grepExpressionGroup);
			} else {
				throw new IllegalStateException("unexpected tree node" + o);
			}
		}
	}

	public void setData(Profile data) {
		enableMaxLength.setSelected(data.isEnableMaxLengthLimit());
		enableHighlightingCheckBox.setSelected(data.isEnabledHighlighting());
		enableFiltering.setSelected(data.isEnabledInputFiltering());
		multilineOutput.setSelected(data.isMultiLineOutput());
		maxProcessingTime.setText(data.getMaxProcessingTime());
		showStatsInConsole.setSelected(data.isShowStatsInConsoleByDefault());
		showStatsInStatusBar.setSelected(data.isShowStatsInStatusBarByDefault());
		enableFoldings.setSelected(data.isEnableFoldings());
		filterOutBeforeGreppingToASubConsole.setSelected(data.isFilterOutBeforeGrep());
		maxLengthToMatch.setText(data.getMaxLengthToMatch());
		minCompilationTime.setText(data.getMinCompilationTime());
		claimFocusAfterBuildCheckBox.setSelected(data.isClaimFocusAfterBuild());
	}

	public void getData(Profile data) {
		data.setEnableMaxLengthLimit(enableMaxLength.isSelected());
		data.setEnabledHighlighting(enableHighlightingCheckBox.isSelected());
		data.setEnabledInputFiltering(enableFiltering.isSelected());
		data.setMultiLineOutput(multilineOutput.isSelected());
		data.setMaxProcessingTime(maxProcessingTime.getText());
		data.setShowStatsInConsoleByDefault(showStatsInConsole.isSelected());
		data.setShowStatsInStatusBarByDefault(showStatsInStatusBar.isSelected());
		data.setEnableFoldings(enableFoldings.isSelected());
		data.setFilterOutBeforeGrep(filterOutBeforeGreppingToASubConsole.isSelected());
		data.setClaimFocusAfterBuild(claimFocusAfterBuildCheckBox.isSelected());
		data.setMinCompilationTime(minCompilationTime.getText());
		data.setMaxLengthToMatch(maxLengthToMatch.getText());
	}

	public boolean isModified(Profile data) {
		if (enableMaxLength.isSelected() != data.isEnableMaxLengthLimit()) return true;
		if (enableHighlightingCheckBox.isSelected() != data.isEnabledHighlighting()) return true;
		if (enableFiltering.isSelected() != data.isEnabledInputFiltering()) return true;
		if (multilineOutput.isSelected() != data.isMultiLineOutput()) return true;
		if (maxProcessingTime.getText() != null ? !maxProcessingTime.getText().equals(data.getMaxProcessingTime()) : data.getMaxProcessingTime() != null)
			return true;
		if (showStatsInConsole.isSelected() != data.isShowStatsInConsoleByDefault()) return true;
		if (showStatsInStatusBar.isSelected() != data.isShowStatsInStatusBarByDefault()) return true;
		if (enableFoldings.isSelected() != data.isEnableFoldings()) return true;
		if (filterOutBeforeGreppingToASubConsole.isSelected() != data.isFilterOutBeforeGrep()) return true;
		if (maxLengthToMatch.getText() != null ? !maxLengthToMatch.getText().equals(data.getMaxLengthToMatch()) : data.getMaxLengthToMatch() != null)
			return true;
		if (claimFocusAfterBuildCheckBox.isSelected() != data.isClaimFocusAfterBuild()) return true;
		if (minCompilationTime.getText() != data.getMinCompilationTime()) return true;

		return false;
	}

	private class DeleteListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			final int keyCode = e.getKeyCode();
			if (keyCode == KeyEvent.VK_DELETE) {
				delete();
			}
		}
	}

	private void delete() {
		TreeNode selectNode = null;
		TreeTableTree tree = table.getTree();
		int[] selectionRows = tree.getSelectionRows();
		if (selectionRows == null) {
			return;
		}
		Arrays.sort(selectionRows);
		selectionRows = ArrayUtil.reverseArray(selectionRows);
		for (int selectionRow : selectionRows) {
			TreePath treePath = tree.getPathForRow(selectionRow);
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();

			if (selectNode == null || selectNode == selectedNode || selectNode.getParent() == selectedNode) {
				int index = parent.getIndex(selectedNode);
				if (index + 1 < parent.getChildCount()) {
					selectNode = parent.getChildAt(index + 1);
				} else if (index > 0) {
					selectNode = parent.getChildAt(index - 1);
				} else {
					selectNode = parent;
				}
			}
			parent.remove(selectedNode);
		}
		rebuildProfile();
		TableUtils.reloadTree(this.table);
		TableUtils.selectNode((DefaultMutableTreeNode) selectNode, table);
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) table.getTree().getModel().getRoot();
		if (root.getChildCount() == 0) {
			disableCopyDeleteButton();
		}
	}

	private class AddNewItemAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode selectedNode = getSelectedNode();
			CheckedTreeNode newChild;
			if (selectedNode == null) {
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) table.getTree().getModel().getRoot();
				GrepExpressionGroupTreeNode aNew = new GrepExpressionGroupTreeNode(new GrepExpressionGroup("new"));
				newChild = new GrepExpressionItemTreeNode(newItem());
				aNew.add(newChild);
				root.add(aNew);
			} else if (selectedNode.getUserObject() instanceof GrepExpressionGroup) {
				newChild = new GrepExpressionItemTreeNode(newItem());
				selectedNode.add(newChild);
			} else {
				GrepExpressionGroupTreeNode parent = (GrepExpressionGroupTreeNode) selectedNode.getParent();
				newChild = new GrepExpressionItemTreeNode(newItem());
				parent.insert(newChild, parent.getIndex(selectedNode) + 1);
			}
			rebuildProfile();
			TableUtils.reloadTree(table);
			TableUtils.selectNode(newChild, table);
		}
	}

	private class AddSoundAction implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent actionEvent)
		{
			showSoundDialog(getProfile().getSound());
		}
	}

	private class AddCommandAction implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent actionEvent)
		{
			showCommandDialog(getProfile().getCommand());
		}
	}

	private class AddNewGroupAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) table.getTree().getModel().getRoot();
			GrepExpressionGroupTreeNode aNew = new GrepExpressionGroupTreeNode(new GrepExpressionGroup("new"));
			root.add(aNew);
			rebuildProfile();
			TableUtils.reloadTree(table);
			TableUtils.selectNode(aNew, table);
		}
	}

	private class ResetToDefaultAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			SettingsDialog.this.settings.setProfiles(DefaultState.createDefault());
			disableCopyDeleteButton();
			importFrom(SettingsDialog.this.settings);
		}
	}

	private class DuplicateAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode selectedNode = getSelectedNode();
			if (selectedNode instanceof GrepExpressionItemTreeNode) {
				GrepExpressionItemTreeNode newChild = new GrepExpressionItemTreeNode(copy(selectedNode));
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
				parent.insert(newChild, parent.getIndex(selectedNode) + 1);

				TableUtils.reloadTree(SettingsDialog.this.table);
				TableUtils.selectNode(newChild, SettingsDialog.this.table);
			} else if (selectedNode instanceof GrepExpressionGroupTreeNode) {
				GrepExpressionGroup group = copy((GrepExpressionGroup) selectedNode.getUserObject());
				GrepExpressionGroupTreeNode newChild = new GrepExpressionGroupTreeNode(group);
				for (GrepExpressionItem grepExpressionItem : group.getGrepExpressionItems()) {
					newChild.add(new GrepExpressionItemTreeNode(grepExpressionItem));
				}
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
				parent.insert(newChild, parent.getIndex(selectedNode) + 1);

				TableUtils.reloadTree(SettingsDialog.this.table);
				TableUtils.expand(newChild, SettingsDialog.this.table);
				TableUtils.selectNode(newChild, SettingsDialog.this.table);
			}
			rebuildProfile();
		}

		private GrepExpressionGroup copy(final GrepExpressionGroup grepExpressionGroup) {
			GrepExpressionGroup group = deepClone(grepExpressionGroup);
			for (GrepExpressionItem grepExpressionItem : group.getGrepExpressionItems()) {
				grepExpressionItem.generateNewId();
			}
			return group;
		}

		private GrepExpressionItem copy(DefaultMutableTreeNode selectedNode) {
			GrepExpressionItem expressionItem = deepClone((GrepExpressionItem) selectedNode.getUserObject());
			expressionItem.generateNewId();
			return expressionItem;
		}
	}

	private class DeleteAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			delete();
		}
	}

	private class FileTailSettings implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final TailIntegrationForm form = new TailIntegrationForm();
			form.setData(settings.getTailSettings());

			DialogBuilder builder = new DialogBuilder(SettingsDialog.this.getRootComponent());
			builder.setCenterPanel(form.getRoot());
			builder.setDimensionServiceKey("GrepConsoleTailFileDialog");
			builder.setTitle("Tail File settings");
			builder.removeAllActions();
			builder.addOkAction();
			builder.addCancelAction();

			boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
			if (isOk) {
				form.getData(settings.getTailSettings());
				GrepConsoleApplicationComponent.getInstance().getState().setTailSettings(settings.getTailSettings());
				form.rebind(settings.getTailSettings());
			}
		}
	}

	private boolean showCommandDialog(ConsoleCommand command)
	{
		DialogBuilder builder = new DialogBuilder(this.getRootComponent());
		CommandSettingsForm commandSettingsForm = new CommandSettingsForm();
		builder.setCenterPanel(commandSettingsForm.getRoot());
		builder.setDimensionServiceKey("GrepConsoleCommand");
		builder.setTitle("Command settings");
		builder.removeAllActions();
		builder.addOkAction();
		builder.addCancelAction();
		commandSettingsForm.setData(command);
		boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
		if (isOk)
		{
			commandSettingsForm.getData(command);
			setCommandIcon();
		}
		getProfile().setCommand(command);
		return isOk;
	}

	private boolean showSoundDialog(Sound sound)
	{
		DialogBuilder builder = new DialogBuilder(this.getRootComponent());
		SoundSettingsForm soundSettingsForm = new SoundSettingsForm();
		builder.setCenterPanel(soundSettingsForm.getRoot());
		builder.setDimensionServiceKey("GrepConsoleSound");
		builder.setTitle("Sound settings");
		builder.removeAllActions();
		builder.addOkAction();
		builder.addCancelAction();
		soundSettingsForm.setData(sound);
		boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
		if (isOk)
		{
			soundSettingsForm.getData(sound);
			setSoundIcon();
		}
		getProfile().setSound(sound);
		return isOk;
	}

}
