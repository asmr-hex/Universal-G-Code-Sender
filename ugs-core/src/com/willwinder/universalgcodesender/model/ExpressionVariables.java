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
package com.willwinder.universalgcodesender.model;

import java.util.HashMap;

/**
 * A class for storing variables which may be accessible for read/write
 * within JavaScript expressions in Macros or Gcode files.
 *
 * @author coco
 */
public class ExpressionVariables {
    private HashMap<String,String> internalVariables = new HashMap<String,String>();
    private HashMap<String,String> userDefinedVariables = new HashMap<String,String>();
    private HashMap<String,boolean> lockedUserDefinedVariables = new HashMap<String,boolean>();

    public ExpressionVariables() {
        // TODO: maybe ingest UGS settings to see if we have persisted any variables
    }

    public void set(String variableName, String variableValue) {
        this.set(variableName, variableValue, false);
    }

    public String get(String variableName) {
        return userDefinedVariables.get(variableName);
    }

    public void lock(String variableName) {
        this.set(variableName, userDefinedVariables.get(variableName), true);
    }

    public void setInternal(String variableName, String variableValue) {
        internalVariables.put(variableName, variableValue);
    }

    public String getInternal(String variableName) {
        return internalVariables.get(variableName);
    }

    private void set(String variableName, String variableValue, boolean lock) {
        userDefinedVariables.put(variableName, variableValue);
        lockedUserDefinedVariables.put(variableName, lock);
    }

}
