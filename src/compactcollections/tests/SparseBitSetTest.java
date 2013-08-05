// Copyright (c) 2013 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "CompactCollections" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "CompactCollections" nor
// may "CompactCollections" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
package compactcollections.tests;
import compactcollections.SparseBitSet;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SparseBitSetTest {
    @Test
    public void testSetGet() {
        SparseBitSet set = new SparseBitSet();

        for(int i = 0; i < 10000; i++) {
            set.setBit(i);
        }

        for(int i = 0; i < 10000; i++) {
            Assert.assertTrue(set.getBit(i));
        }
    }

    @Test
    public void testSetGetRandom() {
        Random random = new Random(59);
        SparseBitSet set = new SparseBitSet();
        List<Integer> bits = new ArrayList<Integer>();
        int maxIndex = -1;

        for(int i = 0; i < 10000; i++) {
            int index = random.nextInt(32768) + 32768;
            set.setBit(index);
            bits.add(index);
            maxIndex = Math.max(index, maxIndex);
        }

        for(int i = 0; i < 10000; i++) {
            int index = bits.get(i);
            Assert.assertTrue(set.getBit(index));
        }

        for(int i = 0; i < maxIndex; i++) {
            if(!bits.contains(i)) {
                Assert.assertFalse(set.getBit(i));
            }
            else break;
        }
    }
}
