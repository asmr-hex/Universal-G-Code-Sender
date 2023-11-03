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

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.ExpressionEngineEvent;
import com.willwinder.universalgcodesender.model.UGSEventDispatcher;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for handling parsing of Javascript expressions.
 *
 * @author coco
 */
public class ExpressionEngine implements UGSEventListener {
    public Pattern pattern = Pattern.compile("\\{[^}]+\\}");

    private Bindings variables = null;
    private Set<String> saved = new HashSet<>();
    private ScriptEngineManager mgr = new ScriptEngineManager();
    private ScriptEngine engine = null;

    private BackendAPI backend = null;
    private UGSEventDispatcher dispatcher = null;

    public class BuiltinVariables {
        public static final String MachineX = "machine_x";
        public static final String MachineY = "machine_y";
        public static final String MachineZ = "machine_z";
        public static final String WorkX    = "work_x";
        public static final String WorkY    = "work_y";
        public static final String WorkZ    = "work_z";
        // TODO make lambdas return Object since we are using bindings.put
        public static HashMap<String, BiFunction<ControllerStatus, Units, Object>> getters;
        static {
            getters = new HashMap<>();
            getters.put(MachineX, (ControllerStatus status, Units units) -> status.getMachineCoord().getPositionIn(units).get(Axis.X));
            getters.put(MachineY, (ControllerStatus status, Units units) -> status.getMachineCoord().getPositionIn(units).get(Axis.Y));
            getters.put(MachineZ, (ControllerStatus status, Units units) -> status.getMachineCoord().getPositionIn(units).get(Axis.Z));
            getters.put(WorkX, (ControllerStatus status, Units units) -> status.getWorkCoord().getPositionIn(units).get(Axis.X));
            getters.put(WorkY, (ControllerStatus status, Units units) -> status.getWorkCoord().getPositionIn(units).get(Axis.Y));
            getters.put(WorkZ, (ControllerStatus status, Units units) -> status.getWorkCoord().getPositionIn(units).get(Axis.Z));
        }

        public static List<String> names() {
            return new ArrayList(getters.keySet());
        }

        public static void init(Bindings bindings) {
            for (String key : getters.keySet()) {
                bindings.put(key, "");
            }
        }

        public static void update(ControllerStatus status, Units units, Bindings bindings) {
            for (Map.Entry<String, BiFunction<ControllerStatus, Units, Object>> entry : getters.entrySet()) {
                bindings.put(entry.getKey(), entry.getValue().apply(status, units));
            }
        }
    }


    public ExpressionEngine(BackendAPI backend, UGSEventDispatcher dispatcher) {
        this.engine = mgr.getEngineByName("JavaScript");
        this.variables = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        this.backend = backend;
        this.dispatcher = dispatcher;
        BuiltinVariables.init(this.variables);

        this.backend.addUGSEventListener(this);
    }

    public void load() {
        Settings settings = this.backend.getSettings();
        if (settings == null) return;

        for (Map.Entry<String, Object> entry : settings.getSavedVariables().entrySet()) {
            this.variables.put(entry.getKey(), entry.getValue());
            saved.add(entry.getKey());
        }
    }

    public String eval(String expression) throws Exception {
        // TODO filter on builtins and saves.
        check(expression);

        return this.engine.eval(expression).toString();
    }

    public void put(String key, Object value) {
        // TODO filter on builtins and saves.

        this.variables.put(key, value);

        this.dispatcher.sendUGSEvent(new ExpressionEngineEvent(this.variables));
    }

    public Object get(String key) {
        return this.variables.get(key);
    }

    public void remove(String key) {
        // TODO filter on builtins and saves.

        this.variables.remove(key);

        this.dispatcher.sendUGSEvent(new ExpressionEngineEvent(this.variables));
    }

    public void rename(String oldName, String newName) {
        // TODO filter on builtins and saves.

        this.variables.put(newName, this.variables.get(oldName));
        this.variables.remove(oldName);

        this.dispatcher.sendUGSEvent(new ExpressionEngineEvent(this.variables));
    }

    public void save(String var, Boolean shouldSave) {
        if (shouldSave && !isSaved(var)) {
            this.saved.add(var);
            this.backend.getSettings().addSavedVariable(var, this.variables.get(var));
            SettingsFactory.saveSettings(this.backend.getSettings());
        }

        if (!shouldSave && isSaved(var)) {
            this.saved.remove(var);
            this.backend.getSettings().removeSavedVariable(var);
            SettingsFactory.saveSettings(this.backend.getSettings());
        }
    }

    public Boolean isSaved(String var) {
        return this.saved.contains(var);
    }

    public Bindings getVars() {
        return this.variables;
    }

    /**
     * This takes a single command line string and evaluates contained expressions
     * if any are present. If any variables contained within the expressions cannot
     * be resolved, i.e. they have not been defined, then this method throws an
     * exception. This should halt the running program.
     *
     * @returns modified command line string with all expressions evaluated
     * @throws Exception if variables cannot be resolved, or if expression is invalid
     */
    public String process(String commandText) throws Exception {
        StringBuilder result = new StringBuilder();
        Matcher matcher = pattern.matcher(commandText);
        boolean expressionsFound = false;
        while (matcher.find()) {
            expressionsFound = true;

            String match = matcher.group();
            String expression = match.substring(1, match.length() - 1);

            check(expression);

            // evaluate the expression in the JavaScript engine.
            String evaluated = eval(expression);

            // TODO if evaluated is null....raise

            // if the entire command is just an expression, put it in comments so it
            // isn't evaluated by the controller.
            if (match.trim().equals(commandText.trim())) {
                evaluated = String.format("(%s -> %s)", expression.trim(), evaluated);
            }

            matcher.appendReplacement(result, evaluated);
        }
        matcher.appendTail(result);

        if (expressionsFound)
            this.dispatcher.sendUGSEvent(new ExpressionEngineEvent(this.variables));

        return result.toString().trim();
    }

    private void check(String expression) throws Exception {
        // TODO check that expression doesn't mutate builtin or saved variables
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStatusEvent controllerStatusEvent) {
            // when the controller status has changed, update builtins, dispatch.
            BuiltinVariables.update(controllerStatusEvent.getStatus(), this.backend.getSettings().getPreferredUnits(), this.variables);
            this.dispatcher.sendUGSEvent(new ExpressionEngineEvent(this.variables));
        }
    }
}
