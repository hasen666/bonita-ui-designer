/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.experimental.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.web.designer.builder.DataBuilder.aJSONData;
import static org.bonitasoft.web.designer.builder.DataBuilder.aUrlParameterData;
import static org.bonitasoft.web.designer.builder.DataBuilder.anExpressionData;
import static org.bonitasoft.web.designer.builder.DataBuilder.anURLData;
import static org.bonitasoft.web.designer.builder.PropertyValueBuilder.aConstantPropertyValue;
import static org.bonitasoft.web.designer.builder.PropertyValueBuilder.aInterpolationPropertyValue;
import static org.bonitasoft.web.designer.builder.PropertyValueBuilder.anExpressionPropertyValue;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aContract;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aContractWithDataRefAndAggregation;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aContractWithMultipleInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aSimpleContract;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aSimpleContractWithDataRef;

import java.util.ArrayList;

import org.bonitasoft.web.designer.experimental.mapping.data.BusinessQueryDataFactory;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.Alignment;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ParameterConstants;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.TextWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.TitleWidget;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.EditMode;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class ContractToPageMapperTest {

    JacksonObjectMapper objectMapper = new JacksonObjectMapper(new ObjectMapper());
    private ContractInputToWidgetMapper contractToWidgetMapper = new ContractInputToWidgetMapper(new DimensionFactory(), objectMapper);
    ContractToContainerMapper contractToContainerMapper = new ContractToContainerMapper(contractToWidgetMapper);

    @Test
    public void visit_a_contract_when_creating_a_page() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.TASK);

        assertThat(getTaskFormContainerContent(page).getRows()).hasSize(5);
    }

    private ContractToPageMapper makeContractToPageMapper() {
        return new ContractToPageMapper(contractToWidgetMapper, contractToContainerMapper, objectMapper, new DimensionFactory(), new BusinessQueryDataFactory());
    }

    @Test
    public void should_create_a_page_with_the_form_type() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        assertThat(page.getType()).isEqualTo("form");
    }

    @Test
    public void should_create_a_page_with_a_form_container() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        assertThat(page.getRows()).hasSize(2);
        assertThat(page.getRows().get(1).get(0)).isInstanceOf(FormContainer.class);
    }

    @Test
    public void create_a_page_with_form_input_and_output() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        assertThat(page.getData()).contains(entry("formInput", aJSONData().value(objectMapper.prettyPrint("{\"names\":[]}")).build()));
        assertThat(page.getData()).contains(entry("formOutput", anExpressionData().value("var output = {\n\t'names': $data.formInput.names\n};\nreturn output;").build()));
    }

    @Test
    public void create_a_page_with_a_context_url_data_for_task() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        assertThat(page.getData()).contains(entry("context", anURLData().value("../API/bpm/userTask/{{taskId}}/context").build()));
    }

    @Test
    public void create_a_page_with_a_id_expression_data_for_task_scope() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        assertThat(page.getData()).contains(entry("taskId", aUrlParameterData().value("id").build()));
    }

    @Test
    public void create_a_page_without_a_context_url_data_for_process_scope() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.PROCESS);

        assertThat(page.getData()).doesNotContain(entry("context", anURLData().value("/bonita/API/bpm/userTask/{{taskId}}/context").build()));
    }

    @Test
    public void create_a_page_without_an_id_expression_data_for_process() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.PROCESS);

        assertThat(page.getData()).doesNotContain(entry("taskId", aUrlParameterData().value("id").build()));
    }

    @Test
    public void create_a_page_without_output_data_for_overview_scope() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContract().build(), FormScope.OVERVIEW);

        assertThat(page.getData()).isEmpty();
    }

    @Test
    public void create_a_page_with_a_submit_button_sending_contract() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        Component submitButon = (Component) getTaskFormContainerContent(page).getRows().get(3).get(0);
        assertThat(submitButon.getId()).isEqualTo("pbButton");
        assertThat(submitButon.getPropertyValues()).contains(
                entry(ParameterConstants.DATA_TO_SEND_PARAMETER, anExpressionPropertyValue("formOutput")),
                entry(ParameterConstants.ACTION_PARAMETER, aConstantPropertyValue(ButtonAction.SUBMIT_TASK.getValue())),
                entry(ParameterConstants.TARGET_URL_ON_SUCCESS_PARAMETER, aInterpolationPropertyValue("/bonita")));
    }

    @Test
    public void create_a_page_and_fetch_associated_task() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.TASK);

        assertThat(page.getData()).contains(entry("task", anURLData().value("../API/bpm/userTask/{{taskId}}").build()));
    }

    @Test
    public void create_a_page_display_task_name() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();
        TitleWidget title = new TitleWidget();
        title.setLevel("Level 1");
        title.setText("{{ task.displayName }}");
        title.setAlignment(Alignment.CENTER);

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.TASK);

        assertThat(grabTaskInformation(page).getRows().get(0).get(0)).isEqualToIgnoringGivenFields(title.toComponent(new DimensionFactory()), "reference");
    }

    @Test
    public void create_a_page_display_task_description() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();
        TextWidget description = new TextWidget();
        description.setText("{{ task.displayDescription }}");

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.TASK);

        assertThat(grabTaskInformation(page).getRows().get(1).get(0)).isEqualToIgnoringGivenFields(description.toComponent(new DimensionFactory()), "reference");
    }

    @Test
    public void should_create_an_empty_container_when_contract_is_empty_and_scope_is_overview() throws Exception {
        Contract anEmptyContract = aContract().build();

        Page page = makeContractToPageMapper().createFormPage("myPage", anEmptyContract, FormScope.OVERVIEW);

        Container container = (Container) page.getRows().get(0).get(0);
        assertThat(container.getRows().get(0)).isEqualTo(new ArrayList<>());
    }
    
    @Test
    public void should_create_a_page_with_a_business_data_when_contract_contains_data_reference_on_a_task() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContractWithDataRef(EditMode.EDIT), FormScope.TASK);

        assertThat(page.getData()).contains(entry("employee",anURLData().value("../{{context.employee_ref.link}}").build()));
        assertThat(page.getData()).contains(entry("employee_addresses",anURLData().value("{{employee|lazyRef:'addresses'}}").build()));
        assertThat(page.getData()).contains(entry("employee_manager",anURLData().value("{{employee|lazyRef:'manager'}}").build()));
        assertThat(page.getData()).contains(entry("employee_manager_addresses",anURLData().value("{{employee_manager|lazyRef:'addresses'}}").build()));
        assertThat(page.getData()).doesNotContain(entry("employee_addresses_country",anURLData().value("{{employee_addresses|lazyRef:'country'}}").build()));
    }
    
    @Test
    public void should_create_a_page_with_a_query_varaible_when_contract_contains_data_reference_with_aggregation() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithDataRefAndAggregation(EditMode.EDIT), FormScope.TASK);

        assertThat(page.getData()).contains(entry("employee_query",anURLData().value("../API/bdm/businessData/org.test.Employee?q=find&p=0&c=99").build()));
    }
    
    @Test
    public void should_create_a_page_without_formInput_when_contract_contains__only_data_reference_on_a_task() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContractWithDataRef(EditMode.EDIT), FormScope.TASK);

        assertThat(page.getData().keySet()).doesNotContain("formInput");
    }

    private Container grabTaskInformation(Page page) {
        return (Container) page.getRows().get(0).get(0);
    }

    public Container getTaskFormContainerContent(Page page) {
        return ((FormContainer) page.getRows().get(1).get(0)).getContainer();
    }
}
