/*
    Copyright 2023 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.expressionengine.panels;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A table model that allows for reading/writing expression variables.
 *
 * @author coco
 */
public class VariablesTableModel extends AbstractTableModel {

    private static final int COLUMN_VARNAME = 0;
    private static final int COLUMN_VARVALUE = 1;
    // public final List<WorkflowFile> fileList = new ArrayList<>();

    // public void addRow(WorkflowFile workflowFile) {
    //     fileList.add(workflowFile);
    //     fireTableRowsInserted(fileList.size(), fileList.size());
    // }

    @Override
    public int getRowCount() {
        // TODO count number of builtins
        // return fileList.size();

        return 5;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // TODO access bindings
        // WorkflowFile workflowFile = fileList.get(rowIndex);
        switch (columnIndex) {
            case COLUMN_VARNAME:
                // return workflowFile.getFile();
                return String.format("myVar%d", rowIndex);

            case COLUMN_VARVALUE:
                return String.format("%d", rowIndex);
        }
        return null;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
        case COLUMN_VARNAME:
            return "Variable";

        case COLUMN_VARVALUE:
            return "Value";
        }
        return null;
    }

    // TODO implement this if the entries are editable
    // @Override
    // public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    //     if (columnIndex == COLUMN_TOOLNAME) {
    //         get(rowIndex).setTool(new WorkflowTool(aValue.toString()));
    //         fireTableCellUpdated(rowIndex, columnIndex);
    //     } else if (columnIndex == COLUMN_COMPLETED) {
    //         get(rowIndex).setCompleted((Boolean) aValue);
    //         fireTableCellUpdated(rowIndex, columnIndex);
    //     }
    // }

    // TODO implement this if entries can be editable
    // @Override
    // public boolean isCellEditable(int rowIndex, int columnIndex) {
    //     return columnIndex == 1 || columnIndex == 2;
    // }
}
