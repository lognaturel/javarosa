/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.xpath.expr;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.XPathNodeset;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.javarosa.xpath.expr.XPathFuncExpr.randomize;
import static org.junit.Assert.assertTrue;

public class RandomizeTest {
    @Test
    public void emptyNodesetIsEmptyWhenRandomized() {
        XPathNodeset nodeset = new XPathNodeset(new ArrayList<TreeReference>(), null, null);
        XPathNodeset result = (XPathNodeset) randomize(nodeset, null);

        assertTrue(nodesEqualInAnyOrder(nodeset, result));
        assertTrue(nodesEqualInOrder(nodeset, result));
    }

    @Test
    public void test() {
        List<TreeReference> refs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            refs.add(new TreeReference());
        }
        XPathNodeset nodeset = new XPathNodeset(refs, null, null);
        XPathNodeset result = (XPathNodeset) randomize(nodeset, null);

        assertTrue(nodesEqualInAnyOrder(nodeset, result));
        assertFalse(nodesEqualInOrder(nodeset, result));
    }

    private boolean nodesEqualInOrder(XPathNodeset nodeset1, XPathNodeset nodeset2) {
        if (nodeset1.size() != nodeset2.size()) {
            return false;
        }

        for (int i = 0; i < nodeset1.size(); i++) {
            // compare object references
            if (nodeset1.getRefAt(i) != (nodeset2.getRefAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean nodesEqualInAnyOrder(XPathNodeset nodeset1, XPathNodeset nodeset2) {
        if (nodeset1.size() != nodeset2.size()) {
            return false;
        }

        for (int i = 0; i < nodeset1.size(); i++) {
            boolean match = false;
            for (int j = 0; j < nodeset2.size(); j++) {
                if (nodeset1.getRefAt(i) == nodeset2.getRefAt(j)) {
                    match = true;
                    break;
                }
            }

            if (!match) {
                return false;
            }
        }
        return true;
    }
}
