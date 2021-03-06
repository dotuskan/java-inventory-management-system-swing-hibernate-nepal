package com.gt.common.utils;

import com.gt.uilib.components.input.DataComboBox;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.FocusManager;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

public class UIUtils {
    public static final char BACKSPACE_KEY = '\b';
    private static final Color COMPONENT_BORDER_COLOR = Color.GRAY;

    public static boolean isEmpty(JTextComponent jt) {
        String str = jt.getText();
        if (StringUtils.isEmpty(str)) {
            return true;
        }

        return false;
    }

    public static JComponent decoratePreviousComponentFocus(JComponent comp, final char key) {
        comp.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                if (key != BACKSPACE_KEY) {
                    return;
                }
                if (e.getKeyChar() == key) {
                    FocusManager.getCurrentManager().focusPreviousComponent();
                }
            }
        });
        return comp;
    }

    public static void clearAllFields(Component parent, Component... ignoreList) {
        List<Component> list = null;

        if (ignoreList != null) {
            list = Arrays.asList(ignoreList);
        }

        if (parent instanceof JTextField) {
            ((JTextField) parent).setText("");
        } else if (parent instanceof JTextField) {
            ((JTextField) parent).setText("");
        } else if (parent instanceof JTextArea) {
            ((JTextArea) parent).setText("");
        } else if (parent instanceof JComboBox) {
            ((JComboBox) parent).setSelectedIndex(0);
        } else if (parent instanceof JCheckBox) {
            ((JCheckBox) parent).setSelected(false);
        } else if (parent instanceof JComponent) {
            Component[] children = ((JComponent) parent).getComponents();
            for (int i = 0; children.length > i; i++) {
                if (list != null && list.contains(children[i])) {
                    continue;
                }
                clearAllFields(children[i]);
            }
        }
    }

    public static void clearAllFields(Component parent) {
        if (parent instanceof JTextField) {
            // if (((JTextField) parent).isEditable()) {
            ((JTextField) parent).setText("");
            // }
        }
        if (parent instanceof JTextArea) {
            if (((JTextArea) parent).isEditable()) {
                ((JTextArea) parent).setText("");
            }
        } else if (parent instanceof JPasswordField) {
            if (((JPasswordField) parent).isEditable()) {
                ((JPasswordField) parent).setText("");
            }
        } else if (parent instanceof DataComboBox) {
            ((DataComboBox) parent).selectDefaultItem();
        } else if (parent instanceof JComponent) {
            Component[] children = ((JComponent) parent).getComponents();
            for (int i = 0; children.length > i; i++) {
                clearAllFields(children[i]);
            }
        }
    }

    public static void toggleAllChildren(Component parent, boolean enabled, Component... ignoreList) {
        List<Component> igList = Arrays.asList(ignoreList);

        if (parent == null) {
            return;
        }
        Color disabledColor = (Color) UIManager.get("TextField.inactiveBackground");
        Color enabledColor = (Color) UIManager.get("TextField.background");
        if (!igList.contains(parent)) {
            parent.setEnabled(enabled);
            if (parent instanceof JTextComponent || parent instanceof JDateChooser || parent instanceof DataComboBox || parent instanceof JComboBox) {
                if (!enabled) parent.setBackground(disabledColor);
                if (enabled) parent.setBackground(enabledColor);
            }
        }

        if (parent instanceof JComponent) {
            Component[] children = ((JComponent) parent).getComponents();
            for (int i = 0; children.length > i; i++) {
                if (children[i] instanceof JLabel) {
                    continue;
                }
                // System.out.println(children.getClass());
                if (!igList.contains(children[i])) {
                    toggleAllChildren(children[i], enabled);
                }
            }
        }
    }

    public static void updateFont(final Font font, JComponent p) {
        for (Component c : p.getComponents()) {
            if (c instanceof JToolBar) {
                continue;
            } else if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;
                jc.updateUI();
                if (jc.getComponentCount() > 0) {
                    updateFont(font, jc);
                }
                c.setFont(font);
            }
        }
    }

    public static void decorateBorders(JComponent p) {
        for (Component c : p.getComponents()) {
            if (c instanceof JToolBar) {
                continue;
            } else if (c instanceof JComboBox) {
//				if (jc instanceof JComboBox) {
                ((JComboBox) c).setBorder(BorderFactory.createLineBorder(COMPONENT_BORDER_COLOR, 1));
//				}
            } else if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;

                if (jc.getComponentCount() > 0) {
                    decorateBorders(jc);
                }
                if (jc instanceof JTextField) {
                    ((JTextField) jc).setBorder(BorderFactory.createLineBorder(COMPONENT_BORDER_COLOR, 1));
                }
                if (jc instanceof JTextArea) {
                    ((JTextArea) jc).setBorder(BorderFactory.createLineBorder(COMPONENT_BORDER_COLOR, 1));
                }

                // if (jc instanceof JDateChooser) {
                // ((JDateChooser) jc).setBorder(BorderFactory.createLineBorder(
                // Color.GRAY, 1));
                // }
                jc.updateUI();
            }
        }
    }
}
