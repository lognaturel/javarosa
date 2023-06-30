package org.javarosa.core.model.test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.group;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.instance;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

import org.javarosa.core.test.Scenario;
import org.javarosa.measure.Measure;
import org.junit.Test;

public class PredicateCachingTest {

    @Test
    public void repeatedEqPredicatesAreOnlyEvaluatedOnceWhileAnswering() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("calculate1"),
                        t("calculate2")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/calculate1").type("string")
                        .calculate("instance('instance')/root/item[value = /data/choice]/label"),
                    bind("/data/calculate2").type("string")
                        .calculate("instance('instance')/root/item[value = /data/choice]/value")
                )
            ),
            body(
                input("/data/choice")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "a");
        });

        // Check that we do less than (size of secondary instance) * (number of calculates with a filter)
        assertThat(evaluations, lessThan(4));
    }

    @Test
    public void repeatedCompPredicatesAreOnlyEvaluatedOnceWhileAnswering() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("calculate1"),
                        t("calculate2")
                    )),
                    instance("instance",
                        item("1", "A"),
                        item("2", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/calculate1").type("string")
                        .calculate("instance('instance')/root/item[value < /data/choice]/label"),
                    bind("/data/calculate2").type("string")
                        .calculate("instance('instance')/root/item[value < /data/choice]/value")
                )
            ),
            body(
                input("/data/choice")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "2");
        });

        // Check that we do less than (size of secondary instance) * (number of calculates with a filter)
        assertThat(evaluations, lessThan(4));
    }

    @Test
    public void repeatedIdempotentFuncPredicatesAreOnlyEvaluatedOnceWhileAnswering() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("calculate1"),
                        t("calculate2")
                    )),
                    instance("instance",
                        item("1", "A"),
                        item("2", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/calculate1").type("string")
                        .calculate("instance('instance')/root/item[regex(value, /data/choice)]/label"),
                    bind("/data/calculate2").type("string")
                        .calculate("instance('instance')/root/item[regex(value, /data/choice)]/value")
                )
            ),
            body(
                input("/data/choice")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "1");
        });

        // Check that we do less than (size of secondary instance) * (number of calculates with a filter)
        assertThat(evaluations, lessThan(4));
    }

    @Test
    public void repeatedEqPredicatesAreOnlyEvaluatedOnce() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("calculate1"),
                        t("calculate2")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/calculate1").type("string")
                        .calculate("instance('instance')/root/item[value = /data/choice]/label"),
                    bind("/data/calculate2").type("string")
                        .calculate("instance('instance')/root/item[value = /data/choice]/value")
                )
            ),
            body(
                input("/data/choice")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "a");
            scenario.answer("/data/choice", "b");
        });

        // Check that we do less than size of secondary instance * number of times we answer
        assertThat(evaluations, lessThan(4));
    }

    @Test
    public void firstPredicateInMultipleEqPredicatesAreOnlyEvaluatedOnce() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("calc"),
                        t("input1"),
                        t("input2")
                    )),
                    instance("instance",
                        t("item",
                            t("value", "A"),
                            t("count", "2"),
                            t("id", "A2")
                        ),
                        t("item",
                            t("value", "A"),
                            t("count", "3"),
                            t("id", "A3")
                        ),
                        t("item",
                            t("value", "B"),
                            t("count", "2"),
                            t("id", "B2")
                        )
                    ),
                    bind("/data/calc").type("string")
                        .calculate("instance('instance')/root/item[value = /data/input1][count = /data/input2]/id"),
                    bind("/data/input1").type("string"),
                    bind("/data/input2").type("string")
                )
            ),
            body(
                input("/data/input1"),
                input("/data/input2")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/input1", "A");
            scenario.answer("/data/input2", "3");

            scenario.answer("/data/input1", "A");
            scenario.answer("/data/input2", "2");
        });

        // Check that we do less than size of (secondary instance + filtered secondary instance) * number of times we answer
        assertThat(evaluations, lessThan(20));
    }

    @Test
    public void repeatedCompPredicatesWithSameAnswerAreOnlyEvaluatedOnce() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("choice"),
                        t("calculate1"),
                        t("calculate2")
                    )),
                    instance("instance",
                        item("1", "A"),
                        item("2", "B")
                    ),
                    bind("/data/choice").type("string"),
                    bind("/data/calculate1").type("string")
                        .calculate("instance('instance')/root/item[value < /data/choice]/label"),
                    bind("/data/calculate2").type("string")
                        .calculate("instance('instance')/root/item[value < /data/choice]/value")
                )
            ),
            body(
                input("/data/choice")
            )
        ));

        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
            scenario.answer("/data/choice", "2");
            scenario.answer("/data/choice", "2");
        });

        // Check that we do less than size of secondary instance * number of times we answer
        assertThat(evaluations, lessThan(4));
    }

    /**
     * A form with multiple secondary instances can have expressions with "equivalent" predicates that filter on
     * different sets of children. It's pretty possible to write a bug where these predicates are treated as the same
     * thing causing incorrect answers.
     */
    @Test
    public void equivalentPredicateExpressionsOnDifferentReferencesAreNotConfused() throws Exception {
        Scenario scenario = Scenario.init("two-secondary-instances.xml");

        scenario.next();
        scenario.answer("a");
        assertThat(scenario.answerOf("/data/both").getValue(), equalTo("AA"));
    }

    @Test
    public void equivalentPredicateExpressionsInRepeatsDoNotGetConfused() throws Exception {
        Scenario scenario = Scenario.init("repeat-secondary-instance.xml");

        scenario.createNewRepeat("/data/repeat");
        scenario.createNewRepeat("/data/repeat");

        scenario.answer("/data/repeat[0]/choice", "a");
        assertThat(scenario.answerOf("/data/repeat[0]/calculate").getValue(), equalTo("A"));
        assertThat(scenario.answerOf("/data/repeat[1]/calculate"), equalTo(null));

        scenario.answer("/data/repeat[1]/choice", "b");
        assertThat(scenario.answerOf("/data/repeat[0]/calculate").getValue(), equalTo("A"));
        assertThat(scenario.answerOf("/data/repeat[1]/calculate").getValue(), equalTo("B"));
    }

    @Test
    public void predicatesOnDifferentChildNamesDoNotGetConfused() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("cat"),
                        t("dog"),
                        t("input")
                    )),
                    instance("instance",
                        t("cat",
                            t("name", "Vinnie"),
                            t("age", "12")
                        ),
                        t("dog",
                            t("name", "Vinnie"),
                            t("age", "9")
                        )
                    ),
                    bind("/data/cat").type("string")
                        .calculate("instance('instance')/root/cat[name = /data/input]/age"),
                    bind("/data/dog").type("string")
                        .calculate("instance('instance')/root/dog[name = /data/input]/age"),
                    bind("/data/input").type("string")
                )
            ),
            body(input("/data/input"))
        ));

        scenario.answer("/data/input", "Vinnie");

        assertThat(scenario.answerOf("/data/cat").getValue(), equalTo("12"));
        assertThat(scenario.answerOf("/data/dog").getValue(), equalTo("9"));
    }

    @Test
    public void eqExpressionsWorkIfEitherSideIsRelative() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("calcltr"),
                        t("calcrtl"),
                        t("input")
                    )),
                    instance("instance",
                        t("item",
                            t("value", "A")
                        ),
                        t("item",
                            t("value", "B")
                        )
                    ),
                    bind("/data/calcltr").type("string")
                        .calculate("instance('instance')/root/item[value = /data/input]/value"),
                    bind("/data/calcrtl").type("string")
                        .calculate("instance('instance')/root/item[/data/input = value]/value"),
                    bind("/data/input").type("string")
                )
            ),
            body(input("/data/input"))
        ));

        scenario.answer("/data/input", "A");
        assertThat(scenario.answerOf("/data/calcltr").getValue(), equalTo("A"));
        assertThat(scenario.answerOf("/data/calcrtl").getValue(), equalTo("A"));

        scenario.answer("/data/input", "B");
        assertThat(scenario.answerOf("/data/calcltr").getValue(), equalTo("B"));
        assertThat(scenario.answerOf("/data/calcrtl").getValue(), equalTo("B"));
    }

    @Test
    public void eqExpressionsWorkIfBothSidesAreRelative() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("calc"),
                        t("input")
                    )),
                    instance("instance",
                        t("item",
                            t("value1", "0"),
                            t("value2", "1")
                        ),
                        t("item",
                            t("value1", "A"),
                            t("value2", "A")
                        )
                    ),
                    bind("/data/calc").type("string")
                        .calculate("instance('instance')/root/item[value1 = value2]/value1"),
                    bind("/data/input").type("string")
                )
            ),
            body(input("/data/input"))
        ));

        assertThat(scenario.answerOf("/data/calc").getValue(), equalTo("A"));
    }

    @Test
    public void multiplePredicatesAtSameLevelDoNotGetConfused() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("calc"),
                        t("calc2"),
                        t("input1"),
                        t("input2")
                    )),
                    instance("instance",
                        t("item",
                            t("value", "A"),
                            t("count", "2"),
                            t("id", "A2")
                        ),
                        t("item",
                            t("value", "A"),
                            t("count", "3"),
                            t("id", "A3")
                        ),
                        t("item",
                            t("value", "B"),
                            t("count", "2"),
                            t("id", "B2")
                        )
                    ),
                    bind("/data/calc").type("string")
                        .calculate("instance('instance')/root/item[value = /data/input1][count = '3']/id"),
                    bind("/data/calc2").type("string")
                        .calculate("instance('instance')/root/item[value = /data/input2][count = '3']/id"),
                    bind("/data/input1").type("string"),
                    bind("/data/input2").type("string")
                )
            ),
            body(
                input("/data/input1"),
                input("/data/input2")
            )
        ));

        scenario.answer("/data/input1", "A");
        scenario.answer("/data/input2", "B");

        assertThat(scenario.answerOf("/data/calc").getValue(), equalTo("A3"));
        assertThat(scenario.answerOf("/data/calc2"), equalTo(null));
    }

    @Test
    public void multiplePredicatesAtDifferentLevelsDoNotGetConfused() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("calc"),
                        t("calc2"),
                        t("calc3"),
                        t("input1"),
                        t("input2")
                    )),
                    instance("instance",
                        t("item",
                            t("value",
                                t("prop1", "a"),
                                t("prop2", "b")
                            ),
                            t("count", "2"),
                            t("id", "A2")
                        ),
                        t("item",
                            t("value",
                                t("prop1", "x"),
                                t("prop2", "y")
                            ),
                            t("count", "3"),
                            t("id", "A3")
                        ),
                        t("item",
                            t("value",
                                t("prop1", "d"),
                                t("prop2", "c")
                            ),
                            t("count", "2"),
                            t("id", "B2")
                        )
                    ),
                    bind("/data/calc").type("string")
                        .calculate("instance('instance')/root/item[count = /data/input1]/value[prop1 = 'a']/prop2"),
                    bind("/data/calc2").type("string")
                        .calculate("instance('instance')/root/item[count = /data/input2]/value[prop1 = 'a']/prop2"),
                    bind("/data/calc3").type("string")
                        .calculate("instance('instance')/root/item[count = /data/input1]/value[prop1 = 'd']/prop2"),
                    bind("/data/input1").type("string"),
                    bind("/data/input2").type("string")
                )
            ),
            body(
                input("/data/input1"),
                input("/data/input2")
            )
        ));

        scenario.answer("/data/input1", "2");
        scenario.answer("/data/input2", "3");

        assertThat(scenario.answerOf("/data/calc").getValue(), equalTo("b"));
        assertThat(scenario.answerOf("/data/calc2"), equalTo(null));
        assertThat(scenario.answerOf("/data/calc3").getValue(), equalTo("c"));
    }

    @Test
    public void similarCmpAndEqExpressionsDoNotGetConfused() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("input"),
                        t("calculate1"),
                        t("calculate2")
                    )),
                    instance("instance",
                        item("1", "A"),
                        item("2", "B")
                    ),
                    bind("/data/input").type("string"),
                    bind("/data/calculate1").type("string")
                        .calculate("instance('instance')/root/item[value < /data/input]/label"),
                    bind("/data/calculate2").type("string")
                        .calculate("instance('instance')/root/item[value = /data/input]/label")
                )
            ),
            body(
                input("/data/input")
            )
        ));

        scenario.answer("/data/input", "2");
        assertThat(scenario.answerOf("/data/calculate1").getValue(), equalTo("A"));
        assertThat(scenario.answerOf("/data/calculate2").getValue(), equalTo("B"));
    }

    @Test
    public void differentEqExpressionsAreNotConfused() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("calc1"),
                        t("calc2"),
                        t("input1"),
                        t("input2")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/calc1").type("string")
                        .calculate("instance('instance')/root/item[value = /data/input1]/label"),
                    bind("/data/calc2").type("string")
                        .calculate("instance('instance')/root/item[label = /data/input2]/label"),
                    bind("/data/input").type("string")
                )
            ),
            body(
                input("/data/input1"),
                input("/data/input2")
            )
        ));

        scenario.answer("/data/input1", "a");
        scenario.answer("/data/input2", "B");

        assertThat(scenario.answerOf("/data/calc1").getValue(), equalTo("A"));
        assertThat(scenario.answerOf("/data/calc2").getValue(), equalTo("B"));
    }

    @Test
    public void differentKindsOfEqExpressionsAreNotConfused() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("calc1"),
                        t("calc2"),
                        t("input")
                    )),
                    instance("instance",
                        item("a", "A"),
                        item("b", "B")
                    ),
                    bind("/data/calc1").type("string")
                        .calculate("instance('instance')/root/item[value = 'a']/label"),
                    bind("/data/calc2").type("string")
                        .calculate("instance('instance')/root/item[value != 'a']/label"),
                    bind("/data/input").type("string")
                )
            ),
            body(
                input("/data/input")
            )
        ));

        assertThat(scenario.answerOf("/data/calc1").getValue(), equalTo("A"));
        assertThat(scenario.answerOf("/data/calc2").getValue(), equalTo("B"));
    }

    @Test
    public void repeatsUsedInCalculatesStayUpToDate() throws Exception {
        Scenario scenario = Scenario.init("Some form", html(
            head(
                title("Some form"),
                model(
                    mainInstance(t("data id=\"some-form\"",
                        t("repeat",
                            t("name"),
                            t("age")
                        ),
                        t("result")
                    )),
                    bind("/data/repeat/input").type("string"),
                    bind("/data/result").type("string").calculate("/data/repeat[name = 'John Bell']/age")
                )
            ),
            body(
                group("/data/repeat",
                    repeat("/data/repeat",
                        input("/data/repeat/name"),
                        input("/data/repeat/age")
                    )
                )
            )
        ));

        assertThat(scenario.answerOf("/data/result"), equalTo(null));

        scenario.createNewRepeat("/data/repeat");
        scenario.answer("/data/repeat[0]/name", "John Bell");
        scenario.answer("/data/repeat[0]/age", "70");

        assertThat(scenario.answerOf("/data/result").getValue(), equalTo("70"));
    }

    @Test
    public void eqPredicatesDoNotIncreaseLoadTime() {
        int evaluations = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation"), () -> {
                try {
                    Scenario.init("Some form", html(
                        head(
                            title("Some form"),
                            model(
                                mainInstance(t("data id=\"some-form\"",
                                    t("choice"),
                                    t("calculate1"),
                                    t("calculate2")
                                )),
                                instance("instance",
                                    item("a", "A"),
                                    item("b", "B")
                                ),
                                bind("/data/choice").type("string"),
                                bind("/data/calculate1").type("string")
                                    .calculate("instance('instance')/root/item[value = /data/choice]/label")
                            )
                        ),
                        body(
                            input("/data/choice")
                        )
                    ));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        );

        assertThat(evaluations, not(greaterThan(2)));
    }
}
