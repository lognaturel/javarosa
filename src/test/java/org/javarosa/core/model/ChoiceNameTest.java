package org.javarosa.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.item;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.repeat;
import static org.javarosa.test.XFormsElement.select1;
import static org.javarosa.test.XFormsElement.select1Dynamic;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.javarosa.test.ResourcePathHelper.r;

import java.io.IOException;
import org.javarosa.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

public class ChoiceNameTest {
    @Test public void choiceNameCallOnLiteralChoiceValue_getsChoiceName() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("jr-choice-name.xml"));
        assertThat(scenario.answerOf("/jr-choice-name/literal_choice_name"), is(stringAnswer("Choice 2")));
    }

    @Test public void choiceNameCallOutsideOfRepeatWithStaticChoices_getsChoiceName() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("jr-choice-name.xml"));
        scenario.answer("/jr-choice-name/select_one_outside", "choice3");
        assertThat(scenario.answerOf("/jr-choice-name/select_one_name_outside"), is(stringAnswer("Choice 3")));
    }

    @Test public void choiceNameCallInRepeatWithStaticChoices_getsChoiceName() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("jr-choice-name.xml"));
        scenario.answer("/jr-choice-name/my-repeat[1]/select_one", "choice4");
        scenario.answer("/jr-choice-name/my-repeat[2]/select_one", "choice1");
        scenario.answer("/jr-choice-name/my-repeat[3]/select_one", "choice5");

        assertThat(scenario.answerOf("/jr-choice-name/my-repeat[1]/select_one_name"), is(stringAnswer("Choice 4")));
        assertThat(scenario.answerOf("/jr-choice-name/my-repeat[2]/select_one_name"), is(stringAnswer("Choice 1")));
        assertThat(scenario.answerOf("/jr-choice-name/my-repeat[3]/select_one_name"), is(stringAnswer("Choice 5")));
    }

    @Test public void choiceNameCall_respectsLanguage() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("jr-choice-name.xml"));
        scenario.setLanguage("French (fr)");
        scenario.answer("/jr-choice-name/select_one_outside", "choice3");
        assertThat(scenario.answerOf("/jr-choice-name/select_one_name_outside"), is(stringAnswer("Choix 3")));
        scenario.answer("/jr-choice-name/my-repeat[1]/select_one", "choice4");
        assertThat(scenario.answerOf("/jr-choice-name/my-repeat[1]/select_one_name"), is(stringAnswer("Choix 4")));

        scenario.setLanguage("English (en)");
        // TODO: why does test fail if value is not set to choice3 again? Does changing language not trigger recomputation?
        scenario.answer("/jr-choice-name/select_one_outside", "choice3");
        assertThat(scenario.answerOf("/jr-choice-name/select_one_name_outside"), is(stringAnswer("Choice 3")));

        // TODO: why does test fail if value is not set to choice4 again? Does changing language not trigger recomputation?
        scenario.answer("/jr-choice-name/my-repeat[1]/select_one", "choice4");
        assertThat(scenario.answerOf("/jr-choice-name/my-repeat[1]/select_one_name"), is(stringAnswer("Choice 4")));
    }

    // The choice list for question cocotero with dynamic itemset is populated on DAG initialization time triggered by the jr:choice-name
    // expression in the calculate.
    @Test public void choiceNameCallWithDynamicChoicesAndNoPredicate_selectsName() throws XFormParser.ParseException {
        Scenario scenario = Scenario.init(r("jr-choice-name.xml"));
        scenario.answer("/jr-choice-name/cocotero_a", "a");
        scenario.answer("/jr-choice-name/cocotero_b", "b");
        assertThat(scenario.answerOf("/jr-choice-name/cocotero_name"), is(stringAnswer("Cocotero a-b")));
    }

    // The choice list for question city with dynamic itemset is populated at DAG initialization time. Since country hasn't been
    // set yet, the choice list is empty. Setting the country does not automatically trigger re-computation of the choice list for the
    // city question. Instead, clients trigger a recomputation of the list when the list is displayed.
    @Test public void choiceNameCallWithDynamicChoicesAndPredicate_requiresExplicitDynamicChoicesRecomputation() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Dynamic Choices and Predicates", html(
            head(
                title("Dynamic Choices and Predicates"),
                model(
                    mainInstance(t("data id=\"dynamic-choices-predicates\"",
                        t("country"),
                        t("city"),
                        t("city_name")
                    )),

                    t("itext",
                        t("translation lang=\"default\"",
                            t("text id=\"static_instance-countries-0\"", t("value", "Canada")),
                            t("text id=\"static_instance-countries-1\"", t("value", "France")),
                            t("text id=\"static_instance-cities-0\"", t("value", "Montréal")),
                            t("text id=\"static_instance-cities-1\"", t("value", "Grenoble")))
                    ),

                    t("instance id=\"cities\"",
                        t("root",
                            t("item", t("itextId", "static_instance-cities-0"), t("name", "montreal"), t("country", "canada")),
                            t("item", t("itextId", "static_instance-cities-1"), t("name", "grenoble"), t("country", "france")))
                    ),
                    bind("/data/country").type("string"),
                    bind("/data/city").type("string"),
                    bind("/data/city_name").type("string").calculate("jr:choice-name(/data/city,'/data/city')")
                )
            ),
            body(
                select1("/data/country", item("canada", "Canada"), item("france", "France")),

                t("select1 ref=\"/data/city\"", t("itemset nodeset=\"instance('cities')/root/item[selected(country,/data/country)]\"",
                    t("value ref=\"name\""), t("label ref=\"jr:itext(itextId)\"")))
            )));

        scenario.answer("/data/country", "france");

        // Trigger recomputation of the city choice list
        assertThat(scenario.choicesOf("/data/city").get(0).getValue(), is("grenoble"));

        scenario.answer("/data/city", "grenoble");
        assertThat(scenario.answerOf("/data/city_name"), is(stringAnswer("Grenoble")));
    }

    @Test public void choiceNameCallWithIndexedRepeatAndStaticChoices_worksWithMultipleRepeats() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Static choices in repeat", html(
            head(
                title("Static choices in repeat"),
                model(
                    mainInstance(t("data id=\"static-choices-repeat\"",
                        t("thing",
                            t("choice")),
                        t("choice1_label")
                    )),

                    bind("/data/thing/choice").type("string"),
                    bind("/data/choice1_label").type("string").calculate("jr:choice-name(indexed-repeat(/data/thing/choice, /data/thing, 1),'/data/thing/choice')")
                )
            ),
            body(
                repeat("/data/thing",
                    select1("/data/thing/choice",
                        item("choice1", "Choice1"),
                        item("choice2", "Choice2"))
            ))));

        scenario.next();
        scenario.next();
        scenario.next();
        scenario.createNewRepeat();

        scenario.answer("/data/thing[1]/choice", "choice1");
        assertThat(scenario.answerOf("/data/choice1_label"), is(stringAnswer("Choice1")));

        scenario.answer("/data/thing[2]/choice", "choice2");
        assertThat(scenario.answerOf("/data/choice1_label"), is(stringAnswer("Choice1")));
    }

    @Test public void choiceNameCallWithIndexedRepeatAndDynamicChoices_worksWithMultipleRepeats() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init("Dynamic choices in repeat", html(
            head(
                title("Dynamic choices in repeat"),
                model(
                    mainInstance(t("data id=\"dynamic-choices-repeat\"",
                        t("thing",
                            t("choice")),
                        t("choice1_label")
                    )),

                    t("instance id=\"choices\"",
                        t("root",
                            item("choice1", "Choice1"),
                            item("choice2", "Choice2"))
                    ),
                    bind("/data/thing/choice").type("string"),
                    bind("/data/choice1_label").type("string").calculate("jr:choice-name(indexed-repeat(/data/thing/choice, /data/thing, 1),'/data/thing/choice')")
                )
            ),
            body(
                repeat("/data/thing",
                    select1Dynamic("/data/thing/choice", "instance('choices')/root/item"))
            )));

        scenario.next();
        scenario.next();
        scenario.next();
        scenario.createNewRepeat();

        scenario.answer("/data/thing[1]/choice","choice1");
        assertThat(scenario.answerOf("/data/choice1_label"), is(stringAnswer("Choice1")));

        scenario.answer("/data/thing[2]/choice", "choice2");
        assertThat(scenario.answerOf("/data/choice1_label"), is(stringAnswer("Choice1")));
    }
}
