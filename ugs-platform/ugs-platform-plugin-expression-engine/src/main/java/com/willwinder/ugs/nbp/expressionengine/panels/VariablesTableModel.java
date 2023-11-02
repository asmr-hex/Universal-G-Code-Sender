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

    private static final int COLUMN_VARNAME  = 0;
    private static final int COLUMN_VARVALUE = 1;
    private static final int COLUMN_LOCKED   = 2;
    private static final int COLUMN_SAVED    = 3;

    public List<String>  builtinVarNames = new ArrayList<>();
    public List<String>  userVarNames    = new ArrayList<>();
    public List<Boolean> userVarLocked   = new ArrayList<>();
    public List<Boolean> userVarSaved    = new ArrayList<>();

    ExpressionEngine engine = null;

    public VariablesTableModel(ExpressionEngine engine) {
        this.engine = engine;
    }

    @Override
    public int getRowCount() {
        return this.engine.getVars().size();
    }

    @Override
    public int getColumnCount() {
        return 4;
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
        case COLUMN_LOCKED:
            // TODO should return a checkbox or something if not a builtin
        case COLUMN_SAVED:
            // TODO should return a checkbox or something if not a builtin
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
        case COLUMN_LOCKED:
            return "Locked";
        case COLUMN_SAVED:
            return "Saved";
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {
        int nBuiltins = builtinVarNames.size();
        if (row < nBuiltins)
            return;

        int oldRow = row;
        row = nBuiltins - row;

        switch (col) {
        case COLUMN_VARNAME:
            System.out.println();
            System.out.println("UPDATING VARIABLE NAME");
            String varName = aValue.toString();
            String oldVarName = userVarNames.get(row);
            this.engine.put(varName, this.engine.get(oldVarName));
            System.out.println("ABOUT TO REMOVE");
            this.engine.remove(oldVarName);
            System.out.println("DONE");
            System.out.println();
        case COLUMN_VARVALUE:
            this.engine.put(userVarNames.get(row), aValue);
        }
        fireTableCellUpdated(oldRow, col);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        int nBuiltins = builtinVarNames.size();
        if (row < nBuiltins)
            return false;

        row = nBuiltins - row;

        if (col < COLUMN_LOCKED && userVarLocked.get(row))
            return false;

        return true;
    }

    public void update() {
        List<String> varNames = new ArrayList(this.engine.getVars().keySet());
        List<String> builtinNames = ExpressionEngine.BuiltinVariables.names();
        List<String> newBuiltinVarNames = new ArrayList();
        List<String> newUserVarNames = new ArrayList();
        List<Boolean> newUserVarLocked = new ArrayList();
        List<Boolean> newUserVarSaved = new ArrayList();

        for (String n : varNames) {
            if (builtinNames.contains(n)) {
                newBuiltinVarNames.add(n);
            }
            else {
                newUserVarNames.add(n);
                newUserVarLocked.add(false);
                newUserVarSaved.add(false);
            }
        }

        builtinVarNames = newBuiltinVarNames;
        userVarNames    = newUserVarNames;
        userVarLocked   = newUserVarLocked;
        userVarSaved    = newUserVarSaved;

        // TODO (coco|2023.11.2) smartly diff?
        fireTableDataChanged();
    }
}
