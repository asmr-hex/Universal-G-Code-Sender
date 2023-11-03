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

import com.willwinder.universalgcodesender.model.ExpressionEngine;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A table model that allows for reading/writing expression variables.
 *
 * @author coco
 */
public class VariablesTableModel extends AbstractTableModel {

    public static final int COLUMN_VARNAME  = 0;
    public static final int COLUMN_VARVALUE = 1;
    public static final int COLUMN_SAVED    = 2;

    public List<String>  builtinVarNames = new ArrayList<>();
    public List<String>  userVarNames    = new ArrayList<>();
    public List<Boolean> userVarSaved    = new ArrayList<>();

    private ExpressionEngine engine = null;

    public VariablesTableModel(ExpressionEngine engine) {
        this.engine = engine;
    }

    @Override
    public int getRowCount() {
        return this.engine.getVars().size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int nBuiltins = builtinVarNames.size();
        if (rowIndex < nBuiltins) {
            switch (columnIndex) {
            case COLUMN_VARNAME:
                return this.builtinVarNames.get(rowIndex);
            case COLUMN_VARVALUE:
                return this.engine.get(this.builtinVarNames.get(rowIndex));
            }
            return null;
        }

        // this is a user var
        String varName = this.userVarNames.get(rowIndex - nBuiltins);

        switch (columnIndex) {
        case COLUMN_VARNAME:
            return varName;
        case COLUMN_VARVALUE:
            return this.engine.get(varName);
        case COLUMN_SAVED:
            return userVarSaved.get(rowIndex-nBuiltins);
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
        case COLUMN_SAVED:
            return "Save";
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case COLUMN_VARNAME:
            return String.class;
        case COLUMN_VARVALUE:
            return Object.class;
        case COLUMN_SAVED:
            return Boolean.class;
        }
        return Object.class;
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {
        int nBuiltins = builtinVarNames.size();
        if (row < nBuiltins)
            return;

        int oldRow = row;
        row = row - nBuiltins;

        switch (col) {
        case COLUMN_VARNAME:
            String newVarName = aValue.toString();
            String oldVarName = userVarNames.get(row);
            this.engine.rename(oldVarName, newVarName);
            break;
        case COLUMN_VARVALUE:
            this.engine.put(userVarNames.get(row), aValue);
            break;
        case COLUMN_SAVED:
            this.engine.save(userVarNames.get(row), (Boolean)aValue);
            userVarSaved.set(row, (Boolean)aValue);
            break;
        }
        fireTableCellUpdated(oldRow, col);
    }

    public void removeRow(int row) {
        int nBuiltins = builtinVarNames.size();
        if (row < nBuiltins || userVarSaved.get(row-nBuiltins))
            return;

        this.engine.remove(userVarNames.get(row-nBuiltins));
        fireTableRowsDeleted(row, row);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        int nBuiltins = builtinVarNames.size();
        if (row < nBuiltins)
            return false;

        row = row - nBuiltins;

        if (col < COLUMN_SAVED && userVarSaved.get(row))
            return false;

        return true;
    }

    public void update() {
        List<String> varNames = new ArrayList(this.engine.getVars().keySet());
        List<String> builtinNames = ExpressionEngine.BuiltinVariables.names();
        List<String> newBuiltinVarNames = new ArrayList();
        List<String> newUserVarNames = new ArrayList();
        List<Boolean> newUserVarSaved = new ArrayList();

        for (String n : varNames) {
            if (builtinNames.contains(n)) {
                newBuiltinVarNames.add(n);
            }
            else {
                newUserVarNames.add(n);
                newUserVarSaved.add(this.engine.isSaved(n));
            }
        }

        builtinVarNames = newBuiltinVarNames;
        userVarNames    = newUserVarNames;
        userVarSaved    = newUserVarSaved;

        // TODO (coco|2023.11.2) smartly diff?
        fireTableDataChanged();
    }
}
