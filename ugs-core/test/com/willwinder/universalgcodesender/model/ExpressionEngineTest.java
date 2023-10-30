/*
    Copyright 2017 Will Winder

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

import com.willwinder.universalgcodesender.model.ExpressionEngine;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import javax.script.ScriptEngine;
import javax.script.ScriptContext;

/**
 *
 * @author coco
 */
public class ExpressionEngineTest {

    // private static final String FIRMWARE = "GRBL";
    // private static final String PORT = "/dev/ttyS0";
    // private static final int BAUD_RATE = 9600;

    // private IController controller;

    // private Settings settings;


    @Before
    public void setup() {}

    @After
    public void teardown() {}

    @Test
    public void testSyncUserVariablesToScriptingScope() throws Exception {
        ExpressionEngine engine = new ExpressionEngine();

        engine.getVariables().set("myVar1", "3.14");
        engine.getVariables().set("myVar2", "2.71");
        engine.getVariables().set("myVar3", "2.66");

        engine.syncUserVariablesToScriptingScope();

        Assert.assertEquals("3.14", engine.getBindings().get("myVar1").toString());
        Assert.assertEquals("2.71", engine.getBindings().get("myVar2").toString());
        Assert.assertEquals("2.66", engine.getBindings().get("myVar3").toString());
    }

    @Test
    public void testSyncScriptingScopeToUserVariables() throws Exception {
        ExpressionEngine engine = new ExpressionEngine();
        ScriptEngine scope = engine.getScriptEngine();

        scope.eval("toolOffset = 66.6 / 3");
        scope.eval("PROBE_Z = toolOffset + 3");

        engine.syncScriptingScopeToUserVariables();

        Assert.assertEquals("22.2", engine.getVariables().get("toolOffset"));
        Assert.assertEquals("25.2", engine.getVariables().get("PROBE_Z"));
    }
}
