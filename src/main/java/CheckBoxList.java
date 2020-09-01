import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class CheckBoxList {
	private JPanel parentPanel;
	private List<Component> components;
	private Node<Integer> componentIndexTree;
	private int maxy;

	public CheckBoxList(JPanel p) {
		parentPanel = p;
		components = new ArrayList<Component>();
		componentIndexTree = new Node<Integer>(-1);
	}

	public void addCheckBox(JCheckBox c) {
		components.add(c);
		int index = components.size() - 1;
		Node<Integer> node = new Node<Integer>(index);
		componentIndexTree.addChild(node);
		c.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkBoxStateChange(node);
			}
		});
	}

	public Node<Integer> searchCheckBox(Node<Integer> node, JCheckBox c) {
		for (Node<Integer> children : node.getChildren()) {
			if (c.equals(components.get(children.getData())))
				return children;
			else if (!children.isLeaf()) {
				Node<Integer> childResult = searchCheckBox(children, c);
				if (childResult != null)
					return childResult;
			}
		}
		return null;
	}

	public void addPanelChild(JCheckBox parent, JPanel child) {
		Node<Integer> parentCheckbox = searchCheckBox(componentIndexTree, parent);
		if (parentCheckbox != null) {
			Globals.setPanelVisibility(child, false);
			components.add(child);
			int index = components.size() - 1;
			Node<Integer> node = new Node<Integer>(index);
			parentCheckbox.addChild(node);
		}
	}

	public void addCheckBoxChild(JCheckBox parent, JCheckBox child) {
		Node<Integer> parentCheckbox = searchCheckBox(componentIndexTree, parent);
		if (parentCheckbox != null) {
			child.setEnabled(false);
			components.add(child);
			int index = components.size() - 1;
			Node<Integer> node = new Node<Integer>(index);
			parentCheckbox.addChild(node);
			child.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					checkBoxStateChange(node);
				}
			});
		}
	}

	public void show(int x, int y) {
		maxy = y;
		for (Node<Integer> children : componentIndexTree.getChildren()) {
			show(children, x, maxy);
			maxy += 10;
		}
	}

	public void show(Node<Integer> node, int x, int y) {
		Component nodeComponent = components.get(node.getData());
		nodeComponent.setBounds(x, y, nodeComponent.getBounds().width, nodeComponent.getBounds().height);
		parentPanel.add(nodeComponent);

		if (nodeComponent.isVisible())
			maxy += nodeComponent.getBounds().height;

		if (nodeComponent instanceof JCheckBox) {
			for (Node<Integer> children : node.getChildren()) {
				if (components.get(children.getData()) instanceof JCheckBox)
					show(children, x + 20, maxy);
				if (components.get(children.getData()) instanceof JPanel)
					show(children, x, maxy);
			}
		}
	}

	public void checkBoxStateChange(Node<Integer> node) {
		JCheckBox checkBox = (JCheckBox) components.get(node.getData());
		for (Node<Integer> children : node.getChildren()) {
			Component nodeComponent = components.get(children.getData());
			if (nodeComponent instanceof JCheckBox) {
				if (checkBox.isSelected())
					((JCheckBox) nodeComponent).setEnabled(true);
				else {
					((JCheckBox) nodeComponent).setEnabled(false);
					if (((JCheckBox) nodeComponent).isSelected()) {
						((JCheckBox) nodeComponent).setSelected(false);
						checkBoxStateChange(children);
					}
				}
			}
			if (nodeComponent instanceof JPanel) {
				if (checkBox.isSelected()) {
					Globals.setPanelVisibility((JPanel) nodeComponent, true);
					moveLowerComponents(nodeComponent, 0, nodeComponent.getBounds().height);
				} else {
					Globals.setPanelVisibility((JPanel) nodeComponent, false);
					moveLowerComponents(nodeComponent, 0, -nodeComponent.getBounds().height);
				}
			}
		}
		Globals.updatePanelSize(parentPanel);
	}

	public void moveLowerComponents(Component component, int x, int y) {
		Component[] components = parentPanel.getComponents();

		for (Component component2 : components) {
			if (component2 != component && component.getBounds().y <= component2.getBounds().y)
				Globals.moveComponent(component2, x, y);
		}
	}
}
