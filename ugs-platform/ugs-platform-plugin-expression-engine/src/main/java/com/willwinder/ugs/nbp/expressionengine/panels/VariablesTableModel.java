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
        if (rowIndex < builtinVarNames.size()) {
            switch (columnIndex) {
            case COLUMN_VARNAME:
                return this.builtinVarNames.get(rowIndex);
            case COLUMN_VARVALUE:
                return this.engine.get(this.builtinVarNames.get(rowIndex));
            }
            return null;
        }

        switch (columnIndex) {
        case COLUMN_VARNAME:
            return this.userVarNames.get(rowIndex);
        case COLUMN_VARVALUE:
            return this.engine.get(this.userVarNames.get(rowIndex));
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

    // @Override
    // public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    //     if (columnIndex == COLUMN_TOOLNAME) {

    //     } else if (columnIndex == COLUMN_COMPLETED) {

    //     }
    // }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (rowIndex < builtinVarNames.size())
            return false;

        if (columnIndex < COLUMN_LOCKED && !userVarLocked.get(columnIndex))
            return false;

        return true;
    }

    public void update() {
        List<String> varNames = new ArrayList(this.engine.getVars().keySet());
        List<String> builtinNames = ExpressionEngine.BuiltinVariables.names();
        List<String> newBuiltinVarNames = new ArrayList();
        List<String> newUserVarNames = new ArrayList();

        for (String n : varNames) {
            if (builtinNames.contains(n)) {
                newBuiltinVarNames.add(n);
            }
            else {
                newUserVarNames.add(n);
            }
        }

        builtinVarNames = newBuiltinVarNames;
        userVarNames    = newUserVarNames;
    }
}
