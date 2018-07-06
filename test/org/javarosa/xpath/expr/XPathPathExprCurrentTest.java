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

import com.sun.org.apache.bcel.internal.generic.Select;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static org.javarosa.test.utils.ResourcePathHelper.r;

public class XPathPathExprCurrentTest {
    @Test
    public void current_in_calculate_should_refer_to_node() {
        FormParseInit fpi = new FormParseInit(r("relative-current-ref.xml"));
        FormDef formDef = fpi.getFormDef();
        FormEntryModel formEntryModel = new FormEntryModel(formDef);
        FormEntryController formEntryController = new FormEntryController(formEntryModel);

        formEntryController.stepToNextEvent(); // group
        formEntryController.stepToNextEvent(); // name

        StringData name = new StringData("Bob");
        formEntryController.answerQuestion(name, true);
        // node with calculate that refers to name node using current() should have the same value as
        // the name node
        StringData relativeName = (StringData) formDef.getMainInstance().getRoot()
                .getFirstDescendantWithName("name_relative").getValue();
        assertEquals(name.getValue(), relativeName.getValue());

        formEntryController.stepToNextEvent(); // name_note

        formEntryController.stepToNextEvent(); // fruit
        Selection fruitSelection = new Selection(1);
        fruitSelection.attachChoice(formEntryModel.getQuestionPrompt().getQuestion());
        formEntryController.answerQuestion(new SelectOneData(fruitSelection), true);

        SelectOneData fruit = (SelectOneData) formDef.getFirstDescendantWithName("fruit").getValue();
        assertEquals("blueberry", fruit.getDisplayText());

        // === Variety question ===
        formEntryController.stepToNextEvent();
        // Collect: calls fep.getAnswerValue which calls fep.getSelectChoices which populates dynamic choices
        FormEntryPrompt formEntryPrompt = formEntryModel.getQuestionPrompt();
        Object answer = formEntryPrompt.getAnswerValue();
        // At this point there should be 2 matches at populateDynamicChoices line 974 but that's not the case

        Selection varietySelection = new Selection(1);
        QuestionDef q = formEntryPrompt.getQuestion();
        final ItemsetBinding dynamicChoices = q.getDynamicChoices();
        formDef.populateDynamicChoices(dynamicChoices, formEntryModel.getFormIndex().getReference());
        varietySelection.attachChoice(q);
        formEntryController.answerQuestion(new SelectOneData(varietySelection), true);

        SelectOneData variety = (SelectOneData) formDef.getFirstDescendantWithName("variety").getValue();
        assertEquals("collins", variety.getDisplayText());
    }

    @Test
    public void current_in_itemset_nodeset_should_fail() {
        FormParseInit fpi = new FormParseInit(r("relative-current-ref-repeat.xml"));
        FormDef formDef = fpi.getFormDef();
        FormEntryModel formEntryModel = new FormEntryModel(formDef);
        FormEntryController formEntryController = new FormEntryController(formEntryModel);
    }
}