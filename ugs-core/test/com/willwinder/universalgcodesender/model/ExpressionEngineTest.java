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

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.utils.Settings;

import java.util.regex.Matcher;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author coco
 */
public class ExpressionEngineTest {

    @Before
    public void setup() throws Exception {}

    @After
    public void teardown() {}

    @Test
    public void testPut() throws Exception {
        ExpressionEngine engine = new ExpressionEngine();

        engine.put("myVar1", "3.14");
        engine.put("myVar2", "2.71");
        engine.put("myVar3", "2.66");

        Assert.assertEquals("3.14", engine.get("myVar1"));
        Assert.assertEquals("2.71", engine.get("myVar2"));
        Assert.assertEquals("2.66", engine.get("myVar3"));
    }

    @Test
    public void testEval() throws Exception {
        ExpressionEngine engine = new ExpressionEngine();

        engine.eval("toolOffset = 66.6 / 3");
        engine.eval("PROBE_Z = toolOffset + 3");

        Assert.assertEquals("22.2", engine.get("toolOffset"));
        Assert.assertEquals("25.2", engine.get("PROBE_Z"));
    }

    @Test
    public void testEvalAfterPut() throws Exception {
        ExpressionEngine engine = new ExpressionEngine();

        engine.put("MeasuredHeight", "55.3");
        engine.eval("toolOffset = 4.7 + MeasuredHeight");

        Assert.assertEquals("55.3", engine.get("MeasuredHeight"));
        Assert.assertEquals("60.0", engine.get("toolOffset"));
    }

    @Test
    public void testExpressionPatternMatcher() {
        ExpressionEngine engine = new ExpressionEngine();
        Matcher matcher;

        matcher = engine.pattern.matcher("${an expression}");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals("${an expression}", matcher.group(0));
        // Assert.assertEquals("${an expression}", matcher.group(0));

        matcher = engine.pattern.matcher("${an expression} ${another expression}");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals("${an expression}", matcher.group());
        Assert.assertTrue(matcher.find());
        Assert.assertEquals("${another expression}", matcher.group());

        matcher = engine.pattern.matcher("G0 ${MZ} F800");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals("${MZ}", matcher.group());
    }

    @Test
    public void testProcess() throws Exception {
        Settings settings = new Settings();
        AbstractController controller = mock(AbstractController.class);
        when(controller.getControllerStatus())
            .thenReturn(
                new ControllerStatus(ControllerState.IDLE,
                                     new Position(1, 2, 33, UnitUtils.Units.MM),
                                     new Position(7, 8, 9, UnitUtils.Units.MM)));

        ExpressionEngine engine = new ExpressionEngine();
        engine.connect(controller, settings);

        String result;

        result = engine.process("${myVar = 543}");
        Assert.assertEquals("(myVar = 543 -> 543)", result);
        Assert.assertEquals("543", engine.get("myVar"));

        result = engine.process("${   myVar = 543}");
        Assert.assertEquals("(myVar = 543 -> 543)", result);
        Assert.assertEquals("543", engine.get("myVar"));

        result = engine.process("  ${   myVar = 543}   ");
        Assert.assertEquals("(myVar = 543 -> 543)", result);
        Assert.assertEquals("543", engine.get("myVar"));

        result = engine.process("${myVar = machine_z}");
        Assert.assertEquals("(myVar = machine_z -> 33.0)", result);
        Assert.assertEquals("33.0", engine.get("myVar"));


        result = engine.process("G0 ${myVar = 234}");
        Assert.assertEquals("G0 234", result);
        Assert.assertEquals("234", engine.get("myVar"));

        result = engine.process("G0 ${myVar = 321} F800");
        Assert.assertEquals("G0 321 F800", result);
        Assert.assertEquals("321", engine.get("myVar"));

        result = engine.process("G0 X100 Y300 F800");
        Assert.assertEquals("G0 X100 Y300 F800", result);
    }
}
