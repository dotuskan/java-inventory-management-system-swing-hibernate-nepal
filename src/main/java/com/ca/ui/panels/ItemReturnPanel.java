package com.ca.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.ca.db.model.Category;
import com.ca.db.model.Nikasa;
import com.ca.db.service.DBUtils;
import com.ca.db.service.ItemReturnServiceImpl;
import com.ca.db.service.NikasaServiceImpl;
import com.ca.db.service.dto.ReturnedItemDTO;
import com.gt.common.constants.Status;
import com.gt.common.utils.DateTimeUtils;
import com.gt.common.utils.ExcelUtils;
import com.gt.common.utils.UIUtils;
import com.gt.uilib.components.AbstractFunctionPanel;
import com.gt.uilib.components.input.DataComboBox;
import com.gt.uilib.components.table.BetterJTable;
import com.gt.uilib.components.table.BetterJTableNoSorting;
import com.gt.uilib.components.table.EasyTableModel;
import com.gt.uilib.inputverifier.Validator;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.toedter.calendar.JDateChooser;

/**
 * entry of returned items
 *
 * @author GT
 */
public class ItemReturnPanel extends AbstractFunctionPanel {
    String[] header = new String[]{"S.N.", "ID", "Name", "Category", "Specification", "Nikasa Date", "Niksa Type", "Sent To", "Nikasa Pana Num",
            "Request Number", "Remaining Quantity", "Unit"};
    String[] returnTblHeader = new String[]{"", "ID", "Name", "Category", "Goods Status", "Return Quantity", "Unit"};
    String[] damageStatusStr = new String[]{"", "Good", "Unrepairable", "Needs Repair", "Exemption"};
    ReturnTable returnTable;
    JPanel formPanel = null;
    JPanel buttonPanel;
    Validator v;
    Validator vCart;
    JDateChooser txtFromDate;
    JDateChooser txtToDate;
    List cellQtyEditors = new ArrayList();
    int idCol = 1;
    int qtyCol = 5;
    int damageStatusCol = 4;
    private JButton btnSave;
    private JPanel upperPane;
    private JPanel lowerPane;
    private BetterJTable table;
    private EasyTableModel dataModel;
    private EasyTableModel cartDataModel;
    private DataComboBox cmbCategory;
    private JLabel lblVendor;
    private JLabel lblPanaNumber;
    private JTextField txtPanaNumber;
    private JLabel lblFrom;
    private JLabel lblTo;
    private JButton btnSaveToExcel;
    private JButton btnPrev;
    private JButton btnNext;
    private JLabel lblItemName;
    private JTextField txtItemname;
    private JPanel receiverHolder;
    private JLabel lblReceiver;

	/*
     * Some Inner classes
	 */
    private ItemReceiverPanel itemReceiverPanel;
    private JTextField txtItemRequestNumber;
    private JPanel addToCartPanel;
    private JSplitPane lowerPanel;
    private JPanel panel_1;
    private JPanel cartPanel;
    private JDateChooser nikasaDateChooser;
    private JLabel lblSentDate;
    private JButton btnSend;
    private JButton btnAddItem;
    private JButton btnDelete;
    private JPanel panel_3;
    // List cellRackNumberEditors = new ArrayList();
    private JLabel lblNiksasaPanaNumber;
    private JTextField txtReturnNUmber;
    private JButton btnReset;

    public ItemReturnPanel() {
        /**
         * all gui components added from here;
         */
        JSplitPane splitPane = new JSplitPane();
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.1);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        add(splitPane, BorderLayout.CENTER);
        splitPane.setLeftComponent(getUpperSplitPane());
        splitPane.setRightComponent(getLowerPanel());
        /**
         * never forget to call after setting up UI
         */
        init();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame jf = new JFrame();
                    ItemReturnPanel panel = new ItemReturnPanel();
                    jf.setBounds(panel.getBounds());
                    jf.getContentPane().add(panel);
                    jf.setVisible(true);
                    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private JSplitPane getLowerPanel() {
        if (lowerPanel == null) {
            lowerPanel = new JSplitPane();
            lowerPanel.setContinuousLayout(true);
            lowerPanel.setResizeWeight(0.6);
            lowerPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);

            lowerPanel.setLeftComponent(getLowerSplitPane());

            panel_1 = new JPanel();
            lowerPanel.setRightComponent(panel_1);
            panel_1.setLayout(new BorderLayout(0, 0));

            cartPanel = new JPanel();
            cartPanel.setBorder(new TitledBorder(null, "Item Return Entry", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            panel_1.add(cartPanel, BorderLayout.CENTER);
            cartPanel.setLayout(new FormLayout(new ColumnSpec[]{FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                    FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(45dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
                    ColumnSpec.decode("left:max(27dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(15dlu;default)"),
                    FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(24dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
                    ColumnSpec.decode("max(9dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(124dlu;default)"),
                    FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(59dlu;default)"),}, new RowSpec[]{FormFactory.RELATED_GAP_ROWSPEC,
                    RowSpec.decode("top:max(31dlu;default)"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("top:max(15dlu;default)"),}));

            panel_3 = new JPanel();
            cartPanel.add(panel_3, "2, 2, fill, fill");
            panel_3.setLayout(new FormLayout(new ColumnSpec[]{FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,}, new RowSpec[]{
                    FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,}));

            btnAddItem = new JButton("Add Item");
            panel_3.add(btnAddItem, "2, 2");

            btnDelete = new JButton("Remove");
            btnDelete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (returnTable.getRowCount() > 0) {
                        int selRow = returnTable.getSelectedRow();
                        if (selRow != -1) {
                            /**
                             * if second column doesnot have primary id info,
                             * then
                             */

                            int selectedId = (Integer) cartDataModel.getKeyAtRow(selRow);
                            System.out.println("Selected ID : " + selectedId + "_  >>  row " + selRow);
                            if (cartDataModel.containsKey(selectedId)) {
                                removeSelectedRowInCartTable(selectedId, selRow);
                            }

                        }
                    }
                }
            });
            panel_3.add(btnDelete, "2, 4");
            btnAddItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (table.getRowCount() > 0) {
                        int selRow = table.getSelectedRow();
                        if (selRow != -1) {
                            /**
                             * if second column doesnot have primary id info,
                             * then
                             */

                            int selectedId = (Integer) dataModel.getKeyAtRow(selRow);

                            if (!cartDataModel.containsKey(selectedId)) {
                                addSelectedRowInCartTable(selectedId);
                            } else {
                                JOptionPane.showMessageDialog(null, "This Item Already Selected", "Duplicate Selection", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            });

            cartPanel.add(getAddToCartPane(), "4, 2, 13, 1, fill, top");

            lblNiksasaPanaNumber = new JLabel("Return Number");
            cartPanel.add(lblNiksasaPanaNumber, "4, 4, left, default");

            txtReturnNUmber = new JTextField();
            cartPanel.add(txtReturnNUmber, "6, 4, fill, default");
            txtReturnNUmber.setColumns(10);

            lblSentDate = new JLabel("Date");
            cartPanel.add(lblSentDate, "10, 4, default, top");

            nikasaDateChooser = new JDateChooser();
            nikasaDateChooser.setDate(new Date());
            cartPanel.add(nikasaDateChooser, "14, 4, fill, top");

            btnSend = new JButton("Receive");
            btnSend.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    // System.out.println(itemReceiverPanel.getSelectedId() +
                    // " >><<>>>>>>>>>><<<<<<<");
                    if (!isValidCart()) {
                        JOptionPane.showMessageDialog(null, "Please fill the required data");
                        return;
                    }

                    btnSend.setEnabled(false);

                    SwingWorker worker = new SwingWorker<Void, Void>() {

                        @Override
                        protected Void doInBackground() throws Exception {
                            if (DataEntryUtils.confirmDBSave()) saveReturn();
                            return null;
                        }

                    };
                    worker.addPropertyChangeListener(new PropertyChangeListener() {

                        public void propertyChange(PropertyChangeEvent evt) {
                            System.out.println("Event " + evt + " name" + evt.getPropertyName() + " value " + evt.getNewValue());
                            if ("DONE".equals(evt.getNewValue().toString())) {
                                btnSend.setEnabled(true);
                                // task.setText("Test");
                            }
                        }
                    });

                    worker.execute();
                }
            });
            cartPanel.add(btnSend, "16, 4, default, top");
        }

        return lowerPanel;
    }

    private void saveReturn() {

        try {
            ItemReturnServiceImpl irs = new ItemReturnServiceImpl();
            irs.saveReturnedItem(returnTable.getIdAndQuantityMap(), txtReturnNUmber.getText().trim());
            handleNikasaSuccess();
        } catch (Exception er) {
            handleDBError(er);
        }

        btnSend.setEnabled(true);

    }

    private boolean isValidCart() {
        if (returnTable.isValidCartQty() && returnTable.getRowCount() > 0 && nikasaDateChooser.getDate() != null) {
            return true;
        }
        return false;
    }

    protected void handleNikasaSuccess() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JOptionPane.showMessageDialog(null, "Saved Successfully");
                cartDataModel.resetModel();
                cartDataModel.fireTableDataChanged();
                UIUtils.clearAllFields(cartPanel);
                itemReceiverPanel.clearAll();
                dataModel.resetModel();
                dataModel.fireTableDataChanged();
                cellQtyEditors.clear();
                // cellRackNumberEditors.clear();
            }
        });
    }

    protected void removeSelectedRowInCartTable(int selectedId, int selRow) {
        cartDataModel.removeRowWithKey(selectedId);
        cartDataModel.fireTableDataChanged();
        // TODO:
        cellQtyEditors.remove(selRow);
        // cellRackNumberEditors.remove(selRow);
        // cartTable.setModel(cartDataModel);
        // cartTable.adjustColumns();
    }

    protected void addSelectedRowInCartTable(int selectedId) {
        try {
            Nikasa bo = (Nikasa) DBUtils.getById(Nikasa.class, selectedId);
            int sn = cartDataModel.getRowCount();
            if (bo != null) {
                // BigDecimal total = bo.getRate().multiply(new
                // BigDecimal(bo.getQuantity()));
                cartDataModel.addRow(new Object[]{++sn, bo.getId(), bo.getItem().getName(), bo.getItem().getCategory().getCategoryName(), "", 0,
                        bo.getItem().getUnitsString().getValue()});
                returnTable.setModel(cartDataModel);
                cartDataModel.fireTableDataChanged();

                cellQtyEditors.add(new CartTableQuantityCellEditor(bo.getRemainingQtyToReturn()));
                // cellRackNumberEditors.add(new
                // CartTableNewRackNumberCellEditor());
                JComboBox comboBox = new JComboBox(damageStatusStr);
                comboBox.setEditable(false);
                DefaultCellEditor editor = new DefaultCellEditor(comboBox);
                TableColumnModel tcm = returnTable.getColumnModel();
                tcm.getColumn(4).setCellEditor(editor);
            }

        } catch (Exception e) {
            System.out.println("populateSelectedRowInForm");
            handleDBError(e);
        }
    }

    @Override
    public void init() {
		/* never forget to call super.init() */
        super.init();
        UIUtils.clearAllFields(upperPane);
        changeStatus(Status.NONE);
        intCombo();
    }

    private void intCombo() {
        try {
			/* Category Combo */
            cmbCategory.init();
            ItemReturnServiceImpl is = new ItemReturnServiceImpl();
            List<Category> cl = is.getNonReturnableCategory();
            for (Category c : cl) {
                cmbCategory.addRow(new Object[]{c.getId(), c.getCategoryName()});
            }
        } catch (Exception e) {
            handleDBError(e);
        }
    }

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();

            btnSaveToExcel = new JButton("Save to Excel");
            btnSaveToExcel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser jf = new JFileChooser();
                    jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    jf.showDialog(ItemReturnPanel.this, "Select Save location");
                    String fileName = jf.getSelectedFile().getAbsolutePath();
                    try {
                        ExcelUtils.writeExcelFromJTable(table, fileName + ".xls");
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(null, "Could not save" + e1.getMessage());
                    }
                }
            });

            btnPrev = new JButton("<");
            buttonPanel.add(btnPrev);

            btnNext = new JButton(">");
            buttonPanel.add(btnNext);
            buttonPanel.add(btnSaveToExcel);
        }
        return buttonPanel;
    }

    @Override
    public void enableDisableComponents() {
        switch (status) {
            case NONE:
                // UIUtils.toggleAllChildren(buttonPanel, false);
                // UIUtils.toggleAllChildren(formPanel, false);
                UIUtils.clearAllFields(formPanel);
                table.setEnabled(true);
                btnSave.setEnabled(true);
                break;

            case READ:
                // UIUtils.toggleAllChildren(formPanel, false);
                // UIUtils.toggleAllChildren(buttonPanel, true);
                UIUtils.clearAllFields(formPanel);
                table.clearSelection();
                table.setEnabled(true);
                break;

            default:
                break;
        }
    }

    @Override
    public void handleSaveAction() {

    }

    private void initValidator() {

        if (v != null) {
            v.resetErrors();
        }

        v = new Validator(mainApp, true);

    }

    private JPanel getUpperFormPanel() {
        if (formPanel == null) {
            formPanel = new JPanel();

            formPanel.setBorder(new TitledBorder(null, "Nikasa Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            formPanel.setBounds(10, 49, 474, 135);
            formPanel.setLayout(new FormLayout(new ColumnSpec[]{FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                    FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                    FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:max(128dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
                    ColumnSpec.decode("left:max(26dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                    FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                    ColumnSpec.decode("max(125dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                    FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,}, new RowSpec[]{FormFactory.RELATED_GAP_ROWSPEC,
                    FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                    FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,}));

            lblItemName = new JLabel("Item Name");
            formPanel.add(lblItemName, "4, 2");

            txtItemname = new JTextField();
            formPanel.add(txtItemname, "8, 2, fill, default");
            txtItemname.setColumns(10);
            //
            // specPanelHolder = new JPanel();
            // formPanel.add(specPanelHolder, "4, 6, 15, 1, fill, fill");
            // specPanelHolder.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

            lblPanaNumber = new JLabel("Nikasa Number");
            formPanel.add(lblPanaNumber, "12, 2");

            txtPanaNumber = new JTextField();
            formPanel.add(txtPanaNumber, "16, 2, fill, default");
            txtPanaNumber.setColumns(10);

            JLabel lblN = new JLabel("Category");
            formPanel.add(lblN, "4, 4");

            cmbCategory = new DataComboBox();
            formPanel.add(cmbCategory, "8, 4, fill, default");

            lblVendor = new JLabel("Item Request No.");
            formPanel.add(lblVendor, "12, 4, default, top");

            txtItemRequestNumber = new JTextField();
            formPanel.add(txtItemRequestNumber, "16, 4, fill, default");
            txtItemRequestNumber.setColumns(10);

            lblFrom = new JLabel("From");
            formPanel.add(lblFrom, "4, 6");

            txtFromDate = new JDateChooser();
            formPanel.add(txtFromDate, "8, 6, fill, default");

            lblTo = new JLabel("To");
            formPanel.add(lblTo, "12, 6");

            txtToDate = new JDateChooser();
            txtToDate.setDate(new Date());
            formPanel.add(txtToDate, "16, 6, fill, default");

            btnSave = new JButton("Search");
            btnSave.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handleSearchQuery();
                }
            });

            lblReceiver = new JLabel("Receiver :");
            formPanel.add(lblReceiver, "4, 8, default, center");

            receiverHolder = new JPanel();
            itemReceiverPanel = new ItemReceiverPanel();
            receiverHolder.add(itemReceiverPanel);
            itemReceiverPanel.hideLilam();
            formPanel.add(receiverHolder, "8, 8, fill, fill");

            formPanel.add(btnSave, "18, 8, default, bottom");

            btnReset = new JButton("Reset");
            formPanel.add(btnReset, "20, 8, default, bottom");
            btnReset.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    UIUtils.clearAllFields(formPanel);
                    // if(currentSpecificationPanel!=null)
                    // currentSpecificationPanel.resetAll();
                    cmbCategory.selectDefaultItem();
                    // cmbVendor.selectDefaultItem();
                    itemReceiverPanel.clearAll();
                }
            });
        }
        return formPanel;
    }

    protected void handleSearchQuery() {
        readAndShowAll(true);
    }

    private void readAndShowAll(boolean showSize0Error) {
        try {
            NikasaServiceImpl is = new NikasaServiceImpl();
            List<Nikasa> brsL;
            // FIXME : pananumber vs - request number ??
            int returnStatus = -1;

            brsL = is.notReturnedNikasaItemQuery(txtItemname.getText(), cmbCategory.getSelectedId(), itemReceiverPanel.getCurrentReceiverConstant(),
                    itemReceiverPanel.getSelectedId(), returnStatus, -1, txtPanaNumber.getText().trim(), "", txtFromDate.getDate(),
                    txtToDate.getDate());

            if (brsL == null || brsL.size() == 0) {
                if (showSize0Error) {
                    JOptionPane.showMessageDialog(null, "No Records Found");
                }
                dataModel.resetModel();
                dataModel.fireTableDataChanged();
                table.adjustColumns();
                return;
            }
            showListInGrid(brsL);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    private void showListInGrid(List<Nikasa> brsL) {
        dataModel.resetModel();
        int sn = 0;
        String nikasaTYpe = "";
        String sentTo = "";
        for (Nikasa bo : brsL) {
            nikasaTYpe = "";
            sentTo = "";

            if (bo.getNikasaType() == Nikasa.OFFICIAL) {
                nikasaTYpe = "Official";
                sentTo = bo.getBranchOffice().getName() + "  " + bo.getBranchOffice().getAddress();
            } else if (bo.getNikasaType() == Nikasa.PERSONNAL) {
                nikasaTYpe = "Personnal";
                sentTo = bo.getPerson().getFirstName() + "  " + bo.getPerson().getLastName();
            }
            // TODO: add person/office name, specs in column
            dataModel.addRow(new Object[]{++sn, bo.getId(), bo.getItem().getName(), bo.getItem().getCategory().getCategoryName(),
                    bo.getItem().getSpeciifcationString(), DateTimeUtils.getCvDateMMMddyyyy(bo.getNikasaDate()), nikasaTYpe, sentTo,
                    bo.getNikasaPanaNumber(), bo.getNikasaRequestNumber(), bo.getRemainingQtyToReturn(), bo.getItem().getUnitsString().getValue()});
        }
        table.setModel(dataModel);
        dataModel.fireTableDataChanged();
        table.adjustColumns();
    }

    @Override
    public String getFunctionName() {
        return "Item Return Entry";
    }

    private JPanel getUpperSplitPane() {
        if (upperPane == null) {
            upperPane = new JPanel();
            upperPane.setLayout(new BorderLayout(0, 0));
            upperPane.add(getUpperFormPanel(), BorderLayout.CENTER);
            upperPane.add(getButtonPanel(), BorderLayout.SOUTH);
        }
        return upperPane;
    }

    private JPanel getLowerSplitPane() {
        if (lowerPane == null) {
            lowerPane = new JPanel();
            lowerPane.setLayout(new BorderLayout());
            dataModel = new EasyTableModel(header);

            table = new BetterJTable(dataModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane sp = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

            lowerPane.add(sp, BorderLayout.CENTER);
        }
        return lowerPane;
    }

    private JPanel getAddToCartPane() {
        if (addToCartPanel == null) {
            addToCartPanel = new JPanel();
            addToCartPanel.setLayout(new BorderLayout());
            cartDataModel = new EasyTableModel(returnTblHeader);

            returnTable = new ReturnTable(cartDataModel);
            returnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            returnTable.setRowSorter(null);

            Object key = returnTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(KeyStroke.getKeyStroke("ENTER"));
            final Action action = returnTable.getActionMap().get(key);
            Action custom = new AbstractAction("wrap") {

                public void actionPerformed(ActionEvent e) {
                    // int row =
                    // cartTable.getSelectionModel().getLeadSelectionIndex();
                    // if (row == cartTable.getRowCount() - 1) {
                    // do custom stuff
                    // return if default shouldn't happen or call default
                    // after
                    return;
                    // }
                    // action.actionPerformed(e);
                }

            };
            // cartTable.getActionMap().put(key, custom);
            JScrollPane sp = new JScrollPane(returnTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            // TODO: number of rows into scrl pane
            addToCartPanel.add(sp, BorderLayout.CENTER);
        }
        return addToCartPanel;
    }

    public class CartTableQuantityCellEditor extends AbstractCellEditor implements TableCellEditor {
        JComponent component = new JTextField();
        int maxQuantity = 0;

        public CartTableQuantityCellEditor(int maxQuantity) {
            this.maxQuantity = maxQuantity;
        }

        /*
         * This method is called when a cell value is edited by the
         * user.(non-Javadoc)
         *
         * @see
         * javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax
         * .swing.JTable, java.lang.Object, boolean, int, int)
         */
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {

            if (isSelected) {
            }

            // Configure the component with the specified value
            ((JTextField) component).setText(value.toString());

            // Return the configured component
            return component;
        }

        /**
         * This method is called when editing is completed.<br>
         * It must return the new value to be stored in the cell.
         */
        public Object getCellEditorValue() {
            Integer retQty = 0;
            try {
                retQty = Integer.parseInt(((JTextField) component).getText());
                // if max
                if (retQty > maxQuantity) {
                    JOptionPane.showMessageDialog(null, "The maximum qty remaining to return is " + maxQuantity, "Max Qty Exceed",
                            JOptionPane.INFORMATION_MESSAGE);
                    retQty = 0;
                }

            } catch (Exception e) {
                retQty = 0;
            }
            return retQty <= 0 ? "0" : retQty;
        }
    }

    public class CartTableNewRackNumberCellEditor extends AbstractCellEditor implements TableCellEditor {
        JComponent component = new JTextField();

        /*
         * This method is called when a cell value is edited by the
         * user.(non-Javadoc)
         *
         * @see
         * javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax
         * .swing.JTable, java.lang.Object, boolean, int, int)
         */
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
            // Configure the component with the specified value
            ((JTextField) component).setText(value.toString());

            return component;
        }

        /**
         * This method is called when editing is completed.<br>
         * It must return the new value to be stored in the cell.
         */
        public Object getCellEditorValue() {

            return ((JTextField) component).getText();
        }
    }

    class ReturnTable extends BetterJTableNoSorting {

        /**
         * ID at sec col, Qty at 6th
         */

        public ReturnTable(TableModel dm) {
            super(dm);
        }

        public boolean isCellEditable(int row, int column) {
            return (column == qtyCol || column == damageStatusCol) ? true : false;
        }

        /**
         * if at lease 1 item has greater than 0 qty, others will be ignored
         * during saving
         *
         * @return
         */
        public boolean isValidCartQty() {
            Map<Integer, ReturnedItemDTO> cartMap = returnTable.getIdAndQuantityMap();
            for (Entry<Integer, ReturnedItemDTO> entry : cartMap.entrySet()) {
                ReturnedItemDTO ret = entry.getValue();
                Integer qty = ret.qty;
                Integer damageStatus = ret.damageStatus;
                if (qty > 0 && damageStatus > 0) {
                    return true;
                }
            }
            return false;

        }

        private int getDamageStatusIndex(String str) {
            for (int i = 0; i < damageStatusStr.length; i++) {
                if (str.trim().equals(damageStatusStr[i])) {
                    return i;
                }
            }
            return -1;
        }

        public Map<Integer, ReturnedItemDTO> getIdAndQuantityMap() {
            Map<Integer, ReturnedItemDTO> cartIdQtyMap = new HashMap<Integer, ReturnedItemDTO>();
            int rows = getRowCount();
            for (int i = 0; i < rows; i++) {
                Integer id = Integer.parseInt(getValueAt(i, idCol).toString());
                Integer qty = Integer.parseInt(getValueAt(i, qtyCol).toString());
                // String rackNumber = getValueAt(i, rackCol).toString();
                int damageStatus = getDamageStatusIndex(getValueAt(i, damageStatusCol).toString());

                /**
                 * Put the items that have qty >0 only
                 */
                if (qty > 0) {
                    cartIdQtyMap.put(id, new ReturnedItemDTO(qty, damageStatus, ""));
                }
            }
            return cartIdQtyMap;
        }

        // Determine editor to be used by row
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == qtyCol) {
                return (TableCellEditor) cellQtyEditors.get(row);
            }
            // if (column == rackCol) {
            // return (TableCellEditor) cellRackNumberEditors.get(row);
            // }

            else
                return super.getCellEditor(row, column);
        }

    }
}
