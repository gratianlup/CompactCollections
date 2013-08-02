# Copyright (c) 2013 Gratian Lup. All rights reserved.
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#
# * Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
#
# * Redistributions in binary form must reproduce the above
# copyright notice, this list of conditions and the following
# disclaimer in the documentation and/or other materials provided
# with the distribution.
#
# * The name "CompactCollections" must not be used to endorse or promote
# products derived from this software without prior written permission.
#
# * Products derived from this software may not be called "CompactCollections" nor
# may "CompactCollections" appear in their names without prior written
# permission of the author.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
package compactcollections.tests;
import compactcollections.VariableIntArray;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VariableIntArrayTest {
    @Test
    public void testAddGet() {
        VariableIntArray array = new VariableIntArray();

        for(int i = 0; i < 1000000; i++) {
            array.addValue(i + 1);
        }

        for(int i = 0; i < 1000000; i++) {
            Assert.assertEquals(array.getValue(i), i + 1);
        }
    }

    @Test
    public void testRandomAddGet() {
        Random random = new Random();
        List<Integer> values = new ArrayList<Integer>();
        VariableIntArray array = new VariableIntArray();

        for(int i = 0; i < 1000000; i++) {
            int value = random.nextInt();
            array.addValue(value);
            values.add(value);
        }

        for(int i = 0; i < 1000000; i++) {
            Assert.assertEquals(array.getValue(i), (int)values.get(i));
        }
    }

    @Test
    public void testSet() {
        VariableIntArray array = new VariableIntArray();

        for(int i = 0; i < 1000000; i++) {
            array.addValue(i + 1);
        }

        for(int i = 0; i < 1000000; i++) {
            if(i % 100 == 0) {
                array.setValue(i, i + 2);
            }
        }

        for(int i = 0; i < 1000000; i++) {
            if(i % 100 == 0) {
                Assert.assertEquals(array.getValue(i), i + 2);
            }
            else Assert.assertEquals(array.getValue(i), i + 1);
        }
    }

    @Test
    public void testRandomSet() {
        Random random = new Random();
        List<Integer> values = new ArrayList<Integer>();
        VariableIntArray array = new VariableIntArray();

        for(int i = 0; i < 1000000; i++) {
            int value = random.nextInt();
            array.addValue(value);
            values.add(value);
        }

        for(int i = 0; i < 1000000; i++) {
            if(i % 10 == 0) {
                int value = random.nextInt();
                array.setValue(i, value);
                values.set(i, value);
            }
        }

        for(int i = 0; i < 1000000; i++) {
            Assert.assertEquals(array.getValue(i), (int)values.get(i));
        }
    }

    @Test
    public void testRandomSetConsecutvie() {
        VariableIntArray array = new VariableIntArray();

        for(int i = 0; i < 1000000; i++) {
            array.setValue(i, i + 1);
        }

        for(int i = 0; i < 1000000; i++) {
            Assert.assertEquals(array.getValue(i), i + 1);
        }
    }
}
