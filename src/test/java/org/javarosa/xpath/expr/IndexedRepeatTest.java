package org.javarosa.xpath.expr;

import org.javarosa.core.test.Scenario;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class IndexedRepeatTest {
    @Test public void indexedRepeatWithRelativeXPathExpressionAsFirstParam() {
        Scenario scenario = Scenario.init("indexed-repeat-nested.xml");
        scenario.createMissingRepeats("/data/my-repeat[0]/my-nested-repeat[0]");
        String uuid = scenario.answerOf("/data/my-repeat[0]/my-nested-repeat[0]/parent").getDisplayText();
        assertThat(scenario.answerOf("/data/my-repeat[0]/my-nested-repeat[0]/indexed-repeat-parent").getDisplayText(), is(uuid));
    }
}
