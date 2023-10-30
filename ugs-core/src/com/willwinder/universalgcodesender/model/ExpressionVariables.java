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


import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

import java.util.function.BiFunction;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class for storing variables which may be accessible for read/write
 * within JavaScript expressions in Macros or Gcode files.
 *
 * @author coco
 */
// TODO maybe just extend from HashMap so we get put, get, entrySet for free...
public class ExpressionVariables {
    public class Builtin {
        public static final String MachineX = "machine_x";
        public static final String MachineY = "machine_y";
        public static final String MachineZ = "machine_z";
        public static final String WorkX    = "work_x";
        public static final String WorkY    = "work_y";
        public static final String WorkZ    = "work_z";

        public static HashMap<String, BiFunction<ControllerStatus, Units, String>> getters;
        static {
            getters = new HashMap<>();
            getters.put(MachineX, (ControllerStatus status, Units units) -> Double.toString(status.getMachineCoord().getPositionIn(units).get(Axis.X)));
            getters.put(MachineY, (ControllerStatus status, Units units) -> Double.toString(status.getMachineCoord().getPositionIn(units).get(Axis.Y)));
            getters.put(MachineZ, (ControllerStatus status, Units units) -> Double.toString(status.getMachineCoord().getPositionIn(units).get(Axis.Z)));
            getters.put(WorkX, (ControllerStatus status, Units units) -> Double.toString(status.getWorkCoord().getPositionIn(units).get(Axis.X)));
            getters.put(WorkY, (ControllerStatus status, Units units) -> Double.toString(status.getWorkCoord().getPositionIn(units).get(Axis.Y)));
            getters.put(WorkZ, (ControllerStatus status, Units units) -> Double.toString(status.getWorkCoord().getPositionIn(units).get(Axis.Z)));
        }

        public HashMap<String,String> variables = new HashMap<String,String>();

        public void update(ControllerStatus status, Units units) {
            for (Map.Entry<String, BiFunction<ControllerStatus, Units, String>> entry : getters.entrySet())
                variables.put(entry.getKey(), entry.getValue().apply(status, units).toString());
        }
    }

    public Builtin builtin = new Builtin();
    private HashMap<String,String> userVariables = new HashMap<String,String>();
    private HashMap<String, Boolean> lockedUserVariables = new HashMap<String, Boolean>();

    public ExpressionVariables() {
        // TODO: maybe ingest UGS settings to see if we have persisted any variables
    }

    public void set(String variableName, String variableValue) {
        this.set(variableName, variableValue, false);
    }

    public String get(String variableName) {
        return userVariables.get(variableName);
    }

    public Set<Map.Entry<String,String>> entrySet() {
        return userVariables.entrySet();
    }

    public void lock(String variableName) {
        this.set(variableName, userVariables.get(variableName), true);
    }

    private void set(String variableName, String variableValue, boolean lock) {
        userVariables.put(variableName, variableValue);
        lockedUserVariables.put(variableName, lock);
    }

}
